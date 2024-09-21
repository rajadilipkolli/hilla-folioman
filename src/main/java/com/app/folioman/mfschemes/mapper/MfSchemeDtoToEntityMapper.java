package com.app.folioman.mfschemes.mapper;

import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.models.response.MFSchemeDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = MfSchemeDtoToEntityMapperHelper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MfSchemeDtoToEntityMapper {

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "mfSchemeType", ignore = true)
    @Mapping(target = "mfSchemeNavs", ignore = true)
    @Mapping(target = "schemeNameAlias", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "amc.name", source = "amc")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "amfiCode", source = "schemeCode")
    MfFundScheme mapMFSchemeDTOToMFSchemeEntity(MFSchemeDTO mfSchemeDTO);
}
