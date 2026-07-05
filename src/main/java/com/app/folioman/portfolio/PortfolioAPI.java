package com.app.folioman.portfolio;

import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingRequestDTO;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingResponseDTO;
import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface PortfolioAPI {

    Optional<InvestmentReturnsDTO> getInvestmentReturnsByPan(String pan);

    List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(String pan);

    List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(String pan);

    UploadFileResponse upload(MultipartFile multipartFile) throws IOException;

    UploadFileResponse uploadFromDto(CasDTO casDTO);

    PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate);

    CasDTO convertPdfCasToJson(MultipartFile pdfFile, String password) throws IOException;

    List<PortfolioSummaryProjection> getPortfolioSummariesByEmail(String email);

    CapitalGainsHarvestingResponseDTO getCapitalGainsHarvesting(String pan, CapitalGainsHarvestingRequestDTO request);
}
