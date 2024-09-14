package com.example.application.portfolio.service;

import static java.util.stream.Collectors.*;

import com.example.application.portfolio.entities.UserCASDetails;
import com.example.application.portfolio.entities.UserFolioDetails;
import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.entities.UserTransactionDetails;
import com.example.application.portfolio.mapper.CasDetailsMapper;
import com.example.application.portfolio.models.CasDTO;
import com.example.application.portfolio.models.UserFolioDTO;
import com.example.application.portfolio.models.UserSchemeDTO;
import com.example.application.portfolio.models.UserTransactionDTO;
import com.example.application.portfolio.models.response.UploadFileResponse;
import com.example.application.shared.LocalDateUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final UserSchemeDetailServiceImpl userSchemeDetailService;

    public UserDetailService(
            PortfolioServiceHelper portfolioServiceHelper,
            CasDetailsMapper casDetailsMapper,
            UserCASDetailsService userCASDetailsService,
            InvestorInfoService investorInfoService,
            UserTransactionDetailsService userTransactionDetailsService,
            UserFolioDetailService userFolioDetailService,
            UserSchemeDetailServiceImpl userSchemeDetailService) {
        this.portfolioServiceHelper = portfolioServiceHelper;
        this.casDetailsMapper = casDetailsMapper;
        this.userCASDetailsService = userCASDetailsService;
        this.investorInfoService = investorInfoService;
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userFolioDetailService = userFolioDetailService;
        this.userSchemeDetailService = userSchemeDetailService;
    }

    public UploadFileResponse upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = parseCasDTO(multipartFile);
        boolean existingUser = validateCasDTO(casDTO);
        UploadFileResponse response;
        if (existingUser) {
            response = processExistingUser(casDTO);
        } else {
            response = processNewUser(casDTO);
        }
        return response;
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
        List<UserFolioDTO> userFolioDTOList = casDTO.folios();
        processNewFolios(userFolioDTOList, userCASDetails, newFolios, newSchemes, newTransactions);

        processNewSchemesAndTransactions(
                userFolioDTOList,
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

            // Grouping by ISIN for userSchemaTransactionMap
            Map<String, List<UserTransactionDTO>> userSchemaTransactionMap = groupTransactionBySchemes(folios);

            // Grouping by ISIN for userSchemaTransactionMapFromDB
            List<UserSchemeDetails> existingUserSchemeDetailsList = userCASDetails.getFolios().stream()
                    .map(UserFolioDetails::getSchemes)
                    .flatMap(List::stream)
                    .toList();
            Map<String, List<UserTransactionDetails>> userSchemaTransactionMapFromDB =
                    groupExistingTransactionsByIsin(existingUserSchemeDetailsList);

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
        userSchemaTransactionMap.forEach((isinFromRequest, requestTransactions) -> {
            List<UserTransactionDetails> dbTransactions =
                    userSchemaTransactionMapFromDB.getOrDefault(isinFromRequest, List.of());
            if (requestTransactions.size() != dbTransactions.size()) {
                // New transactions added to scheme
                List<LocalDate> transactionDateListDB = dbTransactions.stream()
                        .map(UserTransactionDetails::getTransactionDate)
                        .toList();
                requestTransactions.forEach(userTransactionDTO -> {
                    LocalDate newTransactionDate = userTransactionDTO.date();
                    if (!transactionDateListDB.contains(newTransactionDate)) {
                        log.info(
                                "New transaction on date: {} created for isin {} that is not present in the database",
                                newTransactionDate,
                                isinFromRequest);
                        UserTransactionDetails userTransactionDetailsEntity =
                                casDetailsMapper.transactionDTOToTransactionEntity(userTransactionDTO);
                        existingUserSchemeDetailsList.forEach(userSchemeDetailsEntity -> {
                            if (isinFromRequest.equals(userSchemeDetailsEntity.getIsin())) {
                                userSchemeDetailsEntity.addTransaction(userTransactionDetailsEntity);
                                newTransactions.incrementAndGet();
                            }
                        });
                    }
                });
            }
        });
    }

    private Map<String, List<UserTransactionDetails>> groupExistingTransactionsByIsin(
            List<UserSchemeDetails> existingUserSchemeDetailsList) {
        return existingUserSchemeDetailsList.stream()
                .collect(groupingBy(
                        UserSchemeDetails::getIsin,
                        flatMapping(
                                userSchemeDetailsEntity -> userSchemeDetailsEntity.getTransactions().stream(),
                                toList())));
    }

    private Map<String, List<UserTransactionDTO>> groupTransactionBySchemes(List<UserFolioDTO> folios) {
        return folios.stream()
                .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                .collect(groupingBy(
                        UserSchemeDTO::isin, flatMapping(schemeDTO -> schemeDTO.transactions().stream(), toList())));
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
            if (requestSchemes.size() != existingSchemesFromDB.size()) {
                // New schemes added to folio
                List<String> isInListDB = existingSchemesFromDB.stream()
                        .map(UserSchemeDetails::getIsin)
                        .toList();
                requestSchemes.forEach(userSchemeDTO -> {
                    if (!isInListDB.contains(userSchemeDTO.isin())) {
                        log.info(
                                "New ISIN: {} created for folio : {} that is not present in the database",
                                userSchemeDTO.isin(),
                                folioFromRequest);
                        UserSchemeDetails userSchemeDetailsEntity =
                                casDetailsMapper.schemeDTOToSchemeEntity(userSchemeDTO, newTransactions);

                        userCASDetails.getFolios().forEach(userFolioDetailsEntity -> {
                            if (folioFromRequest.equals(userFolioDetailsEntity.getFolio())) {
                                userFolioDetailsEntity.addScheme(userSchemeDetailsEntity);
                                newSchemes.getAndIncrement();
                            }
                        });
                    }
                });
            }
        });
    }

    Map<String, List<UserSchemeDetails>> groupExistingSchemes(
            List<UserFolioDetails> existingUserFolioDetailsEntityList) {

        return existingUserFolioDetailsEntityList.stream()
                .collect(groupingBy(
                        UserFolioDetails::getFolio,
                        flatMapping(userFolioDetailsEntity -> userFolioDetailsEntity.getSchemes().stream(), toList())));
    }

    private Map<String, List<UserSchemeDTO>> groupSchemesByFolio(List<UserFolioDTO> folios) {
        return folios.stream()
                .collect(groupingBy(UserFolioDTO::folio, flatMapping(folio -> folio.schemes().stream(), toList())));
    }

    private void processNewFolios(
            List<UserFolioDTO> folios,
            UserCASDetails userCASDetails,
            AtomicInteger newFolios,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions) {
        int folioSizeFromDB = userCASDetails.getFolios().size();
        int folioSizeFromReq = folios.size();
        if (folioSizeFromReq != folioSizeFromDB) {
            List<String> foliosListFromDB = userCASDetails.getFolios().stream()
                    .map(UserFolioDetails::getFolio)
                    .toList();
            List<String> foliosFromReq =
                    folios.stream().map(UserFolioDTO::folio).toList();
            List<String> newlyAddedFolios = findNewElements(foliosFromReq, foliosListFromDB);
            newlyAddedFolios.forEach(s -> {
                Optional<UserFolioDTO> matchingDTO =
                        folios.stream().filter(dto -> dto.folio().equals(s)).findFirst();
                userCASDetails.addFolioEntity(casDetailsMapper.mapUserFolioDTOToUserFolioDetails(
                        matchingDTO.get(), newSchemes, newTransactions));
                newFolios.getAndIncrement();
            });
        } else {
            log.info("No new folios are added");
        }
    }

    public <T> List<T> findNewElements(List<T> list1, List<T> list2) {
        List<T> extraElements = new ArrayList<>();

        for (T element : list1) {
            if (!list2.contains(element)) {
                extraElements.add(element);
            }
        }
        return extraElements;
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

        List<UserFolioDTO> folios = casDTO.folios();
        if (CollectionUtils.isEmpty(folios)) {
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

    private UserCASDetails getUserCASDetails(UserCASDetails userCASDetails) {
        UserCASDetails savedCasDetailsEntity = userCASDetailsService.saveEntity(userCASDetails);
        CompletableFuture.runAsync(() -> userFolioDetailService.setPANIfNotSet(savedCasDetailsEntity.getId()));
        CompletableFuture.runAsync(userSchemeDetailService::setAMFIIfNull);
        return savedCasDetailsEntity;
    }

    private CasDTO parseCasDTO(MultipartFile multipartFile) throws IOException {
        return portfolioServiceHelper.readValue(multipartFile.getBytes(), CasDTO.class);
    }
}
