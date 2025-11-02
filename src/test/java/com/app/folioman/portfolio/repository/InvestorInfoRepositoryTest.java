package com.app.folioman.portfolio.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.folioman.portfolio.entities.InvestorInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(com.app.folioman.config.SQLContainersConfig.class)
class InvestorInfoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvestorInfoRepository investorInfoRepository;

    @Test
    @Disabled
    void existsByEmailAndName_WhenInvestorExists_ReturnsTrue() {
        InvestorInfo investor = new InvestorInfo();
        investor.setEmail("test@example.com");
        investor.setName("John Doe");
        entityManager.persistAndFlush(investor);

        boolean exists = investorInfoRepository.existsByEmailAndName("test@example.com", "John Doe");

        assertTrue(exists);
    }

    @Test
    void existsByEmailAndName_WhenInvestorDoesNotExist_ReturnsFalse() {
        boolean exists = investorInfoRepository.existsByEmailAndName("nonexistent@example.com", "Jane Doe");

        assertFalse(exists);
    }

    @Test
    @Disabled
    void existsByEmailAndName_WhenEmailDoesNotMatch_ReturnsFalse() {
        InvestorInfo investor = new InvestorInfo();
        investor.setEmail("test@example.com");
        investor.setName("John Doe");
        entityManager.persistAndFlush(investor);

        boolean exists = investorInfoRepository.existsByEmailAndName("different@example.com", "John Doe");

        assertFalse(exists);
    }

    @Test
    @Disabled
    void existsByEmailAndName_WhenNameDoesNotMatch_ReturnsFalse() {
        InvestorInfo investor = new InvestorInfo();
        investor.setEmail("test@example.com");
        investor.setName("John Doe");
        entityManager.persistAndFlush(investor);

        boolean exists = investorInfoRepository.existsByEmailAndName("test@example.com", "Jane Smith");

        assertFalse(exists);
    }

    @Test
    @Disabled
    void existsByEmailAndName_WhenMultipleInvestorsExist_ReturnsCorrectResult() {
        InvestorInfo investor1 = new InvestorInfo();
        investor1.setEmail("test1@example.com");
        investor1.setName("John Doe");
        entityManager.persistAndFlush(investor1);

        InvestorInfo investor2 = new InvestorInfo();
        investor2.setEmail("test2@example.com");
        investor2.setName("Jane Smith");
        entityManager.persistAndFlush(investor2);

        assertTrue(investorInfoRepository.existsByEmailAndName("test1@example.com", "John Doe"));
        assertTrue(investorInfoRepository.existsByEmailAndName("test2@example.com", "Jane Smith"));
        assertFalse(investorInfoRepository.existsByEmailAndName("test1@example.com", "Jane Smith"));
        assertFalse(investorInfoRepository.existsByEmailAndName("test2@example.com", "John Doe"));
    }

    @Test
    void existsByEmailAndName_WithEmptyStrings_ReturnsFalse() {
        boolean exists = investorInfoRepository.existsByEmailAndName("", "");

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndName_WithNullEmail_ReturnsFalse() {
        boolean exists = investorInfoRepository.existsByEmailAndName(null, "John Doe");

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndName_WithNullName_ReturnsFalse() {
        boolean exists = investorInfoRepository.existsByEmailAndName("test@example.com", null);

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndName_WithBothNullParameters_ReturnsFalse() {
        boolean exists = investorInfoRepository.existsByEmailAndName(null, null);

        assertFalse(exists);
    }
}
