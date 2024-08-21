package com.example.application.mfschemes.mapper;

import com.example.application.mfschemes.entities.MFSchemeNav;
import com.example.application.mfschemes.models.response.SchemeNAVDataDTO;
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
    @Mapping(target = "mfScheme.schemeId", source = "schemeId")
    MFSchemeNav schemeNAVDataDTOToEntity(SchemeNAVDataDTO schemeNAVDataDTO);
}
