package com.app.folioman.portfolio.web.controller;

import com.app.folioman.portfolio.models.response.PortfolioResponse;
import com.app.folioman.portfolio.models.response.UploadFileResponse;
import com.app.folioman.portfolio.service.PdfProcessingService;
import com.app.folioman.portfolio.service.UserDetailService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@AnonymousAllowed
@Endpoint
@RestController
@Validated
public class ImportMutualFundController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportMutualFundController.class);

    private final UserDetailService userDetailService;
    private final PdfProcessingService pdfProcessingService;

    public ImportMutualFundController(UserDetailService userDetailService, PdfProcessingService pdfProcessingService) {
        this.userDetailService = userDetailService;
        this.pdfProcessingService = pdfProcessingService;
    }

    @PostMapping(value = "/api/upload-handler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadFileResponse upload(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        LOGGER.info("Received file :{} for processing", multipartFile.getOriginalFilename());
        return userDetailService.upload(multipartFile);
    }

    @PostMapping(value = "/api/upload-pdf-cas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResponse uploadPasswordProtectedCasPdf(
            @RequestPart("file") MultipartFile pdfFile, @RequestPart("password") String password) throws IOException {
        LOGGER.info("Received password-protected PDF file: {} for processing", pdfFile.getOriginalFilename());

        // First convert the PDF to CasDTO
        var casDTO = pdfProcessingService.convertPdfCasToJson(pdfFile, password);

        // Then process the CasDTO using the existing flow
        return userDetailService.uploadFromDto(casDTO);
    }

    @GetMapping("/api/portfolio/{pan}")
    public PortfolioResponse getPortfolio(
            @PathVariable("pan") @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String panNumber,
            @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    @PastOrPresent(message = "Date should be past or today")
                    LocalDate asOfDate) {
        return userDetailService.getPortfolioByPAN(panNumber, asOfDate);
    }
}
