package com.app.folioman.portfolio.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import com.app.folioman.portfolio.entities.*;
import com.app.folioman.portfolio.models.request.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CasDetailsMapperTest {

    private final CasDetailsMapper mapper = Mappers.getMapper(CasDetailsMapper.class);

    private AtomicInteger newFolios;
    private AtomicInteger newSchemes;
    private AtomicInteger newTransactions;

    @BeforeEach
    void setUp() {
        newFolios = new AtomicInteger(0);
        newSchemes = new AtomicInteger(0);
        newTransactions = new AtomicInteger(0);
    }

    @Test
    void convert() {

        // use a real DTO here to exercise the mapper, but verify via the spy
        CasDTO local = new CasDTO(null, FileTypeEnum.KARVY.name(), CasTypeEnum.SUMMARY.name(), null, new ArrayList<>());
        UserCASDetails result = mapper.convert(local, newFolios, newSchemes, newTransactions);

        assertThat(result).isNotNull();
        // basic property mappings
        assertThat(result.getFileTypeEnum()).isEqualTo(FileTypeEnum.KARVY);
        assertThat(result.getCasTypeEnum()).isEqualTo(CasTypeEnum.SUMMARY);
    }

    @Test
    void convertWithNullInput() {
        // MapStruct implementation triggers @AfterMapping which assumes non-null input
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> mapper.convert(null, newFolios, newSchemes, newTransactions));
    }

    @Test
    void mapUserFolioDTOToUserFolioDetails() {

        // use a real DTO and map to entity to validate mapping
        UserFolioDTO localFolio = new UserFolioDTO("124567", "JioAMC", "ABCDE1234F", "OK", "OK", new ArrayList<>());
        UserFolioDetails result = mapper.mapUserFolioDTOToUserFolioDetails(localFolio, newSchemes, newTransactions);

        assertThat(result).isNotNull();
        assertThat(result.getFolio()).isEqualTo("124567");
    }

    @Test
    void mapUserFolioDTOToUserFolioDetailsWithNullInput() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> mapper.mapUserFolioDTOToUserFolioDetails(null, newSchemes, newTransactions));
    }

    @Test
    void schemeDTOToSchemeEntity() {

        UserSchemeDTO localScheme = new UserSchemeDTO(
                "SCHEME",
                "ISIN",
                123L,
                "advisor",
                "RTA",
                "EQUITY",
                "CAMS",
                "0.0",
                "close",
                "closeCalculated",
                null,
                new ArrayList<>());
        UserSchemeDetails result = mapper.schemeDTOToSchemeEntity(localScheme, newTransactions);

        assertThat(result).isNotNull();
        // no id mapping expected
        assertThat(result.getId()).isNull();
    }

    @Test
    void schemeDTOToSchemeEntityWithNullInput() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> mapper.schemeDTOToSchemeEntity(null, newTransactions));
    }

    @Test
    void transactionDTOToTransactionEntity() {

        UserTransactionDTO localTxn = new UserTransactionDTO(
                LocalDate.parse("2020-01-01"),
                "BUY",
                100.0d,
                1.0d,
                100.0d,
                100.0d,
                com.app.folioman.portfolio.models.request.TransactionType.PURCHASE,
                null);
        UserTransactionDetails result = mapper.transactionDTOToTransactionEntity(localTxn);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(com.app.folioman.portfolio.models.request.TransactionType.PURCHASE);
    }

    @Test
    void transactionDTOToTransactionEntityWithNullInput() {
        UserTransactionDetails result = mapper.transactionDTOToTransactionEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void addFolioEntityToCaseDetails() {
        // use a real CasDTO with a single folio to exercise the @AfterMapping hook
        UserFolioDTO folioDTO = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", new ArrayList<>());
        CasDTO localCas =
                new CasDTO(null, FileTypeEnum.CAMS.name(), CasTypeEnum.DETAILED.name(), null, List.of(folioDTO));
        com.app.folioman.portfolio.entities.UserCASDetails userCAS =
                mapper.convert(localCas, newFolios, newSchemes, newTransactions);

        assertThat(userCAS).isNotNull();
        assertThat(newFolios.get()).isOne();
        assertThat(userCAS.getFolios()).hasSize(1);
    }

    @Test
    void addFolioEntityToCaseDetailsWithEmptyFolios() {
        CasDTO emptyCas = new CasDTO(null, FileTypeEnum.CAMS.name(), CasTypeEnum.DETAILED.name(), null, List.of());
        com.app.folioman.portfolio.entities.UserCASDetails userCAS =
                mapper.convert(emptyCas, newFolios, newSchemes, newTransactions);

        assertThat(userCAS).isNotNull();
        assertThat(newFolios.get()).isZero();
    }

    @Test
    void addFolioEntityToCaseDetailsWithMultipleFolios() {
        UserFolioDTO f1 = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", new ArrayList<>());
        UserFolioDTO f2 = new UserFolioDTO("F2", "AMC", "PAN", "KYC", "PANKYC", new ArrayList<>());
        CasDTO localCas2 =
                new CasDTO(null, FileTypeEnum.CAMS.name(), CasTypeEnum.DETAILED.name(), null, List.of(f1, f2));
        com.app.folioman.portfolio.entities.UserCASDetails userCAS2 =
                mapper.convert(localCas2, newFolios, newSchemes, newTransactions);

        assertThat(userCAS2).isNotNull();
        assertThat(newFolios.get()).isEqualTo(2);
        assertThat(userCAS2.getFolios()).hasSize(2);
    }

    @Test
    void addSchemaEntityToFolioEntity() {
        UserSchemeDTO s1 = new UserSchemeDTO(
                "S1",
                "ISIN",
                111L,
                "advisor",
                "RTA",
                "EQUITY",
                "CAMS",
                "0.0",
                "close",
                "closeCalculated",
                null,
                new ArrayList<>());
        UserFolioDTO folioWithScheme = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", List.of(s1));
        com.app.folioman.portfolio.entities.UserFolioDetails folioEntity =
                mapper.mapUserFolioDTOToUserFolioDetails(folioWithScheme, newSchemes, newTransactions);

        assertThat(folioEntity).isNotNull();
        assertThat(newSchemes.get()).isOne();
        assertThat(folioEntity.getSchemes()).hasSize(1);
    }

    @Test
    void addSchemaEntityToFolioEntityWithEmptySchemes() {
        UserFolioDTO folioWithoutSchemes = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", List.of());
        com.app.folioman.portfolio.entities.UserFolioDetails folioEntity2 =
                mapper.mapUserFolioDTOToUserFolioDetails(folioWithoutSchemes, newSchemes, newTransactions);

        assertThat(folioEntity2).isNotNull();
        assertThat(newSchemes.get()).isZero();
    }

    @Test
    void addSchemaEntityToFolioEntityWithMultipleSchemes() {
        UserSchemeDTO s1 = new UserSchemeDTO(
                "S1",
                "ISIN",
                111L,
                "advisor",
                "RTA",
                "EQUITY",
                "CAMS",
                "0.0",
                "close",
                "closeCalculated",
                null,
                new ArrayList<>());
        UserSchemeDTO s2 = new UserSchemeDTO(
                "S2",
                "ISIN2",
                222L,
                "advisor2",
                "RTA2",
                "EQUITY",
                "CAMS",
                "0.0",
                "close",
                "closeCalculated",
                null,
                new ArrayList<>());
        UserFolioDTO folioWithTwo = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", List.of(s1, s2));
        com.app.folioman.portfolio.entities.UserFolioDetails folioEntity3 =
                mapper.mapUserFolioDTOToUserFolioDetails(folioWithTwo, newSchemes, newTransactions);

        assertThat(folioEntity3).isNotNull();
        assertThat(newSchemes.get()).isEqualTo(2);
        assertThat(folioEntity3.getSchemes()).hasSize(2);
    }

    @Test
    void addTransactionEntityToSchemeEntity() {
        UserTransactionDTO t1 = new UserTransactionDTO(
                java.time.LocalDate.parse("2020-01-01"),
                "BUY",
                100.0d,
                1.0d,
                100.0d,
                100.0d,
                TransactionType.PURCHASE,
                null);
        UserSchemeDTO schemeWithTxn = new UserSchemeDTO(
                "S1",
                "ISIN",
                555L,
                "advisor",
                "RTA",
                "EQUITY",
                "CAMS",
                "0.0",
                "close",
                "closeCalculated",
                null,
                List.of(t1));
        com.app.folioman.portfolio.entities.UserSchemeDetails schemeEntity =
                mapper.schemeDTOToSchemeEntity(schemeWithTxn, newTransactions);

        assertThat(schemeEntity).isNotNull();
        assertThat(newTransactions.get()).isOne();
        assertThat(schemeEntity.getTransactions()).hasSize(1);
    }

    @Test
    void addTransactionEntityToSchemeEntityWithEmptyTransactions() {
        UserSchemeDTO schemeWithoutTx = new UserSchemeDTO(
                "S1",
                "ISIN",
                666L,
                "advisor",
                "RTA",
                "EQUITY",
                "CAMS",
                "0.0",
                "close",
                "closeCalculated",
                null,
                List.of());
        com.app.folioman.portfolio.entities.UserSchemeDetails schemeEntity2 =
                mapper.schemeDTOToSchemeEntity(schemeWithoutTx, newTransactions);

        assertThat(schemeEntity2).isNotNull();
        assertThat(newTransactions.get()).isZero();
    }

    @Test
    void addTransactionEntityToSchemeEntityWithMultipleTransactions() {
        UserTransactionDTO t1 = new UserTransactionDTO(
                java.time.LocalDate.parse("2020-01-01"),
                "BUY",
                100.0d,
                1.0d,
                100.0d,
                100.0d,
                TransactionType.PURCHASE,
                null);
        UserTransactionDTO t2 = new UserTransactionDTO(
                java.time.LocalDate.parse("2020-01-02"),
                "SELL",
                50.0d,
                0.5d,
                100.0d,
                50.0d,
                TransactionType.REDEMPTION,
                null);
        UserSchemeDTO schemeWithTwo = new UserSchemeDTO(
                "S1",
                "ISIN",
                777L,
                "advisor",
                "RTA",
                "EQUITY",
                "CAMS",
                "0.0",
                "close",
                "closeCalculated",
                null,
                List.of(t1, t2));
        com.app.folioman.portfolio.entities.UserSchemeDetails schemeEntity3 =
                mapper.schemeDTOToSchemeEntity(schemeWithTwo, newTransactions);

        assertThat(schemeEntity3).isNotNull();
        assertThat(newTransactions.get()).isEqualTo(2);
        assertThat(schemeEntity3.getTransactions()).hasSize(2);
    }
}
