package com.app.folioman.mfschemes.mapper;

import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.models.response.SchemeNAVDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SchemeNAVDataDtoToEntityMapper {

    @Mapping(target = "mfScheme", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "navDate", source = "date")
    @Mapping(target = "mfScheme.amfiCode", source = "schemeId")
    MFSchemeNav schemeNAVDataDTOToEntity(SchemeNAVDataDTO schemeNAVDataDTO);
}
