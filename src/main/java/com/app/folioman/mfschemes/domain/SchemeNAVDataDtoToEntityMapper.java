package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.domain.models.response.SchemeNAVDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SchemeNAVDataDtoToEntityMapper {

    @Mapping(target = "mfFundSchemeEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "navDate", source = "date")
    @Mapping(target = "mfFundSchemeEntity.amfiCode", source = "schemeId")
    MFSchemeNavEntity schemeNAVDataDTOToEntity(SchemeNAVDataDTO schemeNAVDataDTO);
}
