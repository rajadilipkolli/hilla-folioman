package com.app.folioman.portfolio.rest.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.auth.domain.JwtService;
import com.app.folioman.portfolio.domain.PortfolioAPI;
import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(controllers = ImportMutualFundController.class)
@WithMockUser(roles = "USER")
@Execution(ExecutionMode.SAME_THREAD)
class ImportMutualFundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioAPI portfolioAPI;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        reset(portfolioAPI);
    }

    @Test
    void getPortfolioInvalidPan() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCD1234EF")
                        .param("asOfDate", "2024-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("getPortfolio.panNumber: Invalid PAN number format")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/ABCD1234EF")));
    }

    @Test
    void getPortfolioForAfterDate() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F")
                        .param("asOfDate", "2100-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("getPortfolio.asOfDate: Date should be past or today")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/ABCDE1234F")));
    }

    @Test
    void getPortfolioForAfterDateAndInValidPan() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCD1234EF")
                        .param("asOfDate", "2100-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(
                        jsonPath(
                                "$.detail",
                                matchesPattern(
                                        "getPortfolio\\.asOfDate: Date should be past or today, getPortfolio\\.panNumber: Invalid PAN number format|getPortfolio\\.panNumber: Invalid PAN number format, getPortfolio\\.asOfDate: Date should be past or today")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/ABCD1234EF")));
    }

    @Test
    void getPortfolio_HappyPath() throws Exception {
        String pan = "ABCDE1234F";
        PortfolioResponse response = new PortfolioResponse(new BigDecimal("15000"), List.of());

        doReturn(response).when(portfolioAPI).getPortfolioByPAN(eq(pan), any());

        this.mockMvc
                .perform(get("/api/portfolio/{pan}", pan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPortfolioValue", is(15000)));

        verify(portfolioAPI).getPortfolioByPAN(eq(pan), any());
    }

    @Test
    void upload_HappyPath() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON_VALUE, "{}".getBytes());
        UploadFileResponse response = new UploadFileResponse(1, 2, 3, 100L);

        doReturn(response).when(portfolioAPI).upload(any(MultipartFile.class));

        this.mockMvc
                .perform(
                        multipart("/api/upload-handler").with(csrf()).file(file).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newFolios", is(1)))
                .andExpect(jsonPath("$.newSchemes", is(2)))
                .andExpect(jsonPath("$.newTransactions", is(3)))
                .andExpect(jsonPath("$.userCASDetailsId", is(100)));

        verify(portfolioAPI).upload(any(MultipartFile.class));
    }

    @Test
    void uploadPasswordProtectedCasPdf_HappyPath() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf content".getBytes());
        MockMultipartFile password = new MockMultipartFile("password", "", "text/plain", "pass123".getBytes());
        UploadFileResponse response = new UploadFileResponse(1, 2, 3, 100L);

        // Mock PDF conversion
        CasDTO mockCasDto = new CasDTO(null, null, null, null, List.of());
        doReturn(mockCasDto).when(portfolioAPI).convertPdfCasToJson(any(MultipartFile.class), eq("pass123"));
        doReturn(response).when(portfolioAPI).uploadFromDto(mockCasDto);

        this.mockMvc
                .perform(multipart("/api/upload-pdf-cas")
                        .with(csrf())
                        .file(file)
                        .file(password)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCASDetailsId", is(100)));

        verify(portfolioAPI).convertPdfCasToJson(any(MultipartFile.class), eq("pass123"));
        verify(portfolioAPI).uploadFromDto(mockCasDto);
    }
}
