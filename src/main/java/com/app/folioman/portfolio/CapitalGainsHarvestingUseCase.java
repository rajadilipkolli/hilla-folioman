package com.app.folioman.portfolio;

import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingRequestDTO;
import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingResponseDTO;

public interface CapitalGainsHarvestingUseCase {

    CapitalGainsHarvestingResponseDTO getCapitalGainsHarvesting(
            String pan, String email, CapitalGainsHarvestingRequestDTO request);
}
