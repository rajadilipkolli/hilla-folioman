package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserCASDetails;
import com.example.application.portfolio.entities.UserFolioDetails;
import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.entities.UserTransactionDetails;
import com.example.application.portfolio.mapper.CasDetailsMapper;
import com.example.application.portfolio.models.CasDTO;
import com.example.application.portfolio.models.UserFolioDTO;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Map<String, Long> upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = parseCasDTO(multipartFile);
        Map<String, Long> response = processCasDTO(casDTO);
        return response;
    }

    private Map<String, Long> processCasDTO(CasDTO casDTO) {
        Map<String, Long> response = new HashMap<>();
        if (casDTO.investorInfo() != null) {
            String email = casDTO.investorInfo().email();
            String name = casDTO.investorInfo().name();
            if (email.isEmpty() || name.isEmpty()) {
                throw new IllegalArgumentException("Email or Name invalid!");
            }
            List<UserFolioDTO> folios = casDTO.folios();
            if (CollectionUtils.isEmpty(folios)) {
                throw new IllegalArgumentException("No folios found!");
            } else {
                AtomicInteger newTransactions = new AtomicInteger();
                AtomicInteger newFolios = new AtomicInteger();
                AtomicInteger newSchemes = new AtomicInteger();
                UserCASDetails userCASDetails = casDetailsMapper.convert(casDTO);
                casDTO.folios().forEach(userFolioDTO -> {
                    UserFolioDetails userFolioDetails =
                            casDetailsMapper.mapUserFolioDTOToUserFolioDetails(userFolioDTO);
                    userFolioDTO.schemes().forEach(userSchemeDTO -> {
                        UserSchemeDetails userSchemeDetails = casDetailsMapper.schemeDTOToSchemeEntity(userSchemeDTO);
                        userSchemeDTO.transactions().forEach(transactionDTO -> {
                            UserTransactionDetails userTransactionDetails =
                                    casDetailsMapper.transactionDTOToTransactionEntity(transactionDTO);
                            userSchemeDetails.addTransaction(userTransactionDetails);
                            newTransactions.getAndIncrement();
                        });
                        userFolioDetails.addScheme(userSchemeDetails);
                        newSchemes.getAndIncrement();
                    });
                    userCASDetails.addFolioEntity(userFolioDetails);
                    newFolios.getAndIncrement();
                });
                if (userCASDetails != null) {
                    UserCASDetails savedCasDetailsEntity = userCASDetailsService.saveEntity(userCASDetails);
                    response.put("num_folios", newFolios.longValue());
                    response.put("num_transactions", newTransactions.longValue());
                    response.put("num_schemes", newSchemes.longValue());
                }
            }
        }

        return response;
    }

    private CasDTO parseCasDTO(MultipartFile multipartFile) throws IOException {
        return portfolioServiceHelper.readValue(multipartFile.getBytes(), CasDTO.class);
    }
}
