package com.app.folioman.shared;

import com.app.folioman.mfschemes.models.response.MFSchemeDTO;
import java.util.Optional;

public interface MFNavService {

    Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId);
}
