package com.app.folioman.portfolio.web.controller;

import com.app.folioman.portfolio.models.response.PortfolioResponse;
import com.app.folioman.portfolio.models.response.UploadFileResponse;
import com.app.folioman.portfolio.service.UserDetailService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import java.io.IOException;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@AnonymousAllowed
@Endpoint
@RestController
public class ImportMutualFundController {

    private static final Logger log = LoggerFactory.getLogger(ImportMutualFundController.class);

    private final UserDetailService userDetailService;

    public ImportMutualFundController(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @PostMapping(value = "/api/upload-handler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadFileResponse upload(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        log.info("Received file :{} for processing", multipartFile.getOriginalFilename());
        return userDetailService.upload(multipartFile);
    }

    @GetMapping("/api/portfolio/{pan}")
    public PortfolioResponse getPortfolio(
            @PathVariable("pan") String panNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return userDetailService.getPortfolioByPAN(panNumber, asOfDate);
    }
}
