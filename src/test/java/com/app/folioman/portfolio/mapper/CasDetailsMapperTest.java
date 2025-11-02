package com.app.folioman.portfolio.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.request.CasDTO;
import com.app.folioman.portfolio.models.request.UserFolioDTO;
import com.app.folioman.portfolio.models.request.UserSchemeDTO;
import com.app.folioman.portfolio.models.request.UserTransactionDTO;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CasDetailsMapperTest {

    @Mock
    private CasDetailsMapper mapper;

    @Mock
    private CasDTO casDTO;

    @Mock
    private UserFolioDTO userFolioDTO;

    @Mock
    private UserSchemeDTO userSchemeDTO;

    @Mock
    private UserTransactionDTO userTransactionDTO;

    @Mock
    private UserCASDetails userCASDetails;

    @Mock
    private UserFolioDetails userFolioDetails;

    @Mock
    private UserSchemeDetails userSchemeDetails;

    @Mock
    private UserTransactionDetails userTransactionDetails;

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
        when(mapper.convert(
                        any(CasDTO.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class)))
                .thenReturn(userCASDetails);

        UserCASDetails result = mapper.convert(casDTO, newFolios, newSchemes, newTransactions);

        assertNotNull(result);
        verify(mapper).convert(casDTO, newFolios, newSchemes, newTransactions);
    }

    @Test
    void testConvertWithNullInput() {
        when(mapper.convert(isNull(), any(AtomicInteger.class), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(null);

        UserCASDetails result = mapper.convert(null, newFolios, newSchemes, newTransactions);

        assertNull(result);
        verify(mapper).convert(null, newFolios, newSchemes, newTransactions);
    }

    @Test
    void testMapUserFolioDTOToUserFolioDetails() {
        when(mapper.mapUserFolioDTOToUserFolioDetails(
                        any(UserFolioDTO.class), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(userFolioDetails);

        UserFolioDetails result = mapper.mapUserFolioDTOToUserFolioDetails(userFolioDTO, newSchemes, newTransactions);

        assertNotNull(result);
        verify(mapper).mapUserFolioDTOToUserFolioDetails(userFolioDTO, newSchemes, newTransactions);
    }

    @Test
    void testMapUserFolioDTOToUserFolioDetailsWithNullInput() {
        when(mapper.mapUserFolioDTOToUserFolioDetails(isNull(), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(null);

        UserFolioDetails result = mapper.mapUserFolioDTOToUserFolioDetails(null, newSchemes, newTransactions);

        assertNull(result);
        verify(mapper).mapUserFolioDTOToUserFolioDetails(null, newSchemes, newTransactions);
    }

    @Test
    void testSchemeDTOToSchemeEntity() {
        when(mapper.schemeDTOToSchemeEntity(any(UserSchemeDTO.class), any(AtomicInteger.class)))
                .thenReturn(userSchemeDetails);

        UserSchemeDetails result = mapper.schemeDTOToSchemeEntity(userSchemeDTO, newTransactions);

        assertNotNull(result);
        verify(mapper).schemeDTOToSchemeEntity(userSchemeDTO, newTransactions);
    }

    @Test
    void testSchemeDTOToSchemeEntityWithNullInput() {
        when(mapper.schemeDTOToSchemeEntity(isNull(), any(AtomicInteger.class))).thenReturn(null);

        UserSchemeDetails result = mapper.schemeDTOToSchemeEntity(null, newTransactions);

        assertNull(result);
        verify(mapper).schemeDTOToSchemeEntity(null, newTransactions);
    }

    @Test
    void testTransactionDTOToTransactionEntity() {
        when(mapper.transactionDTOToTransactionEntity(any(UserTransactionDTO.class)))
                .thenReturn(userTransactionDetails);

        UserTransactionDetails result = mapper.transactionDTOToTransactionEntity(userTransactionDTO);

        assertNotNull(result);
        verify(mapper).transactionDTOToTransactionEntity(userTransactionDTO);
    }

    @Test
    void testTransactionDTOToTransactionEntityWithNullInput() {
        when(mapper.transactionDTOToTransactionEntity(isNull())).thenReturn(null);

        UserTransactionDetails result = mapper.transactionDTOToTransactionEntity(null);

        assertNull(result);
        verify(mapper).transactionDTOToTransactionEntity(null);
    }

    @Test
    void testAddFolioEntityToCaseDetails() {
        List<UserFolioDTO> folios = List.of(userFolioDTO);
        when(casDTO.folios()).thenReturn(folios);
        when(mapper.mapUserFolioDTOToUserFolioDetails(
                        any(UserFolioDTO.class), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(userFolioDetails);
        doCallRealMethod()
                .when(mapper)
                .addFolioEntityToCaseDetails(
                        any(CasDTO.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(UserCASDetails.class));

        mapper.addFolioEntityToCaseDetails(casDTO, newFolios, newSchemes, newTransactions, userCASDetails);

        verify(mapper).mapUserFolioDTOToUserFolioDetails(userFolioDTO, newSchemes, newTransactions);
        verify(userCASDetails).addFolioEntity(userFolioDetails);
        assertEquals(1, newFolios.get());
    }

    @Test
    void testAddFolioEntityToCaseDetailsWithEmptyFolios() {
        when(casDTO.folios()).thenReturn(List.of());
        doCallRealMethod()
                .when(mapper)
                .addFolioEntityToCaseDetails(
                        any(CasDTO.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(UserCASDetails.class));

        mapper.addFolioEntityToCaseDetails(casDTO, newFolios, newSchemes, newTransactions, userCASDetails);

        verify(mapper, never())
                .mapUserFolioDTOToUserFolioDetails(
                        any(UserFolioDTO.class), any(AtomicInteger.class), any(AtomicInteger.class));
        verify(userCASDetails, never()).addFolioEntity(any(UserFolioDetails.class));
        assertEquals(0, newFolios.get());
    }

    @Test
    void testAddFolioEntityToCaseDetailsWithMultipleFolios() {
        List<UserFolioDTO> folios = List.of(userFolioDTO, userFolioDTO);
        when(casDTO.folios()).thenReturn(folios);
        when(mapper.mapUserFolioDTOToUserFolioDetails(
                        any(UserFolioDTO.class), any(AtomicInteger.class), any(AtomicInteger.class)))
                .thenReturn(userFolioDetails);
        doCallRealMethod()
                .when(mapper)
                .addFolioEntityToCaseDetails(
                        any(CasDTO.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(UserCASDetails.class));

        mapper.addFolioEntityToCaseDetails(casDTO, newFolios, newSchemes, newTransactions, userCASDetails);

        verify(mapper, times(2)).mapUserFolioDTOToUserFolioDetails(userFolioDTO, newSchemes, newTransactions);
        verify(userCASDetails, times(2)).addFolioEntity(userFolioDetails);
        assertEquals(2, newFolios.get());
    }

    @Test
    void testAddSchemaEntityToFolioEntity() {
        List<UserSchemeDTO> schemes = List.of(userSchemeDTO);
        when(userFolioDTO.schemes()).thenReturn(schemes);
        when(mapper.schemeDTOToSchemeEntity(any(UserSchemeDTO.class), any(AtomicInteger.class)))
                .thenReturn(userSchemeDetails);
        doCallRealMethod()
                .when(mapper)
                .addSchemaEntityToFolioEntity(
                        any(UserFolioDTO.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(UserFolioDetails.class));

        mapper.addSchemaEntityToFolioEntity(userFolioDTO, newSchemes, newTransactions, userFolioDetails);

        verify(mapper).schemeDTOToSchemeEntity(userSchemeDTO, newTransactions);
        verify(userFolioDetails).addScheme(userSchemeDetails);
        assertEquals(1, newSchemes.get());
    }

    @Test
    void testAddSchemaEntityToFolioEntityWithEmptySchemes() {
        when(userFolioDTO.schemes()).thenReturn(List.of());
        doCallRealMethod()
                .when(mapper)
                .addSchemaEntityToFolioEntity(
                        any(UserFolioDTO.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(UserFolioDetails.class));

        mapper.addSchemaEntityToFolioEntity(userFolioDTO, newSchemes, newTransactions, userFolioDetails);

        verify(mapper, never()).schemeDTOToSchemeEntity(any(UserSchemeDTO.class), any(AtomicInteger.class));
        verify(userFolioDetails, never()).addScheme(any(UserSchemeDetails.class));
        assertEquals(0, newSchemes.get());
    }

    @Test
    void testAddSchemaEntityToFolioEntityWithMultipleSchemes() {
        List<UserSchemeDTO> schemes = List.of(userSchemeDTO, userSchemeDTO);
        when(userFolioDTO.schemes()).thenReturn(schemes);
        when(mapper.schemeDTOToSchemeEntity(any(UserSchemeDTO.class), any(AtomicInteger.class)))
                .thenReturn(userSchemeDetails);
        doCallRealMethod()
                .when(mapper)
                .addSchemaEntityToFolioEntity(
                        any(UserFolioDTO.class),
                        any(AtomicInteger.class),
                        any(AtomicInteger.class),
                        any(UserFolioDetails.class));

        mapper.addSchemaEntityToFolioEntity(userFolioDTO, newSchemes, newTransactions, userFolioDetails);

        verify(mapper, times(2)).schemeDTOToSchemeEntity(userSchemeDTO, newTransactions);
        verify(userFolioDetails, times(2)).addScheme(userSchemeDetails);
        assertEquals(2, newSchemes.get());
    }

    @Test
    void testAddTransactionEntityToSchemeEntity() {
        List<UserTransactionDTO> transactions = List.of(userTransactionDTO);
        when(userSchemeDTO.transactions()).thenReturn(transactions);
        when(mapper.transactionDTOToTransactionEntity(any(UserTransactionDTO.class)))
                .thenReturn(userTransactionDetails);
        doCallRealMethod()
                .when(mapper)
                .addTransactionEntityToSchemeEntity(
                        any(UserSchemeDTO.class), any(AtomicInteger.class), any(UserSchemeDetails.class));

        mapper.addTransactionEntityToSchemeEntity(userSchemeDTO, newTransactions, userSchemeDetails);

        verify(mapper).transactionDTOToTransactionEntity(userTransactionDTO);
        verify(userSchemeDetails).addTransaction(userTransactionDetails);
        assertEquals(1, newTransactions.get());
    }

    @Test
    void testAddTransactionEntityToSchemeEntityWithEmptyTransactions() {
        when(userSchemeDTO.transactions()).thenReturn(List.of());
        doCallRealMethod()
                .when(mapper)
                .addTransactionEntityToSchemeEntity(
                        any(UserSchemeDTO.class), any(AtomicInteger.class), any(UserSchemeDetails.class));

        mapper.addTransactionEntityToSchemeEntity(userSchemeDTO, newTransactions, userSchemeDetails);

        verify(mapper, never()).transactionDTOToTransactionEntity(any(UserTransactionDTO.class));
        verify(userSchemeDetails, never()).addTransaction(any(UserTransactionDetails.class));
        assertEquals(0, newTransactions.get());
    }

    @Test
    void testAddTransactionEntityToSchemeEntityWithMultipleTransactions() {
        List<UserTransactionDTO> transactions = List.of(userTransactionDTO, userTransactionDTO);
        when(userSchemeDTO.transactions()).thenReturn(transactions);
        when(mapper.transactionDTOToTransactionEntity(any(UserTransactionDTO.class)))
                .thenReturn(userTransactionDetails);
        doCallRealMethod()
                .when(mapper)
                .addTransactionEntityToSchemeEntity(
                        any(UserSchemeDTO.class), any(AtomicInteger.class), any(UserSchemeDetails.class));

        mapper.addTransactionEntityToSchemeEntity(userSchemeDTO, newTransactions, userSchemeDetails);

        verify(mapper, times(2)).transactionDTOToTransactionEntity(userTransactionDTO);
        verify(userSchemeDetails, times(2)).addTransaction(userTransactionDetails);
        assertEquals(2, newTransactions.get());
    }
}
