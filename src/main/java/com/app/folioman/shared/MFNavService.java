package com.app.folioman.shared;

import com.app.folioman.mfschemes.MFSchemeDTO;
import java.util.Optional;

public interface MFNavService {

    Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId);
}
