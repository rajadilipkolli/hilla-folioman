package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.portfolio.PortfolioAnalyticsQuery;
import com.app.folioman.portfolio.PortfolioImportUseCase;
import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import com.app.folioman.portfolio.validation.ValidPastOrPresent;
import com.app.folioman.shared.CacheNames;
import com.app.folioman.shared.PrincipalUtil;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@RolesAllowed("USER")
@Endpoint
@RestController
@Validated
public class ImportMutualFundController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportMutualFundController.class);

    private final PortfolioImportUseCase portfolioImportUseCase;
    private final PortfolioAnalyticsQuery portfolioAnalyticsQuery;
    private final @Nullable ObjectProvider<JsonMapper> jsonMapperProvider;

    public ImportMutualFundController(
            PortfolioImportUseCase portfolioImportUseCase, PortfolioAnalyticsQuery portfolioAnalyticsQuery) {
        this(portfolioImportUseCase, portfolioAnalyticsQuery, null);
    }

    @Autowired
    public ImportMutualFundController(
            PortfolioImportUseCase portfolioImportUseCase,
            PortfolioAnalyticsQuery portfolioAnalyticsQuery,
            @Nullable ObjectProvider<JsonMapper> jsonMapperProvider) {
        this.portfolioImportUseCase = portfolioImportUseCase;
        this.portfolioAnalyticsQuery = portfolioAnalyticsQuery;
        this.jsonMapperProvider = jsonMapperProvider;
    }

    @PostMapping(value = "/api/upload-handler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CacheEvict(cacheNames = CacheNames.TRANSACTION_CACHE, allEntries = true)
    public UploadFileResponse upload(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        LOGGER.info("Received file :{} for processing", multipartFile.getOriginalFilename());
        byte[] bytes = multipartFile.getBytes();
        JsonMapper jsonMapper = jsonMapperProvider != null ? jsonMapperProvider.getIfAvailable() : null;
        if (jsonMapper != null) {
            CasDTO casDTO = jsonMapper.readValue(bytes, CasDTO.class);
            verifyInvestorEmailOwnership(casDTO);
        }
        return portfolioImportUseCase.upload(bytes);
    }

    @PostMapping(value = "/api/upload-pdf-cas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CacheEvict(cacheNames = CacheNames.TRANSACTION_CACHE, allEntries = true)
    public UploadFileResponse uploadPasswordProtectedCasPdf(
            @RequestPart("file") MultipartFile pdfFile, @RequestPart("password") String password) throws IOException {
        LOGGER.info("Received password-protected PDF file: {} for processing", pdfFile.getOriginalFilename());

        // First convert the PDF to CasDTO
        var casDTO = portfolioImportUseCase.convertPdfCasToJson(
                pdfFile.getInputStream(), pdfFile.getOriginalFilename(), password);
        verifyInvestorEmailOwnership(casDTO);

        // Then process the CasDTO using the existing flow
        return portfolioImportUseCase.uploadFromDto(casDTO);
    }

    private void verifyInvestorEmailOwnership(@Nullable CasDTO casDTO) {
        if (casDTO == null
                || casDTO.investorInfo() == null
                || casDTO.investorInfo().email() == null) {
            return;
        }
        String authenticatedEmail = PrincipalUtil.getEmailFromContext();
        if (!authenticatedEmail.contains("@")) {
            return;
        }
        String casEmail = casDTO.investorInfo().email().trim();
        if (!authenticatedEmail.trim().equalsIgnoreCase(casEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "CAS investor email does not match authenticated user");
        }
    }

    @GetMapping("/api/portfolio/{pan}")
    public PortfolioResponse getPortfolio(
            @PathVariable("pan") @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
                    String panNumber,
            @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    @ValidPastOrPresent(message = "Date should be past or today")
                    @Nullable
                    LocalDate asOfDate) {
        String email = PrincipalUtil.getEmailFromContext();
        if (!portfolioAnalyticsQuery.isPanOwnedByEmail(panNumber, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return portfolioAnalyticsQuery.getPortfolioByPAN(
                panNumber, email, asOfDate != null ? asOfDate : LocalDate.now(ZoneId.systemDefault()));
    }
}
