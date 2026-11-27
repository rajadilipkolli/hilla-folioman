package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.UserSchemeDetailService;
import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioDetailsDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import com.app.folioman.portfolio.rest.dtos.UserFolioDTO;
import com.app.folioman.portfolio.rest.dtos.UserSchemeDTO;
import com.app.folioman.portfolio.rest.dtos.UserTransactionDTO;
import com.app.folioman.shared.LocalDateUtility;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jobrunr.scheduling.JobScheduler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

@Service
@Transactional(readOnly = true)
class UserDetailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailService.class);

    private final PortfolioServiceHelper portfolioServiceHelper;
    private final CasDetailsMapper casDetailsMapper;
    private final UserCASDetailsService userCASDetailsService;
    private final InvestorInfoService investorInfoService;
    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserFolioDetailService userFolioDetailService;
    private final UserSchemeDetailService userSchemeDetailService;
    private final PortfolioValueUpdateService portfolioValueUpdateService;
    private final @Nullable ObjectProvider<JobScheduler> jobSchedulerProvider;
    private final @Nullable PortfolioPostUploadService portfolioPostUploadService;
    private final @Nullable JdbcTemplate jdbcTemplate;
    private final @Nullable UserPortfolioValueRepository userPortfolioValueRepository;

    UserDetailService(
            PortfolioServiceHelper portfolioServiceHelper,
            CasDetailsMapper casDetailsMapper,
            UserCASDetailsService userCASDetailsService,
            InvestorInfoService investorInfoService,
            UserTransactionDetailsService userTransactionDetailsService,
            UserFolioDetailService userFolioDetailService,
            UserSchemeDetailService userSchemeDetailService,
            PortfolioValueUpdateService portfolioValueUpdateService) {
        this(
                portfolioServiceHelper,
                casDetailsMapper,
                userCASDetailsService,
                investorInfoService,
                userTransactionDetailsService,
                userFolioDetailService,
                userSchemeDetailService,
                portfolioValueUpdateService,
                null,
                null,
                null,
                null);
    }

    @Autowired
    UserDetailService(
            PortfolioServiceHelper portfolioServiceHelper,
            CasDetailsMapper casDetailsMapper,
            UserCASDetailsService userCASDetailsService,
            InvestorInfoService investorInfoService,
            UserTransactionDetailsService userTransactionDetailsService,
            UserFolioDetailService userFolioDetailService,
            UserSchemeDetailService userSchemeDetailService,
            PortfolioValueUpdateService portfolioValueUpdateService,
            @Nullable ObjectProvider<JobScheduler> jobSchedulerProvider,
            @Nullable PortfolioPostUploadService portfolioPostUploadService,
            @Nullable JdbcTemplate jdbcTemplate,
            @Nullable UserPortfolioValueRepository userPortfolioValueRepository) {
        this.portfolioServiceHelper = portfolioServiceHelper;
        this.casDetailsMapper = casDetailsMapper;
        this.userCASDetailsService = userCASDetailsService;
        this.investorInfoService = investorInfoService;
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userFolioDetailService = userFolioDetailService;
        this.userSchemeDetailService = userSchemeDetailService;
        this.portfolioValueUpdateService = portfolioValueUpdateService;
        this.jobSchedulerProvider = jobSchedulerProvider;
        this.portfolioPostUploadService = portfolioPostUploadService;
        this.jdbcTemplate = jdbcTemplate;
        this.userPortfolioValueRepository = userPortfolioValueRepository;
    }

    @Transactional
    public UploadFileResponse upload(byte[] casJsonBytes) throws IOException {
        CasDTO casDTO = portfolioServiceHelper.readValue(casJsonBytes, CasDTO.class);
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
    @Transactional
    public UploadFileResponse uploadFromDto(CasDTO casDTO) {
        LOGGER.info("Processing CasDTO from converted source");
        boolean existingUser = validateCasDTO(casDTO);
        return existingUser ? processExistingUser(casDTO) : processNewUser(casDTO);
    }

    private UploadFileResponse processExistingUser(CasDTO casDTO) {
        AtomicInteger newTransactions = new AtomicInteger();
        AtomicInteger newFolios = new AtomicInteger();
        AtomicInteger newSchemes = new AtomicInteger();
        LocalDate from = LocalDateUtility.parse(casDTO.statementPeriod().from());
        LocalDate to = LocalDateUtility.parse(casDTO.statementPeriod().myto());

        String canonicalEmail =
                canonicalizeEmail(Objects.requireNonNull(casDTO.investorInfo().email()));
        String name = Objects.requireNonNull(casDTO.investorInfo().name());

        long userTransactionFromReqCount = portfolioServiceHelper.countTransactionsByUserFolioDTOList(casDTO.folios());
        Long userTransactionFromDBCount =
                userTransactionDetailsService.findAllTransactionsByEmailNameAndPeriod(name, canonicalEmail, from, to);

        UserCasDetailsEntity userCasDetailsEntity = userCASDetailsService
                .findByInvestorEmailAndName(canonicalEmail, name)
                .orElseThrow(() -> new IllegalStateException("User should exist"));

        if (userTransactionFromReqCount == userTransactionFromDBCount) {
            LOGGER.info("No new transactions are added");
        } else {
            importNewTransaction(
                    casDTO,
                    userCasDetailsEntity,
                    newFolios,
                    newSchemes,
                    newTransactions,
                    userTransactionFromReqCount,
                    userTransactionFromDBCount);
        }

        // For existing users, new folios and schemes are cascaded automatically on flush.
        // New transactions were explicitly saved in processNewTransactions.
        // We do not call getUserCASDetails to avoid a massive merge on the entire tree.

        userFolioDetailService.setPANIfNotSet(userCasDetailsEntity.getId());
        boolean hasChanges = newFolios.get() > 0 || newSchemes.get() > 0 || newTransactions.get() > 0;
        boolean valuationsMissing = userPortfolioValueRepository != null
                && userPortfolioValueRepository
                        .getLatestPortfolioValueByCasId(userCasDetailsEntity.getId())
                        .isEmpty();
        if (hasChanges || valuationsMissing) {
            schedulePostUploadProcessing(userCasDetailsEntity.getId());
        }

        return new UploadFileResponse(
                newFolios.get(), newSchemes.get(), newTransactions.get(), userCasDetailsEntity.getId());
    }

    private void importNewTransaction(
            CasDTO casDTO,
            UserCasDetailsEntity userCasDetailsEntity,
            AtomicInteger newFolios,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions,
            long userTransactionFromReqCount,
            Long userTransactionFromDBCount) {

        // Optimize folio and scheme processing by processing only if counts don't match
        processNewFolios(casDTO.folios(), userCasDetailsEntity, newFolios, newSchemes, newTransactions);
        processNewSchemesAndTransactions(
                casDTO.folios(),
                userCasDetailsEntity,
                newSchemes,
                newTransactions,
                userTransactionFromReqCount,
                userTransactionFromDBCount);
    }

    private void addNewTransactions(
            List<UserFolioDTO> folios,
            UserCasDetailsEntity userCasDetails,
            AtomicInteger newTransactions,
            long userTransactionFromReqCount,
            Long userTransactionFromDBCount) {

        // Check if all new transactions are added as part of adding schemes
        if (userTransactionFromReqCount == (userTransactionFromDBCount + newTransactions.get())) {
            LOGGER.info("All new transactions are added as part of adding schemes, hence skipping");
        } else {
            // New transactions are added

            // Grouping by rtaCode for userSchemaTransactionMap
            Map<String, List<UserTransactionDTO>> userSchemaTransactionMap = groupTransactionBySchemes(folios);

            // Grouping by rtaCode for userSchemaTransactionMapFromDB
            List<UserSchemeDetailsEntity> existingUserSchemeDetailsList = userCasDetails.getFolios().stream()
                    .map(UserFolioDetailsEntity::getSchemes)
                    .flatMap(List::stream)
                    .toList();

            Map<String, List<UserTransactionDetailsEntity>> userSchemaTransactionMapFromDB =
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
            Map<String, List<UserTransactionDetailsEntity>> userSchemaTransactionMapFromDB,
            List<UserSchemeDetailsEntity> existingUserSchemeDetailsList) {

        // Create a map to collect new transactions by their scheme
        Map<UserSchemeDetailsEntity, List<UserTransactionDetailsEntity>> transactionsByScheme = new HashMap<>();

        userSchemaTransactionMap.forEach((folioSchemeKeyFromRequest, requestTransactions) -> {
            List<UserTransactionDetailsEntity> dbTransactions =
                    userSchemaTransactionMapFromDB.getOrDefault(folioSchemeKeyFromRequest, List.of());

            if (requestTransactions.size() != dbTransactions.size()) {
                // For efficient lookup, create a frequency map of existing transactions
                Map<TransactionKey, Long> dbTransactionFrequencyMap = dbTransactions.stream()
                        .collect(Collectors.groupingBy(TransactionKey::from, Collectors.counting()));

                // Find matching scheme once outside the inner loop
                UserSchemeDetailsEntity matchingScheme = existingUserSchemeDetailsList.stream()
                        .filter(scheme -> folioSchemeKeyFromRequest.equals(folioSchemeKey(
                                scheme.getUserFolioDetails() != null
                                        ? scheme.getUserFolioDetails().getFolio()
                                        : "",
                                scheme.getRtaCode())))
                        .findFirst()
                        .orElse(null);

                if (matchingScheme != null) {
                    // Process all transactions for this scheme focusing on finding the new ones
                    List<UserTransactionDetailsEntity> newTransactionsForScheme = new ArrayList<>();

                    for (UserTransactionDTO dto : requestTransactions) {
                        TransactionKey key = TransactionKey.from(dto);
                        long dbCount = dbTransactionFrequencyMap.getOrDefault(key, 0L);

                        if (dbCount > 0) {
                            // If it exists in DB, "match" it and decrement count
                            dbTransactionFrequencyMap.put(key, dbCount - 1);
                        } else {
                            // If it doesn't exist or we've run out of matches, it's new
                            LOGGER.info(
                                    "New transaction on date: {} with description: {} created for {} that is not present in the database",
                                    dto.date(),
                                    dto.description(),
                                    folioSchemeKeyFromRequest);
                            UserTransactionDetailsEntity entity =
                                    casDetailsMapper.transactionDTOToTransactionEntity(dto);
                            entity.setUserSchemeDetails(matchingScheme);
                            newTransactions.incrementAndGet();
                            newTransactionsForScheme.add(entity);
                        }
                    }

                    if (!newTransactionsForScheme.isEmpty()) {
                        transactionsByScheme.put(matchingScheme, newTransactionsForScheme);
                    }
                }
            }
        });

        // Process all transactions in a single batch where possible
        if (!transactionsByScheme.isEmpty()) {
            List<UserTransactionDetailsEntity> allNewTransactions = new ArrayList<>();
            transactionsByScheme.forEach((scheme, transactions) -> allNewTransactions.addAll(transactions));

            // Save all new transactions in a single batch operation
            if (!allNewTransactions.isEmpty()) {
                LOGGER.info("Batch saving {} new transactions", allNewTransactions.size());
                List<UserTransactionDetailsEntity> savedTransactions =
                        userTransactionDetailsService.saveTransactions(allNewTransactions);

                // Add newly saved transactions back to their in-memory scheme collections
                for (UserTransactionDetailsEntity savedTxn : savedTransactions) {
                    UserSchemeDetailsEntity scheme = savedTxn.getUserSchemeDetails();
                    if (scheme != null && scheme.getTransactions() != null) {
                        scheme.getTransactions().add(savedTxn);
                    }
                }
            }
        }
    }

    private Map<String, List<UserTransactionDetailsEntity>> groupExistingTransactionsByRtaCode(
            List<UserSchemeDetailsEntity> existingUserSchemeDetailsList) {
        return existingUserSchemeDetailsList.stream()
                .collect(Collectors.groupingBy(
                        scheme -> folioSchemeKey(
                                scheme.getUserFolioDetails() != null
                                        ? scheme.getUserFolioDetails().getFolio()
                                        : "",
                                scheme.getRtaCode()),
                        Collectors.flatMapping(
                                userSchemeDetailsEntity -> userSchemeDetailsEntity.getTransactions().stream(),
                                Collectors.toList())));
    }

    private Map<String, List<UserTransactionDTO>> groupTransactionBySchemes(List<UserFolioDTO> folios) {
        return folios.stream()
                .flatMap(userFolioDTO -> userFolioDTO.schemes().stream()
                        .map(schemeDTO -> Map.entry(
                                folioSchemeKey(userFolioDTO.folio(), schemeDTO.rtaCode()), schemeDTO.transactions())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.flatMapping(entry -> entry.getValue().stream(), Collectors.toList())));
    }

    private String folioSchemeKey(@Nullable String folio, @Nullable String rtaCode) {
        return Objects.toString(folio, "") + "::" + Objects.toString(rtaCode, "");
    }

    private String canonicalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void processNewSchemesAndTransactions(
            List<UserFolioDTO> folios,
            UserCasDetailsEntity userCasDetailsEntity,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions,
            long userTransactionFromReqCount,
            Long userTransactionFromDBCount) {
        // Check if all new transactions are added as part of adding folios
        if (userTransactionFromReqCount == (userTransactionFromDBCount + newTransactions.get())) {
            LOGGER.info("All new transactions are added as part of adding folios, hence skipping");
        } else {
            // New schemes or transactions are added

            // Grouping by folio for requestedFolioSchemesMap
            Map<String, List<UserSchemeDTO>> requestedFolioSchemesMap = groupSchemesByFolio(folios);
            Map<String, List<UserSchemeDetailsEntity>> existingFolioSchemesMap =
                    groupExistingSchemes(userCasDetailsEntity.getFolios());

            // Update schemes in existing folios
            updateExistingFoliosWithNewSchemes(
                    requestedFolioSchemesMap,
                    existingFolioSchemesMap,
                    userCasDetailsEntity,
                    newSchemes,
                    newTransactions);

            // add new transactions in existing schemes
            addNewTransactions(
                    folios,
                    userCasDetailsEntity,
                    newTransactions,
                    userTransactionFromReqCount,
                    userTransactionFromDBCount);
        }
    }

    private void updateExistingFoliosWithNewSchemes(
            Map<String, List<UserSchemeDTO>> requestedFolioSchemesMap,
            Map<String, List<UserSchemeDetailsEntity>> existingFolioSchemesMap,
            UserCasDetailsEntity userCasDetails,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions) {

        Map<UserFolioDetailsEntity, List<UserSchemeDetailsEntity>> newSchemesByFolio = new HashMap<>();

        requestedFolioSchemesMap.forEach((folioFromRequest, requestSchemes) -> {
            List<UserSchemeDetailsEntity> existingSchemesFromDB =
                    existingFolioSchemesMap.getOrDefault(folioFromRequest, new ArrayList<>());

            Set<String> rtaCodeSet = existingSchemesFromDB.stream()
                    .map(UserSchemeDetailsEntity::getRtaCode)
                    .collect(Collectors.toSet());

            // Find the matching folio entity
            UserFolioDetailsEntity matchingFolio = userCasDetails.getFolios().stream()
                    .filter(folio -> folioFromRequest.equals(folio.getFolio()))
                    .findFirst()
                    .orElse(null);

            if (matchingFolio != null) {
                // Collect all new schemes for this folio
                List<UserSchemeDetailsEntity> newSchemesForFolio = requestSchemes.stream()
                        .filter(scheme -> !rtaCodeSet.contains(scheme.rtaCode()))
                        .map(userSchemeDTO -> {
                            LOGGER.info(
                                    "New RTACode: {} created for folio : {} that is not present in the database",
                                    userSchemeDTO.rtaCode(),
                                    folioFromRequest);
                            UserSchemeDetailsEntity entity =
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

    private Map<String, List<UserSchemeDetailsEntity>> groupExistingSchemes(
            List<UserFolioDetailsEntity> existingUserFolioDetailsEntityList) {
        return existingUserFolioDetailsEntityList.stream()
                .collect(Collectors.groupingBy(
                        UserFolioDetailsEntity::getFolio,
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
            UserCasDetailsEntity userCasDetails,
            AtomicInteger newFolios,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions) {

        Set<String> foliosListFromDB = userCasDetails.getFolios().stream()
                .map(UserFolioDetailsEntity::getFolio)
                .collect(Collectors.toSet());
        Set<String> foliosFromReq = folios.stream().map(UserFolioDTO::folio).collect(Collectors.toSet());

        foliosFromReq.stream()
                .filter(folio -> !foliosListFromDB.contains(folio))
                .forEach(newFolio -> {
                    Optional<UserFolioDTO> matchingDTO = folios.stream()
                            .filter(dto -> dto.folio().equals(newFolio))
                            .findFirst();
                    matchingDTO.ifPresent(dto -> {
                        userCasDetails.addFolioEntity(
                                casDetailsMapper.mapUserFolioDTOToUserFolioDetails(dto, newSchemes, newTransactions));
                        newFolios.incrementAndGet();
                    });
                });
    }

    private UserCasDetailsEntity getUserCASDetails(UserCasDetailsEntity userCasDetailsEntity) {
        // Save entity in a single transaction
        UserCasDetailsEntity savedCasDetailsEntity = userCASDetailsService.saveEntity(userCasDetailsEntity);

        // Run critical post-processing tasks synchronously
        userFolioDetailService.setPANIfNotSet(savedCasDetailsEntity.getId());

        // Run non-critical post-processing tasks durably via JobRunr
        schedulePostUploadProcessing(savedCasDetailsEntity.getId());

        return savedCasDetailsEntity;
    }

    private void schedulePostUploadProcessing(Long casId) {
        Runnable trigger = () -> {
            JobScheduler scheduler = jobSchedulerProvider != null ? jobSchedulerProvider.getIfAvailable() : null;
            if (scheduler != null && portfolioPostUploadService != null) {
                scheduler.enqueue(() -> portfolioPostUploadService.processCasPostUpload(casId));
            } else {
                CompletableFuture.runAsync(() -> userSchemeDetailService.setUserSchemeAMFIIfNull(casId))
                        .thenRun(() -> portfolioValueUpdateService.updatePortfolioValue(casId));
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    trigger.run();
                }
            });
        } else {
            trigger.run();
        }
    }

    private boolean validateCasDTO(CasDTO casDTO) {
        String rawEmail = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();
        if (rawEmail == null || rawEmail.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Email or Name invalid!");
        }
        if (CollectionUtils.isEmpty(casDTO.folios())) {
            throw new IllegalArgumentException("No folios found!");
        }
        String canonicalEmail = canonicalizeEmail(rawEmail);
        if (jdbcTemplate != null && TransactionSynchronizationManager.isSynchronizationActive()) {
            jdbcTemplate.queryForObject(
                    "SELECT pg_advisory_xact_lock(hashtext(?))", Object.class, canonicalEmail + ":" + name);
        }
        return investorInfoService.existsByEmailAndName(canonicalEmail, name);
    }

    private UploadFileResponse processNewUser(CasDTO casDTO) {
        AtomicInteger newTransactions = new AtomicInteger();
        AtomicInteger newFolios = new AtomicInteger();
        AtomicInteger newSchemes = new AtomicInteger();
        UserCasDetailsEntity userCasDetailsEntity =
                casDetailsMapper.convert(casDTO, newFolios, newSchemes, newTransactions);

        // Ensure investor information matches the CAS DTO
        if (userCasDetailsEntity.getInvestorInfoEntity() == null) {
            userCasDetailsEntity.setInvestorInfoEntity(
                    casDetailsMapper.mapInvestorInfoDTOToInvestorInfoEntity(casDTO.investorInfo()));
        }
        if (userCasDetailsEntity.getInvestorInfoEntity() != null
                && userCasDetailsEntity.getInvestorInfoEntity().getEmail() != null) {
            userCasDetailsEntity
                    .getInvestorInfoEntity()
                    .setEmail(canonicalizeEmail(
                            userCasDetailsEntity.getInvestorInfoEntity().getEmail()));
        }

        UserCasDetailsEntity savedCasDetailsEntity = getUserCASDetails(userCasDetailsEntity);
        return new UploadFileResponse(
                newFolios.get(), newSchemes.get(), newTransactions.get(), savedCasDetailsEntity.getId());
    }

    private record TransactionKey(
            @Nullable LocalDate date,
            @Nullable String description,
            BigDecimal amount,
            TransactionType type,
            @Nullable Double units,
            @Nullable Double balance) {

        static TransactionKey from(UserTransactionDTO dto) {
            return new TransactionKey(
                    dto.date(),
                    dto.description(),
                    BigDecimal.valueOf(dto.amount() != null ? dto.amount() : 0.0)
                            .setScale(4, RoundingMode.HALF_UP),
                    TransactionType.valueOf(dto.type().name()),
                    dto.units(),
                    dto.balance());
        }

        /** Creates a comparison key from a persisted transaction. */
        static TransactionKey from(UserTransactionDetailsEntity entity) {
            return new TransactionKey(
                    entity.getTransactionDate(),
                    entity.getDescription(),
                    entity.getAmount() != null
                            ? entity.getAmount().setScale(4, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                    entity.getType(),
                    entity.getUnits() != null ? entity.getUnits().doubleValue() : null,
                    entity.getBalance() != null ? entity.getBalance().doubleValue() : null);
        }
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, String email, @Nullable LocalDate evaluationDate) {
        List<PortfolioDetailsDTO> portfolioDetailsDTOList = portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(
                panNumber, email, LocalDateUtility.getAdjustedDateOrDefault(evaluationDate));
        BigDecimal totalPortfolioValue = portfolioDetailsDTOList.stream()
                .map(PortfolioDetailsDTO::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(totalPortfolioValue.setScale(4, RoundingMode.HALF_UP), portfolioDetailsDTOList);
    }
}
