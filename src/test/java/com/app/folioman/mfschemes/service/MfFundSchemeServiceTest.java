package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class MfFundSchemeServiceTest {

    @Mock
    private MfFundSchemeRepository mfFundSchemeRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    private MfFundSchemeService mfFundSchemeService;

    @BeforeEach
    void setUp() {
        // Use lenient stubbing for transaction manager to avoid unnecessary stubbing failures
        org.mockito.Mockito.lenient()
                .when(transactionManager.getTransaction(any()))
                .thenReturn(transactionStatus);
        mfFundSchemeService = new MfFundSchemeService(mfFundSchemeRepository, transactionManager);
    }

    @Test
    void saveDataInBatches_shouldReturnZero_whenInputIsNull() {
        int result = mfFundSchemeService.saveDataInBatches(null, 10);

        assertThat(result).isZero();
        verify(mfFundSchemeRepository, never()).saveAll(anyList());
    }

    @Test
    void saveDataInBatches_shouldReturnZero_whenInputIsEmpty() {
        List<MfFundScheme> emptyList = new ArrayList<>();

        int result = mfFundSchemeService.saveDataInBatches(emptyList, 10);

        assertThat(result).isZero();
        verify(mfFundSchemeRepository, never()).saveAll(anyList());
    }

    @Test
    void saveDataInBatches_shouldSaveAllSuccessfully_whenNormalOperation() {
        List<MfFundScheme> schemes = Arrays.asList(new MfFundScheme(), new MfFundScheme(), new MfFundScheme());

        int result = mfFundSchemeService.saveDataInBatches(schemes, 2);

        assertThat(result).isEqualTo(3);
        verify(mfFundSchemeRepository, times(2)).saveAll(anyList());
    }

    @Test
    void saveDataInBatches_shouldHandleBatchFailureWithIndividualRetry() {
        List<MfFundScheme> schemes = Arrays.asList(new MfFundScheme(), new MfFundScheme());

        doThrow(new DataAccessException("Batch failed") {})
                .when(mfFundSchemeRepository)
                .saveAll(anyList());

        int result = mfFundSchemeService.saveDataInBatches(schemes, 2);

        assertThat(result).isEqualTo(2);
        verify(mfFundSchemeRepository, times(1)).saveAll(anyList());
        verify(mfFundSchemeRepository, times(2)).save(any(MfFundScheme.class));
    }

    @Test
    void saveDataInBatches_shouldHandlePartialIndividualFailures() {
        List<MfFundScheme> schemes = Arrays.asList(new MfFundScheme(), new MfFundScheme());

        doThrow(new DataAccessException("Batch failed") {})
                .when(mfFundSchemeRepository)
                .saveAll(anyList());

        // Simulate first individual save throwing, second succeeding by using an invocation counter
        final java.util.concurrent.atomic.AtomicInteger saveCounter = new java.util.concurrent.atomic.AtomicInteger(0);
        org.mockito.Mockito.doAnswer(invocation -> {
                    int count = saveCounter.getAndIncrement();
                    if (count == 0) {
                        // throw only on the first invocation
                        throw new DataAccessException("Individual failed") {};
                    }
                    return null;
                })
                .when(mfFundSchemeRepository)
                .save(any(MfFundScheme.class));

        int result = mfFundSchemeService.saveDataInBatches(schemes, 2);

        assertThat(result).isOne();
        verify(mfFundSchemeRepository, times(2)).save(any(MfFundScheme.class));
    }

    @Test
    void saveDataInBatches_shouldProcessMultipleBatches() {
        List<MfFundScheme> schemes = Arrays.asList(
                new MfFundScheme(), new MfFundScheme(), new MfFundScheme(), new MfFundScheme(), new MfFundScheme());

        int result = mfFundSchemeService.saveDataInBatches(schemes, 2);

        assertThat(result).isEqualTo(5);
        verify(mfFundSchemeRepository, times(3)).saveAll(anyList());
    }

    @Test
    void saveDataInBatches_shouldHandleBatchSizeGreaterThanListSize() {
        List<MfFundScheme> schemes = Arrays.asList(new MfFundScheme(), new MfFundScheme());

        int result = mfFundSchemeService.saveDataInBatches(schemes, 10);

        assertThat(result).isEqualTo(2);
        verify(mfFundSchemeRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getTotalCount_shouldReturnRepositoryCount() {
        when(mfFundSchemeRepository.count()).thenReturn(42L);

        long result = mfFundSchemeService.getTotalCount();

        assertThat(result).isEqualTo(42L);
        verify(mfFundSchemeRepository).count();
    }

    @Test
    void findDistinctAmfiCode_shouldReturnRepositoryResult() {
        List<String> expectedCodes = Arrays.asList("CODE1", "CODE2", "CODE3");
        when(mfFundSchemeRepository.findDistinctAmfiCode()).thenReturn(expectedCodes);

        List<String> result = mfFundSchemeService.findDistinctAmfiCode();

        assertThat(result).containsExactlyElementsOf(expectedCodes);
        verify(mfFundSchemeRepository).findDistinctAmfiCode();
    }

    @Test
    void findDistinctAmfiCode_shouldReturnEmptyList_whenNoData() {
        when(mfFundSchemeRepository.findDistinctAmfiCode()).thenReturn(Collections.emptyList());

        List<String> result = mfFundSchemeService.findDistinctAmfiCode();

        assertThat(result).containsExactlyElementsOf(Collections.emptyList());
        verify(mfFundSchemeRepository).findDistinctAmfiCode();
    }
}
