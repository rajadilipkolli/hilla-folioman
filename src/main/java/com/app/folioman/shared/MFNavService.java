package com.app.folioman.shared;

import java.util.Optional;

public interface MFNavService {

    Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId);
}
