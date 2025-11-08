package com.app.folioman.portfolio.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.portfolio.entities.CasTypeEnum;
import com.app.folioman.portfolio.entities.FileTypeEnum;
import com.app.folioman.portfolio.entities.InvestorInfo;
import com.app.folioman.portfolio.entities.UserCASDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(SQLContainersConfig.class)
class InvestorInfoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvestorInfoRepository investorInfoRepository;

    @Test
    void existsByEmailAndName_WhenInvestorExists_ReturnsTrue() {
        InvestorInfo investor = new InvestorInfo();
        investor.setEmail("test@example.com");
        investor.setName("John Doe");
        // InvestorInfo has a @OneToOne with @MapsId to UserCASDetails; create and set the
        // required UserCASDetails so the id generation works in tests.
        UserCASDetails userCas = new UserCASDetails();
        // set required enum fields with reasonable defaults for tests
        userCas.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCas.setFileTypeEnum(FileTypeEnum.CAMS);
        userCas.setInvestorInfo(investor);
        // persist the owner (UserCASDetails) which will cascade to InvestorInfo
        entityManager.persistAndFlush(userCas);

        boolean exists = investorInfoRepository.existsByEmailAndName("test@example.com", "John Doe");

        assertTrue(exists);
    }

    @Test
    void existsByEmailAndName_WhenInvestorDoesNotExist_ReturnsFalse() {
        boolean exists = investorInfoRepository.existsByEmailAndName("nonexistent@example.com", "Jane Doe");

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndName_WhenEmailDoesNotMatch_ReturnsFalse() {
        InvestorInfo investor = new InvestorInfo();
        investor.setEmail("test@example.com");
        investor.setName("John Doe");
        UserCASDetails userCas = new UserCASDetails();
        userCas.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCas.setFileTypeEnum(FileTypeEnum.CAMS);
        userCas.setInvestorInfo(investor);
        entityManager.persistAndFlush(userCas);

        boolean exists = investorInfoRepository.existsByEmailAndName("different@example.com", "John Doe");

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndName_WhenNameDoesNotMatch_ReturnsFalse() {
        InvestorInfo investor = new InvestorInfo();
        investor.setEmail("test@example.com");
        investor.setName("John Doe");
        UserCASDetails userCas = new UserCASDetails();
        userCas.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCas.setFileTypeEnum(FileTypeEnum.CAMS);
        userCas.setInvestorInfo(investor);
        entityManager.persistAndFlush(userCas);

        boolean exists = investorInfoRepository.existsByEmailAndName("test@example.com", "Jane Smith");

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndName_WhenMultipleInvestorsExist_ReturnsCorrectResult() {
        InvestorInfo investor1 = new InvestorInfo();
        investor1.setEmail("test1@example.com");
        investor1.setName("John Doe");
        UserCASDetails userCas1 = new UserCASDetails();
        userCas1.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCas1.setFileTypeEnum(FileTypeEnum.CAMS);
        userCas1.setInvestorInfo(investor1);
        entityManager.persistAndFlush(userCas1);

        InvestorInfo investor2 = new InvestorInfo();
        investor2.setEmail("test2@example.com");
        investor2.setName("Jane Smith");
        UserCASDetails userCas2 = new UserCASDetails();
        userCas2.setCasTypeEnum(CasTypeEnum.DETAILED);
        userCas2.setFileTypeEnum(FileTypeEnum.CAMS);
        userCas2.setInvestorInfo(investor2);
        entityManager.persistAndFlush(userCas2);

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
