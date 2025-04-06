package com.app.folioman.portfolio.web.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.portfolio.TestData;
import com.app.folioman.portfolio.models.request.CasDTO;
import com.app.folioman.portfolio.models.response.UploadFileResponse;
import com.app.folioman.portfolio.service.PdfProcessingService;
import com.app.folioman.portfolio.service.UserDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ImportMutualFundController.class)
@AutoConfigureMockMvc
class ImportMutualFundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailService userDetailService;

    @MockitoBean
    private PdfProcessingService pdfProcessingService;

    @Test
    void getPortfolioInvalidPan() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCD1234EF")
                        .param("asOfDate", "2024-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
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
                .andExpect(jsonPath("$.type", is("about:blank")))
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
                .andExpect(jsonPath("$.type", is("about:blank")))
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
    void uploadPdfCasFileSuccessfully() throws Exception {
        // Prepare mock PDF file
        MockMultipartFile mockPdf = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "test pdf content".getBytes());

        // Create password part
        MockMultipartFile passwordPart = new MockMultipartFile("password", "", "text/plain", "testpassword".getBytes());

        CasDTO mockCasDTO = TestData.getCasDTO();

        UploadFileResponse uploadFileResponse = new UploadFileResponse(1, 2, 3, 4L);
        given(pdfProcessingService.convertPdfCasToJson(any(), anyString())).willReturn(mockCasDTO);
        given(userDetailService.uploadFromDto(any())).willReturn(uploadFileResponse);

        this.mockMvc
                .perform(multipart("/api/upload-pdf-cas")
                        .file(mockPdf)
                        .file(passwordPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newFolios", is(1)));

        verify(pdfProcessingService).convertPdfCasToJson(any(), eq("testpassword"));
        verify(userDetailService).uploadFromDto(eq(mockCasDTO));
    }
}
