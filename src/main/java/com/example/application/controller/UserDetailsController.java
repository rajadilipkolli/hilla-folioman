package com.example.application.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Endpoint
@AnonymousAllowed
@RestController
class UserDetailsController {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsController.class);

    @PostMapping(value = "/api/upload-handler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("file") MultipartFile multipartFile)
            throws IOException {
        log.info("Received file :{} for processing", multipartFile.getOriginalFilename());
        return "SuccessFully processed";
    }
}
