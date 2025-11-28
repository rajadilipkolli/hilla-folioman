package com.app.folioman.portfolio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.repository.InvestorInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestorInfoServiceTest {

    @Mock
    private InvestorInfoRepository investorInfoRepository;

    private InvestorInfoService investorInfoService;

    @BeforeEach
    void setUp() {
        investorInfoService = new InvestorInfoService(investorInfoRepository);
    }

    @Test
    void constructor_ShouldCreateInstance_WhenRepositoryIsProvided() {
        InvestorInfoService service = new InvestorInfoService(investorInfoRepository);
        assertNotNull(service);
    }

    @Test
    void existsByEmailAndName_ShouldReturnTrue_WhenRepositoryReturnsTrue() {
        String email = "test@example.com";
        String name = "John Doe";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(true);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertTrue(result);
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }

    @Test
    void existsByEmailAndName_ShouldReturnFalse_WhenRepositoryReturnsFalse() {
        String email = "test@example.com";
        String name = "John Doe";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertFalse(result);
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }

    @Test
    void existsByEmailAndName_ShouldDelegateToRepository_WhenEmailIsNull() {
        String name = "John Doe";
        when(investorInfoRepository.existsByEmailAndName(null, name)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(null, name);

        assertFalse(result);
        verify(investorInfoRepository).existsByEmailAndName(null, name);
    }

    @Test
    void existsByEmailAndName_ShouldDelegateToRepository_WhenNameIsNull() {
        String email = "test@example.com";
        when(investorInfoRepository.existsByEmailAndName(email, null)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(email, null);

        assertFalse(result);
        verify(investorInfoRepository).existsByEmailAndName(email, null);
    }

    @Test
    void existsByEmailAndName_ShouldDelegateToRepository_WhenEmailIsEmpty() {
        String email = "";
        String name = "John Doe";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertFalse(result);
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }

    @Test
    void existsByEmailAndName_ShouldDelegateToRepository_WhenNameIsEmpty() {
        String email = "test@example.com";
        String name = "";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertFalse(result);
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }

    @Test
    void existsByEmailAndName_ShouldDelegateToRepository_WhenBothParametersAreNull() {
        when(investorInfoRepository.existsByEmailAndName(null, null)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(null, null);

        assertFalse(result);
        verify(investorInfoRepository).existsByEmailAndName(null, null);
    }
}
