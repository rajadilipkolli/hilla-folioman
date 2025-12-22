package com.app.folioman.mfschemes.mapper;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.MfSchemeDtoToEntityMapperHelper;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = MfSchemeDtoToEntityMapperHelper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MfSchemeDtoToEntityMapper {

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "sid", ignore = true)
    @Mapping(target = "rtaCode", ignore = true)
    @Mapping(target = "rta", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "amcCode", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "mfSchemeType", ignore = true)
    @Mapping(target = "mfSchemeNavs", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "amc.name", source = "amc")
    @Mapping(target = "amfiCode", source = "schemeCode")
    @Mapping(target = "name", source = "schemeName")
    MfFundScheme mapMFSchemeDTOToMfFundScheme(MFSchemeDTO mfSchemeDTO);
}
