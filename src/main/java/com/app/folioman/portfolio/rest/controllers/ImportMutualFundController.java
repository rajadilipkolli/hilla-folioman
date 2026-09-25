package com.app.folioman.portfolio.rest.controllers;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.PortfolioAPI;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import com.app.folioman.portfolio.validation.ValidPastOrPresent;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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

@RolesAllowed("USER")
@Endpoint
@RestController
@Validated
public class ImportMutualFundController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportMutualFundController.class);

    private final PortfolioAPI portfolioAPI;

    public ImportMutualFundController(PortfolioAPI portfolioAPI) {
        this.portfolioAPI = portfolioAPI;
    }

    @PostMapping(value = "/api/upload-handler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = CacheNames.USER_PROFILE_CACHE,
                        condition =
                                "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication() != null",
                        key =
                                "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"),
                @CacheEvict(
                        cacheNames = CacheNames.SUMMARY_CACHE,
                        condition =
                                "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication() != null",
                        key =
                                "'summary_' + #result.userCASDetailsId() + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
            })
    public UploadFileResponse upload(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        LOGGER.info("Received file :{} for processing", multipartFile.getOriginalFilename());
        return portfolioAPI.upload(multipartFile);
    }

    @PostMapping(value = "/api/upload-pdf-cas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = CacheNames.USER_PROFILE_CACHE,
                        condition =
                                "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication() != null",
                        key =
                                "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"),
                @CacheEvict(
                        cacheNames = CacheNames.SUMMARY_CACHE,
                        condition =
                                "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication() != null",
                        key =
                                "'summary_' + #result.userCASDetailsId() + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
            })
    public UploadFileResponse uploadPasswordProtectedCasPdf(
            @RequestPart("file") MultipartFile pdfFile, @RequestPart("password") String password) throws IOException {
        LOGGER.info("Received password-protected PDF file: {} for processing", pdfFile.getOriginalFilename());

        // First convert the PDF to CasDTO
        var casDTO = portfolioAPI.convertPdfCasToJson(pdfFile, password);

        // Then process the CasDTO using the existing flow
        return portfolioAPI.uploadFromDto(casDTO);
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
        if (!portfolioAPI.isPanOwnedByEmail(panNumber, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return portfolioAPI.getPortfolioByPAN(
                panNumber, email, asOfDate != null ? asOfDate : LocalDate.now(ZoneId.systemDefault()));
    }
}
