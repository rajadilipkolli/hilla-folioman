package com.app.folioman.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.portfolio.config.PortfolioCacheProperties;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for integration tests using a hybrid database approach:
 * - NoSQL (MongoDB) for JobRunr and related operations
 * - SQL for core application data
 */
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {NoSQLContainersConfig.class, SQLContainersConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserPortfolioValueRepository userPortfolioValueRepository;

    @Autowired
    protected MFSchemeNavRepository mfSchemeNavRepository;

    @Autowired
    protected ApplicationProperties applicationProperties;

    @Autowired
    protected PortfolioCacheProperties portfolioCacheProperties;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;
}
