package com.example.application.portfolio.service;

import com.example.application.portfolio.models.UserFolioDTO;
import com.example.application.portfolio.models.UserSchemeDTO;
import com.example.application.portfolio.models.UserTransactionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortfolioServiceHelper {

    private final ObjectMapper mapper;

    public PortfolioServiceHelper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T readValue(byte[] bytes, Class<T> responseClassType) throws IOException {
        return this.mapper.readValue(bytes, responseClassType);
    }

    public long countTransactionsByUserFolioDTOList(List<UserFolioDTO> folios) {
        int count = 0;
        for (UserFolioDTO folio : folios) {
            for (UserSchemeDTO schemeDTO : folio.schemes()) {
                for (UserTransactionDTO userTransactionDTO : schemeDTO.transactions()) {
                    count++;
                }
            }
        }
        return count;
    }
}
