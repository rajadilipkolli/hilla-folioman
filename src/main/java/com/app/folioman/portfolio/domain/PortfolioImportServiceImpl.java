package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.PortfolioImportUseCase;
import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import java.io.IOException;
import java.io.InputStream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
class PortfolioImportServiceImpl implements PortfolioImportUseCase {

    private final UserDetailService userDetailService;
    private final PdfProcessingService pdfProcessingService;

    PortfolioImportServiceImpl(UserDetailService userDetailService, PdfProcessingService pdfProcessingService) {
        this.userDetailService = userDetailService;
        this.pdfProcessingService = pdfProcessingService;
    }

    @Override
    public UploadFileResponse upload(byte[] casJsonBytes) throws IOException {
        return userDetailService.upload(casJsonBytes);
    }

    @Override
    public UploadFileResponse uploadFromDto(CasDTO casDTO) {
        return userDetailService.uploadFromDto(casDTO);
    }

    @Override
    public CasDTO convertPdfCasToJson(InputStream pdfStream, @Nullable String originalFilename, String password)
            throws IOException {
        return pdfProcessingService.convertPdfCasToJson(pdfStream, originalFilename, password);
    }
}
