package com.app.folioman.mfschemes.mapper;

import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.shared.MFSchemeDTO;
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
    MFSchemeDTO convertEntityToDto(MfFundScheme mfScheme);

    @AfterMapping
    default MFSchemeDTO updateMFScheme(MfFundScheme mfScheme, @MappingTarget MFSchemeDTO mfSchemeDTO) {
        String date = null;
        String nav = null;
        if (!mfScheme.getMfSchemeNavs().isEmpty()) {
            LocalDate localDate = mfScheme.getMfSchemeNavs().getFirst().getNavDate();
            nav = String.valueOf(mfScheme.getMfSchemeNavs().getFirst().getNav());
            if (null != localDate) {
                date = localDate.toString();
            }
        }
        MFSchemeType mfSchemeType = mfScheme.getMfSchemeType();
        String subCategory = mfSchemeType.getSubCategory();
        String category = mfSchemeType.getCategory();
        String categoryAndSubCategory;
        if (StringUtils.hasText(subCategory)) {
            categoryAndSubCategory = category + " - " + subCategory;
        } else {
            categoryAndSubCategory = category;
        }
        return mfSchemeDTO.withNavAndDateAndSchemeType(
                mfSchemeType.getType() + "(" + categoryAndSubCategory + ")", nav, date);
    }
}
