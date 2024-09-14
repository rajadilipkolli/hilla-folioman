package com.example.application.portfolio.service;

import com.example.application.portfolio.models.UserFolioDTO;
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
        return folios.stream()
                .flatMap(folio -> folio.schemes().stream())
                .flatMap(scheme -> scheme.transactions().stream())
                .count();
    }
}
