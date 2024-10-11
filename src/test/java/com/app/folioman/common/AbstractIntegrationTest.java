package com.app.folioman.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {RedisContainersConfig.class, SQLContainersConfig.class})
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserPortfolioValueRepository userPortfolioValueRepository;

    @Autowired
    protected MFSchemeNavRepository mfSchemeNavRepository;
}
