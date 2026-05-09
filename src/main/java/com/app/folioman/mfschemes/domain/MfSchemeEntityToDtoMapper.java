package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import java.time.LocalDate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.util.StringUtils;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MfSchemeEntityToDtoMapper {

    @Mapping(target = "schemeName", source = "name")
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "nav", ignore = true)
    @Mapping(target = "schemeCode", source = "amfiCode")
    @Mapping(target = "amc", source = "amc.name")
    @Mapping(target = "schemeType", ignore = true)
    MFSchemeDTO convertEntityToDto(MfFundSchemeEntity mfScheme);

    @AfterMapping
    default MFSchemeDTO updateMFScheme(MfFundSchemeEntity mfFundSchemeEntity, @MappingTarget MFSchemeDTO mfSchemeDTO) {
        String date = null;
        String nav = null;
        if (!mfFundSchemeEntity.getMfSchemeNavs().isEmpty()) {
            LocalDate localDate =
                    mfFundSchemeEntity.getMfSchemeNavs().getFirst().getNavDate();
            nav = String.valueOf(mfFundSchemeEntity.getMfSchemeNavs().getFirst().getNav());
            if (null != localDate) {
                date = localDate.toString();
            }
        }
        MFSchemeTypeEntity mfSchemeTypeEntity = mfFundSchemeEntity.getMfSchemeTypeEntity();
        String subCategory = mfSchemeTypeEntity.getSubCategory();
        String category = mfSchemeTypeEntity.getCategory();
        String categoryAndSubCategory;
        if (StringUtils.hasText(subCategory)) {
            categoryAndSubCategory = category + " - " + subCategory;
        } else {
            categoryAndSubCategory = category;
        }
        return mfSchemeDTO.withNavAndDateAndSchemeType(
                mfSchemeTypeEntity.getType() + "(" + categoryAndSubCategory + ")", nav, date);
    }
}
