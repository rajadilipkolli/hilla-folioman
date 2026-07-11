package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import java.time.LocalDate;
import java.util.Comparator;
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
        String date = "";
        String nav = "";
        if (!mfFundSchemeEntity.getMfSchemeNavs().isEmpty()) {
            var latestNav = mfFundSchemeEntity.getMfSchemeNavs().stream()
                    .max(Comparator.comparing(
                            MFSchemeNavEntity::getNavDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElse(mfFundSchemeEntity.getMfSchemeNavs().getFirst());
            LocalDate localDate = latestNav.getNavDate();
            if (latestNav.getNav() != null) {
                nav = String.valueOf(latestNav.getNav());
            }
            if (localDate != null) {
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
