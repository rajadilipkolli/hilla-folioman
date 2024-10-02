package com.app.folioman.mfschemes.service;

import com.app.folioman.shared.MFNavService;
import com.app.folioman.shared.MFSchemeDTO;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MFNavServiceDelegate implements MFNavService {

    private final MFSchemeNavService mfSchemeNavService;

    public MFNavServiceDelegate(MFSchemeNavService mfSchemeNavService) {
        this.mfSchemeNavService = mfSchemeNavService;
    }

    @Override
    public Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId) {
        return Optional.ofNullable(mfSchemeNavService.getNav(schemeId));
    }
}
