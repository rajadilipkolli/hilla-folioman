package com.app.folioman.portfolio;

import com.app.folioman.portfolio.domain.PdfProcessingService;
import com.app.folioman.portfolio.domain.UserDetailService;
import com.app.folioman.portfolio.domain.UserTransactionDetailsService;
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
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PortfolioAPI {

    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserDetailService userDetailService;
    private final PdfProcessingService pdfProcessingService;

    public PortfolioAPI(
            UserTransactionDetailsService userTransactionDetailsService,
            UserDetailService userDetailService,
            PdfProcessingService pdfProcessingService) {
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userDetailService = userDetailService;
        this.pdfProcessingService = pdfProcessingService;
    }

    public Optional<InvestmentReturnsDTO> getInvestmentReturnsByPan(String pan) {
        return userTransactionDetailsService.getInvestmentReturnsByPan(pan);
    }

    public List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(pan);
    }

    public List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(String pan) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerYear(pan);
    }

    public UploadFileResponse upload(MultipartFile multipartFile) throws IOException {
        return userDetailService.upload(multipartFile);
    }

    public UploadFileResponse uploadFromDto(CasDTO casDTO) {
        return userDetailService.uploadFromDto(casDTO);
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {
        return userDetailService.getPortfolioByPAN(panNumber, asOfDate);
    }

    public CasDTO convertPdfCasToJson(MultipartFile pdfFile, String password) throws IOException {
        return pdfProcessingService.convertPdfCasToJson(pdfFile, password);
    }
}
