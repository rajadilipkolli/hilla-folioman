package com.app.folioman.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.repository.InvestorInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestorInfoServiceTest {

    @Mock
    private InvestorInfoRepository investorInfoRepository;

    @InjectMocks
    private InvestorInfoService investorInfoService;

    @Test
    void constructor_ShouldCreateInstance_WhenRepositoryIsProvided() {
        InvestorInfoService service = new InvestorInfoService(investorInfoRepository);
        assertThat(service).isNotNull();
    }

    @Test
    void existsByEmailAndName_ShouldReturnTrue_WhenRepositoryReturnsTrue() {
        String email = "test@example.com";
        String name = "John Doe";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(true);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertThat(result).isTrue();
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }

    @Test
    void existsByEmailAndName_ShouldReturnFalse_WhenRepositoryReturnsFalse() {
        String email = "test@example.com";
        String name = "John Doe";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertThat(result).isFalse();
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }

    @Test
    void existsByEmailAndName_ShouldDelegateToRepository_WhenEmailIsEmpty() {
        String email = "";
        String name = "John Doe";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertThat(result).isFalse();
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }

    @Test
    void existsByEmailAndName_ShouldDelegateToRepository_WhenNameIsEmpty() {
        String email = "test@example.com";
        String name = "";
        when(investorInfoRepository.existsByEmailAndName(email, name)).thenReturn(false);

        boolean result = investorInfoService.existsByEmailAndName(email, name);

        assertThat(result).isFalse();
        verify(investorInfoRepository).existsByEmailAndName(email, name);
    }
}
