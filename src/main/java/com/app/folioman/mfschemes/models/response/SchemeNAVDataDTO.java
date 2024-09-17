package com.app.folioman.mfschemes.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDate;

public record SchemeNAVDataDTO(
        @JsonFormat(pattern = "dd-MM-yyyy", shape = JsonFormat.Shape.STRING) LocalDate date, String nav, Long schemeId)
        implements Serializable {

    public SchemeNAVDataDTO withSchemeId(Long schemeCode) {
        return new SchemeNAVDataDTO(date(), nav(), schemeCode);
    }
}
