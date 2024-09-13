package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserCASDetails;
import com.example.application.portfolio.mapper.CasDetailsMapper;
import com.example.application.portfolio.models.CasDTO;
import com.example.application.portfolio.models.UserFolioDTO;
import com.example.application.portfolio.models.response.UploadFileResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserDetailService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailService.class);
    private final PortfolioServiceHelper portfolioServiceHelper;
    private final CasDetailsMapper casDetailsMapper;
    private final UserCASDetailsService userCASDetailsService;

    public UserDetailService(
            PortfolioServiceHelper portfolioServiceHelper,
            CasDetailsMapper casDetailsMapper,
            UserCASDetailsService userCASDetailsService) {
        this.portfolioServiceHelper = portfolioServiceHelper;
        this.casDetailsMapper = casDetailsMapper;
        this.userCASDetailsService = userCASDetailsService;
    }

    public UploadFileResponse upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = parseCasDTO(multipartFile);
        validateCasDTO(casDTO);
        UploadFileResponse response = processCasDTO(casDTO);
        return response;
    }

    private void validateCasDTO(CasDTO casDTO) {
        if (casDTO.investorInfo() == null) {
            throw new IllegalArgumentException("Investor information is missing!");
        }

        String email = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();
        if (email.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Email or Name invalid!");
        }

        List<UserFolioDTO> folios = casDTO.folios();
        if (CollectionUtils.isEmpty(folios)) {
            throw new IllegalArgumentException("No folios found!");
        }
    }

    private UploadFileResponse processCasDTO(CasDTO casDTO) {
        AtomicInteger newTransactions = new AtomicInteger();
        AtomicInteger newFolios = new AtomicInteger();
        AtomicInteger newSchemes = new AtomicInteger();
        UserCASDetails userCASDetails = casDetailsMapper.convert(casDTO, newFolios, newSchemes, newTransactions);
        UploadFileResponse uploadFileResponse = null;
        if (userCASDetails != null) {
            UserCASDetails savedCasDetailsEntity = userCASDetailsService.saveEntity(userCASDetails);
            uploadFileResponse = new UploadFileResponse(
                    newFolios.get(), newSchemes.get(), newTransactions.get(), savedCasDetailsEntity.getId());
        }

        return uploadFileResponse;
    }

    private CasDTO parseCasDTO(MultipartFile multipartFile) throws IOException {
        return portfolioServiceHelper.readValue(multipartFile.getBytes(), CasDTO.class);
    }
}
