package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.portfolio.rest.dtos.Fund;
import com.app.folioman.portfolio.rest.dtos.InvestmentRequest;
import com.app.folioman.portfolio.rest.dtos.InvestmentResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReBalanceServiceImplTest {

    private final ReBalanceServiceImpl reBalanceService = new ReBalanceServiceImpl();

    @Test
    void reBalanceComputesTargetInvestments() {
        List<Fund> funds = List.of(new Fund(5000.0, 0.4), new Fund(3000.0, 0.3), new Fund(2000.0, 0.3));
        InvestmentRequest request = new InvestmentRequest(funds, 1000.0);

        InvestmentResponse response = reBalanceService.reBalance(request);

        assertThat(response.investments()).containsExactly(-600.0, 300.0, 1300.0);
    }

    @Test
    void reBalanceHandlesLargeNumbersWithoutPrecisionDrift() {
        List<Fund> funds = List.of(new Fund(500000.0, 0.5), new Fund(500000.0, 0.5));
        InvestmentRequest request = new InvestmentRequest(funds, 100000.0);

        InvestmentResponse response = reBalanceService.reBalance(request);

        assertThat(response.investments()).containsExactly(50000.0, 50000.0);
    }
}
