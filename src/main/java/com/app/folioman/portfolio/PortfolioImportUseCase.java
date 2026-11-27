package com.app.folioman.portfolio;

import com.app.folioman.portfolio.rest.dtos.CasDTO;
import com.app.folioman.portfolio.rest.dtos.UploadFileResponse;
import java.io.IOException;
import java.io.InputStream;
import org.jspecify.annotations.Nullable;

public interface PortfolioImportUseCase {

    UploadFileResponse upload(byte[] casJsonBytes) throws IOException;

    UploadFileResponse uploadFromDto(CasDTO casDTO);

    CasDTO convertPdfCasToJson(InputStream pdfStream, @Nullable String originalFilename, String password)
            throws IOException;
}
