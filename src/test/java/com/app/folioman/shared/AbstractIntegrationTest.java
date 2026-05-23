package com.app.folioman.shared;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.app.folioman.Application;
import com.app.folioman.config.NoSQLContainersConfig;
import com.app.folioman.config.SQLContainersConfig;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.domain.MfSchemeDtoToEntityMapper;
import com.app.folioman.mfschemes.domain.MfSchemeNavRepository;
import com.app.folioman.portfolio.config.PortfolioCacheProperties;
import com.app.folioman.portfolio.domain.UserPortfolioValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base class for integration tests using a hybrid database approach:
 * - NoSQL (MongoDB) for JobRunr and related operations
 * - SQL for core application data
 */
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {Application.class, NoSQLContainersConfig.class, SQLContainersConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    protected MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;

    @Autowired
    protected ApplicationProperties applicationProperties;

    @Autowired
    protected PortfolioCacheProperties portfolioCacheProperties;

    @Autowired
    protected UserPortfolioValueRepository userPortfolioValueRepository;

    @Autowired
    protected MfSchemeNavRepository mfSchemeNavRepository;

    @LocalServerPort
    protected int port;

    protected RequestPostProcessor testUser() {
        return user("user").roles("USER");
    }
}
