package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.UserSchemeDetailService;
import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.mapper.CasDetailsMapper;
import com.app.folioman.portfolio.models.request.CasDTO;
import com.app.folioman.portfolio.models.request.UserFolioDTO;
import com.app.folioman.portfolio.models.request.UserSchemeDTO;
import com.app.folioman.portfolio.models.request.UserTransactionDTO;
import com.app.folioman.portfolio.models.response.PortfolioDetailsDTO;
import com.app.folioman.portfolio.models.response.PortfolioResponse;
import com.app.folioman.portfolio.models.response.UploadFileResponse;
import com.app.folioman.shared.LocalDateUtility;
import com.app.folioman.shared.UploadedSchemesList;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class UserDetailService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailService.class);

    private final PortfolioServiceHelper portfolioServiceHelper;
    private final CasDetailsMapper casDetailsMapper;
    private final UserCASDetailsService userCASDetailsService;
    private final InvestorInfoService investorInfoService;
    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserFolioDetailService userFolioDetailService;
    private final UserSchemeDetailService userSchemeDetailService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PortfolioValueUpdateService portfolioValueUpdateService;

    UserDetailService(
            PortfolioServiceHelper portfolioServiceHelper,
            CasDetailsMapper casDetailsMapper,
            UserCASDetailsService userCASDetailsService,
            InvestorInfoService investorInfoService,
            UserTransactionDetailsService userTransactionDetailsService,
            UserFolioDetailService userFolioDetailService,
            UserSchemeDetailService userSchemeDetailService,
            ApplicationEventPublisher applicationEventPublisher,
            PortfolioValueUpdateService portfolioValueUpdateService) {
        this.portfolioServiceHelper = portfolioServiceHelper;
        this.casDetailsMapper = casDetailsMapper;
        this.userCASDetailsService = userCASDetailsService;
        this.investorInfoService = investorInfoService;
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userFolioDetailService = userFolioDetailService;
        this.userSchemeDetailService = userSchemeDetailService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.portfolioValueUpdateService = portfolioValueUpdateService;
    }

    public UploadFileResponse upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = parseCasDTO(multipartFile);
        boolean existingUser = validateCasDTO(casDTO);
        return existingUser ? processExistingUser(casDTO) : processNewUser(casDTO);
    }

    /**
     * Processes a CasDTO object directly, bypassing the file parsing step.
     * This is useful when the CasDTO is generated from another source, like a PDF conversion.
     *
     * @param casDTO The CasDTO object to process
     * @return UploadFileResponse with processing statistics
     */
    public UploadFileResponse uploadFromDto(CasDTO casDTO) {
        log.info("Processing CasDTO from converted source");
        boolean existingUser = validateCasDTO(casDTO);
        return existingUser ? processExistingUser(casDTO) : processNewUser(casDTO);
    }

    private UploadFileResponse processExistingUser(CasDTO casDTO) {
        AtomicInteger newTransactions = new AtomicInteger();
        AtomicInteger newFolios = new AtomicInteger();
        AtomicInteger newSchemes = new AtomicInteger();
        LocalDate from = LocalDateUtility.parse(casDTO.statementPeriod().from());
        LocalDate to = LocalDateUtility.parse(casDTO.statementPeriod().myto());

        long userTransactionFromReqCount = portfolioServiceHelper.countTransactionsByUserFolioDTOList(casDTO.folios());
        Long userTransactionFromDBCount = userTransactionDetailsService.findAllTransactionsByEmailNameAndPeriod(
                casDTO.investorInfo().name(), casDTO.investorInfo().email(), from, to);
        UserCASDetails userCASDetails = null;

        if (userTransactionFromReqCount == userTransactionFromDBCount) {
            log.info("No new transactions are added");
        } else {
            userCASDetails = importNewTransaction(
                    casDTO,
                    newFolios,
                    newSchemes,
                    newTransactions,
                    userTransactionFromReqCount,
                    userTransactionFromDBCount);
        }

        return new UploadFileResponse(
                newFolios.get(),
                newSchemes.get(),
                newTransactions.get(),
                userCASDetails != null ? userCASDetails.getId() : 0L);
    }

    private UserCASDetails importNewTransaction(
            CasDTO casDTO,
            AtomicInteger newFolios,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions,
            long userTransactionFromReqCount,
            Long userTransactionFromDBCount) {

        UserCASDetails userCASDetails = userCASDetailsService.findByInvestorEmailAndName(
                casDTO.investorInfo().email(), casDTO.investorInfo().name());

        // Optimize folio and scheme processing by processing only if counts don't match
        processNewFolios(casDTO.folios(), userCASDetails, newFolios, newSchemes, newTransactions);
        processNewSchemesAndTransactions(
                casDTO.folios(),
                userCASDetails,
                newSchemes,
                newTransactions,
                userTransactionFromReqCount,
                userTransactionFromDBCount);

        return getUserCASDetails(userCASDetails);
    }

    private void addNewTransactions(
            List<UserFolioDTO> folios,
            UserCASDetails userCASDetails,
            AtomicInteger newTransactions,
            long userTransactionFromReqCount,
            Long userTransactionFromDBCount) {

        // Check if all new transactions are added as part of adding schemes
        if (userTransactionFromReqCount == (userTransactionFromDBCount + newTransactions.get())) {
            log.info("All new transactions are added as part of adding schemes, hence skipping");
        } else {
            // New transactions are added

            // Grouping by rtaCode for userSchemaTransactionMap
            Map<String, List<UserTransactionDTO>> userSchemaTransactionMap = groupTransactionBySchemes(folios);

            // Grouping by rtaCode for userSchemaTransactionMapFromDB
            List<UserSchemeDetails> existingUserSchemeDetailsList = userCASDetails.getFolios().stream()
                    .map(UserFolioDetails::getSchemes)
                    .flatMap(List::stream)
                    .toList();

            Map<String, List<UserTransactionDetails>> userSchemaTransactionMapFromDB =
                    groupExistingTransactionsByRtaCode(existingUserSchemeDetailsList);

            // Update transactions in existing schemes
            processNewTransactions(
                    newTransactions,
                    userSchemaTransactionMap,
                    userSchemaTransactionMapFromDB,
                    existingUserSchemeDetailsList);
        }
    }

    private void processNewTransactions(
            AtomicInteger newTransactions,
            Map<String, List<UserTransactionDTO>> userSchemaTransactionMap,
            Map<String, List<UserTransactionDetails>> userSchemaTransactionMapFromDB,
            List<UserSchemeDetails> existingUserSchemeDetailsList) {

        // Create a map to collect new transactions by their scheme
        Map<UserSchemeDetails, List<UserTransactionDetails>> transactionsByScheme = new HashMap<>();

        userSchemaTransactionMap.forEach((rtaCodeFromRequest, requestTransactions) -> {
            List<UserTransactionDetails> dbTransactions =
                    userSchemaTransactionMapFromDB.getOrDefault(rtaCodeFromRequest, List.of());

            if (requestTransactions.size() != dbTransactions.size()) {
                // For efficient lookup, create a set of transaction dates that already exist
                Set<LocalDate> transactionDateSetDB = dbTransactions.stream()
                        .map(UserTransactionDetails::getTransactionDate)
                        .collect(Collectors.toSet());

                // Find matching scheme once outside the inner loop
                UserSchemeDetails matchingScheme = existingUserSchemeDetailsList.stream()
                        .filter(scheme -> rtaCodeFromRequest.equals(scheme.getRtaCode()))
                        .findFirst()
                        .orElse(null);

                if (matchingScheme != null) {
                    // Process all new transactions for this scheme
                    List<UserTransactionDetails> newTransactionsForScheme = requestTransactions.stream()
                            .filter(dto -> !transactionDateSetDB.contains(dto.date()))
                            .map(dto -> {
                                log.info(
                                        "New transaction on date: {} created for rtaCode {} that is not present in the database",
                                        dto.date(),
                                        rtaCodeFromRequest);
                                UserTransactionDetails entity = casDetailsMapper.transactionDTOToTransactionEntity(dto);
                                entity.setUserSchemeDetails(matchingScheme);
                                newTransactions.incrementAndGet();
                                return entity;
                            })
                            .collect(Collectors.toList());

                    if (!newTransactionsForScheme.isEmpty()) {
                        transactionsByScheme.put(matchingScheme, newTransactionsForScheme);
                    }
                }
            }
        });

        // Process all transactions in a single batch where possible
        if (!transactionsByScheme.isEmpty()) {
            List<UserTransactionDetails> allNewTransactions = new ArrayList<>();

            // Update in-memory relationships
            transactionsByScheme.forEach((scheme, transactions) -> {
                scheme.getTransactions().addAll(transactions);
                allNewTransactions.addAll(transactions);
            });

            // Save all new transactions in a single batch operation
            if (!allNewTransactions.isEmpty()) {
                log.info("Batch saving {} new transactions", allNewTransactions.size());
                userTransactionDetailsService.saveTransactions(allNewTransactions);
            }
        }
    }

    private Map<String, List<UserTransactionDetails>> groupExistingTransactionsByRtaCode(
            List<UserSchemeDetails> existingUserSchemeDetailsList) {
        return existingUserSchemeDetailsList.stream()
                .collect(Collectors.groupingBy(
                        UserSchemeDetails::getRtaCode,
                        Collectors.flatMapping(
                                userSchemeDetailsEntity -> userSchemeDetailsEntity.getTransactions().stream(),
                                Collectors.toList())));
    }

    private Map<String, List<UserTransactionDTO>> groupTransactionBySchemes(List<UserFolioDTO> folios) {
        return folios.stream()
                .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                .collect(Collectors.groupingBy(
                        UserSchemeDTO::rtaCode,
                        Collectors.flatMapping(schemeDTO -> schemeDTO.transactions().stream(), Collectors.toList())));
    }

    private void processNewSchemesAndTransactions(
            List<UserFolioDTO> folios,
            UserCASDetails userCASDetails,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions,
            long userTransactionFromReqCount,
            Long userTransactionFromDBCount) {
        // Check if all new transactions are added as part of adding folios
        if (userTransactionFromReqCount == (userTransactionFromDBCount + newTransactions.get())) {
            log.info("All new transactions are added as part of adding folios, hence skipping");
        } else {
            // New schemes or transactions are added

            // Grouping by folio for requestedFolioSchemesMap
            Map<String, List<UserSchemeDTO>> requestedFolioSchemesMap = groupSchemesByFolio(folios);
            Map<String, List<UserSchemeDetails>> existingFolioSchemesMap =
                    groupExistingSchemes(userCASDetails.getFolios());

            // Update schemes in existing folios
            updateExistingFoliosWithNewSchemes(
                    requestedFolioSchemesMap, existingFolioSchemesMap, userCASDetails, newSchemes, newTransactions);

            // add new transactions in existing schemes
            addNewTransactions(
                    folios, userCASDetails, newTransactions, userTransactionFromReqCount, userTransactionFromDBCount);
        }
    }

    private void updateExistingFoliosWithNewSchemes(
            Map<String, List<UserSchemeDTO>> requestedFolioSchemesMap,
            Map<String, List<UserSchemeDetails>> existingFolioSchemesMap,
            UserCASDetails userCASDetails,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions) {

        Map<UserFolioDetails, List<UserSchemeDetails>> newSchemesByFolio = new HashMap<>();

        requestedFolioSchemesMap.forEach((folioFromRequest, requestSchemes) -> {
            List<UserSchemeDetails> existingSchemesFromDB =
                    existingFolioSchemesMap.getOrDefault(folioFromRequest, new ArrayList<>());

            Set<String> rtaCodeSet = existingSchemesFromDB.stream()
                    .map(UserSchemeDetails::getRtaCode)
                    .collect(Collectors.toSet());

            // Find the matching folio entity
            UserFolioDetails matchingFolio = userCASDetails.getFolios().stream()
                    .filter(folio -> folioFromRequest.equals(folio.getFolio()))
                    .findFirst()
                    .orElse(null);

            if (matchingFolio != null) {
                // Collect all new schemes for this folio
                List<UserSchemeDetails> newSchemesForFolio = requestSchemes.stream()
                        .filter(scheme -> !rtaCodeSet.contains(scheme.rtaCode()))
                        .map(userSchemeDTO -> {
                            log.info(
                                    "New RTACode: {} created for folio : {} that is not present in the database",
                                    userSchemeDTO.rtaCode(),
                                    folioFromRequest);
                            UserSchemeDetails entity =
                                    casDetailsMapper.schemeDTOToSchemeEntity(userSchemeDTO, newTransactions);
                            entity.setUserFolioDetails(matchingFolio);
                            newSchemes.incrementAndGet();
                            return entity;
                        })
                        .collect(Collectors.toList());

                if (!newSchemesForFolio.isEmpty()) {
                    newSchemesByFolio.put(matchingFolio, newSchemesForFolio);
                }
            }
        });

        // Process all schemes in a single batch where possible
        if (!newSchemesByFolio.isEmpty()) {
            // Update in-memory relationships
            newSchemesByFolio.forEach((folio, schemes) -> {
                folio.getSchemes().addAll(schemes);
            });
        }
    }

    private Map<String, List<UserSchemeDetails>> groupExistingSchemes(
            List<UserFolioDetails> existingUserFolioDetailsEntityList) {
        return existingUserFolioDetailsEntityList.stream()
                .collect(Collectors.groupingBy(
                        UserFolioDetails::getFolio,
                        Collectors.flatMapping(
                                userFolioDetailsEntity -> userFolioDetailsEntity.getSchemes().stream(),
                                Collectors.toList())));
    }

    private Map<String, List<UserSchemeDTO>> groupSchemesByFolio(List<UserFolioDTO> folios) {
        return folios.stream()
                .collect(Collectors.groupingBy(
                        UserFolioDTO::folio,
                        Collectors.flatMapping(folio -> folio.schemes().stream(), Collectors.toList())));
    }

    private void processNewFolios(
            List<UserFolioDTO> folios,
            UserCASDetails userCASDetails,
            AtomicInteger newFolios,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions) {

        Set<String> foliosListFromDB = userCASDetails.getFolios().stream()
                .map(UserFolioDetails::getFolio)
                .collect(Collectors.toSet());
        Set<String> foliosFromReq = folios.stream().map(UserFolioDTO::folio).collect(Collectors.toSet());

        foliosFromReq.stream()
                .filter(folio -> !foliosListFromDB.contains(folio))
                .forEach(newFolio -> {
                    Optional<UserFolioDTO> matchingDTO = folios.stream()
                            .filter(dto -> dto.folio().equals(newFolio))
                            .findFirst();
                    matchingDTO.ifPresent(dto -> {
                        userCASDetails.addFolioEntity(
                                casDetailsMapper.mapUserFolioDTOToUserFolioDetails(dto, newSchemes, newTransactions));
                        newFolios.incrementAndGet();
                    });
                });
    }

    private UserCASDetails getUserCASDetails(UserCASDetails userCASDetails) {
        // Extract AMFI codes before saving to avoid extra database fetches later
        List<Long> schemesList = userCASDetails.getFolios().stream()
                .map(UserFolioDetails::getSchemes)
                .flatMap(List::stream)
                .map(UserSchemeDetails::getAmfi)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()); // Using collect instead of toList() for thread safety

        // Save entity in a single transaction
        UserCASDetails savedCasDetailsEntity = userCASDetailsService.saveEntity(userCASDetails);

        // Run non-critical post-processing tasks asynchronously
        Long savedId = savedCasDetailsEntity.getId();
        CompletableFuture.runAsync(() -> userFolioDetailService.setPANIfNotSet(savedId));
        CompletableFuture.runAsync(userSchemeDetailService::setUserSchemeAMFIIfNull);

        // Publish event with pre-collected schemes list
        if (!schemesList.isEmpty()) {
            applicationEventPublisher.publishEvent(new UploadedSchemesList(schemesList));
        }

        // Start portfolio value update asynchronously
        portfolioValueUpdateService.updatePortfolioValue(savedCasDetailsEntity);

        return savedCasDetailsEntity;
    }

    private CasDTO parseCasDTO(MultipartFile multipartFile) throws IOException {
        return portfolioServiceHelper.readValue(multipartFile.getBytes(), CasDTO.class);
    }

    private boolean validateCasDTO(CasDTO casDTO) {
        if (casDTO.investorInfo() == null) {
            throw new IllegalArgumentException("Investor information is missing!");
        }
        String email = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();
        if (email.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Email or Name invalid!");
        }
        if (CollectionUtils.isEmpty(casDTO.folios())) {
            throw new IllegalArgumentException("No folios found!");
        }
        return investorInfoService.existsByEmailAndName(email, name);
    }

    private UploadFileResponse processNewUser(CasDTO casDTO) {
        AtomicInteger newTransactions = new AtomicInteger();
        AtomicInteger newFolios = new AtomicInteger();
        AtomicInteger newSchemes = new AtomicInteger();
        UserCASDetails userCASDetails = casDetailsMapper.convert(casDTO, newFolios, newSchemes, newTransactions);
        UserCASDetails savedCasDetailsEntity = getUserCASDetails(userCASDetails);
        return new UploadFileResponse(
                newFolios.get(), newSchemes.get(), newTransactions.get(), savedCasDetailsEntity.getId());
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate evaluationDate) {
        List<PortfolioDetailsDTO> portfolioDetailsDTOList = portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(
                panNumber, LocalDateUtility.getAdjustedDateOrDefault(evaluationDate));
        BigDecimal totalPortfolioValue = portfolioDetailsDTOList.stream()
                .map(PortfolioDetailsDTO::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(totalPortfolioValue.setScale(4, RoundingMode.HALF_UP), portfolioDetailsDTOList);
    }
}
