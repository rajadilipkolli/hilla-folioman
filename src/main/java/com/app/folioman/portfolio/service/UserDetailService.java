package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.mapper.CasDetailsMapper;
import com.app.folioman.portfolio.models.CasDTO;
import com.app.folioman.portfolio.models.UserFolioDTO;
import com.app.folioman.portfolio.models.UserSchemeDTO;
import com.app.folioman.portfolio.models.UserTransactionDTO;
import com.app.folioman.portfolio.models.response.UploadFileResponse;
import com.app.folioman.shared.LocalDateUtility;
import com.app.folioman.shared.UploadedSchemesList;
import com.app.folioman.shared.UserSchemeDetailService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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

    UserDetailService(
            PortfolioServiceHelper portfolioServiceHelper,
            CasDetailsMapper casDetailsMapper,
            UserCASDetailsService userCASDetailsService,
            InvestorInfoService investorInfoService,
            UserTransactionDetailsService userTransactionDetailsService,
            UserFolioDetailService userFolioDetailService,
            UserSchemeDetailService userSchemeDetailService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.portfolioServiceHelper = portfolioServiceHelper;
        this.casDetailsMapper = casDetailsMapper;
        this.userCASDetailsService = userCASDetailsService;
        this.investorInfoService = investorInfoService;
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userFolioDetailService = userFolioDetailService;
        this.userSchemeDetailService = userSchemeDetailService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public UploadFileResponse upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = parseCasDTO(multipartFile);
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

        userSchemaTransactionMap.forEach((rtaCodeFromRequest, requestTransactions) -> {
            List<UserTransactionDetails> dbTransactions =
                    userSchemaTransactionMapFromDB.getOrDefault(rtaCodeFromRequest, List.of());

            if (requestTransactions.size() != dbTransactions.size()) {
                // New transactions added to scheme
                Set<LocalDate> transactionDateSetDB = dbTransactions.stream()
                        .map(UserTransactionDetails::getTransactionDate)
                        .collect(Collectors.toSet());

                requestTransactions.parallelStream().forEach(userTransactionDTO -> {
                    LocalDate newTransactionDate = userTransactionDTO.date();
                    if (!transactionDateSetDB.contains(newTransactionDate)) {
                        log.info(
                                "New transaction on date: {} created for rtaCode {} that is not present in the database",
                                newTransactionDate,
                                rtaCodeFromRequest);
                        UserTransactionDetails userTransactionDetailsEntity =
                                casDetailsMapper.transactionDTOToTransactionEntity(userTransactionDTO);

                        existingUserSchemeDetailsList.forEach(userSchemeDetailsEntity -> {
                            if (rtaCodeFromRequest.equals(userSchemeDetailsEntity.getRtaCode())) {
                                userSchemeDetailsEntity.addTransaction(userTransactionDetailsEntity);
                                newTransactions.incrementAndGet();
                            }
                        });
                    }
                });
            }
        });
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

        requestedFolioSchemesMap.forEach((folioFromRequest, requestSchemes) -> {
            List<UserSchemeDetails> existingSchemesFromDB =
                    existingFolioSchemesMap.getOrDefault(folioFromRequest, new ArrayList<>());

            Set<String> rtaCodeSet = existingSchemesFromDB.stream()
                    .map(UserSchemeDetails::getRtaCode)
                    .collect(Collectors.toSet());

            requestSchemes.stream()
                    .filter(scheme -> !rtaCodeSet.contains(scheme.rtaCode()))
                    .forEach(userSchemeDTO -> {
                        log.info(
                                "New RTACode: {} created for folio : {} that is not present in the database",
                                userSchemeDTO.rtaCode(),
                                folioFromRequest);
                        UserSchemeDetails userSchemeDetailsEntity =
                                casDetailsMapper.schemeDTOToSchemeEntity(userSchemeDTO, newTransactions);

                        userCASDetails.getFolios().forEach(userFolioDetailsEntity -> {
                            if (folioFromRequest.equals(userFolioDetailsEntity.getFolio())) {
                                userFolioDetailsEntity.addScheme(userSchemeDetailsEntity);
                                newSchemes.incrementAndGet();
                            }
                        });
                    });
        });
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
        UserCASDetails savedCasDetailsEntity = userCASDetailsService.saveEntity(userCASDetails);
        CompletableFuture.runAsync(() -> userFolioDetailService.setPANIfNotSet(savedCasDetailsEntity.getId()));
        CompletableFuture.runAsync(userSchemeDetailService::setUserSchemeAMFIIfNull);
        List<Long> schemesList = userCASDetails.getFolios().stream()
                .map(UserFolioDetails::getSchemes)
                .flatMap(List::stream)
                .map(UserSchemeDetails::getAmfi)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        applicationEventPublisher.publishEvent(new UploadedSchemesList(schemesList));
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
}
