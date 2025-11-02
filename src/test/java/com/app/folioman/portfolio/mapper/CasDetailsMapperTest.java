package com.app.folioman.portfolio.mapper;

import static org.junit.jupiter.api.Assertions.*;

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
    void testConvert() {

        // use a real DTO here to exercise the mapper, but verify via the spy
        CasDTO local = new CasDTO(null, FileTypeEnum.KARVY.name(), CasTypeEnum.SUMMARY.name(), null, new ArrayList<>());
        UserCASDetails result = mapper.convert(local, newFolios, newSchemes, newTransactions);

        assertNotNull(result);
        // basic property mappings
        assertEquals(FileTypeEnum.KARVY, result.getFileTypeEnum());
        assertEquals(CasTypeEnum.SUMMARY, result.getCasTypeEnum());
    }

    @Test
    void testConvertWithNullInput() {
        // MapStruct implementation triggers @AfterMapping which assumes non-null input
        assertThrows(NullPointerException.class, () -> mapper.convert(null, newFolios, newSchemes, newTransactions));
    }

    @Test
    void testMapUserFolioDTOToUserFolioDetails() {

        // use a real DTO and map to entity to validate mapping
        UserFolioDTO localFolio = new UserFolioDTO("124567", "JioAMC", "ABCDE1234F", "OK", "OK", new ArrayList<>());
        UserFolioDetails result = mapper.mapUserFolioDTOToUserFolioDetails(localFolio, newSchemes, newTransactions);

        assertNotNull(result);
        assertEquals("124567", result.getFolio());
    }

    @Test
    void testMapUserFolioDTOToUserFolioDetailsWithNullInput() {
        assertThrows(
                NullPointerException.class,
                () -> mapper.mapUserFolioDTOToUserFolioDetails(null, newSchemes, newTransactions));
    }

    @Test
    void testSchemeDTOToSchemeEntity() {

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

        assertNotNull(result);
        // no id mapping expected
        assertNull(result.getId());
    }

    @Test
    void testSchemeDTOToSchemeEntityWithNullInput() {
        assertThrows(NullPointerException.class, () -> mapper.schemeDTOToSchemeEntity(null, newTransactions));
    }

    @Test
    void testTransactionDTOToTransactionEntity() {

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

        assertNotNull(result);
        assertEquals(com.app.folioman.portfolio.models.request.TransactionType.PURCHASE, result.getType());
    }

    @Test
    void testTransactionDTOToTransactionEntityWithNullInput() {
        UserTransactionDetails result = mapper.transactionDTOToTransactionEntity(null);

        assertNull(result);
    }

    @Test
    void testAddFolioEntityToCaseDetails() {
        // use a real CasDTO with a single folio to exercise the @AfterMapping hook
        UserFolioDTO folioDTO = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", new ArrayList<>());
        CasDTO localCas =
                new CasDTO(null, FileTypeEnum.CAMS.name(), CasTypeEnum.DETAILED.name(), null, List.of(folioDTO));
        com.app.folioman.portfolio.entities.UserCASDetails userCAS =
                mapper.convert(localCas, newFolios, newSchemes, newTransactions);

        assertNotNull(userCAS);
        assertEquals(1, newFolios.get());
        assertEquals(1, userCAS.getFolios().size());
    }

    @Test
    void testAddFolioEntityToCaseDetailsWithEmptyFolios() {
        CasDTO emptyCas = new CasDTO(null, FileTypeEnum.CAMS.name(), CasTypeEnum.DETAILED.name(), null, List.of());
        com.app.folioman.portfolio.entities.UserCASDetails userCAS =
                mapper.convert(emptyCas, newFolios, newSchemes, newTransactions);

        assertNotNull(userCAS);
        assertEquals(0, newFolios.get());
    }

    @Test
    void testAddFolioEntityToCaseDetailsWithMultipleFolios() {
        UserFolioDTO f1 = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", new ArrayList<>());
        UserFolioDTO f2 = new UserFolioDTO("F2", "AMC", "PAN", "KYC", "PANKYC", new ArrayList<>());
        CasDTO localCas2 =
                new CasDTO(null, FileTypeEnum.CAMS.name(), CasTypeEnum.DETAILED.name(), null, List.of(f1, f2));
        com.app.folioman.portfolio.entities.UserCASDetails userCAS2 =
                mapper.convert(localCas2, newFolios, newSchemes, newTransactions);

        assertNotNull(userCAS2);
        assertEquals(2, newFolios.get());
        assertEquals(2, userCAS2.getFolios().size());
    }

    @Test
    void testAddSchemaEntityToFolioEntity() {
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

        assertNotNull(folioEntity);
        assertEquals(1, newSchemes.get());
        assertEquals(1, folioEntity.getSchemes().size());
    }

    @Test
    void testAddSchemaEntityToFolioEntityWithEmptySchemes() {
        UserFolioDTO folioWithoutSchemes = new UserFolioDTO("F1", "AMC", "PAN", "KYC", "PANKYC", List.of());
        com.app.folioman.portfolio.entities.UserFolioDetails folioEntity2 =
                mapper.mapUserFolioDTOToUserFolioDetails(folioWithoutSchemes, newSchemes, newTransactions);

        assertNotNull(folioEntity2);
        assertEquals(0, newSchemes.get());
    }

    @Test
    void testAddSchemaEntityToFolioEntityWithMultipleSchemes() {
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

        assertNotNull(folioEntity3);
        assertEquals(2, newSchemes.get());
        assertEquals(2, folioEntity3.getSchemes().size());
    }

    @Test
    void testAddTransactionEntityToSchemeEntity() {
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

        assertNotNull(schemeEntity);
        assertEquals(1, newTransactions.get());
        assertEquals(1, schemeEntity.getTransactions().size());
    }

    @Test
    void testAddTransactionEntityToSchemeEntityWithEmptyTransactions() {
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

        assertNotNull(schemeEntity2);
        assertEquals(0, newTransactions.get());
    }

    @Test
    void testAddTransactionEntityToSchemeEntityWithMultipleTransactions() {
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

        assertNotNull(schemeEntity3);
        assertEquals(2, newTransactions.get());
        assertEquals(2, schemeEntity3.getTransactions().size());
    }
}
