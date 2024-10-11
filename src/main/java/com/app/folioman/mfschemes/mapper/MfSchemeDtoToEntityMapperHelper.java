package com.app.folioman.mfschemes.mapper;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.service.MFSchemeTypeService;
import com.app.folioman.mfschemes.service.MfAmcService;
import com.app.folioman.mfschemes.util.SchemeConstants;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class MfSchemeDtoToEntityMapperHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeDtoToEntityMapperHelper.class);

    // Define the regular expressions
    private static final Pattern TYPE_CATEGORY_SUBCATEGORY_PATTERN =
            Pattern.compile("^([^()]+)\\(([^()]+)\\s*-\\s*([^()]+)\\)$");

    private final MFSchemeTypeService mFSchemeTypeService;
    private final MfAmcService mfAmcService;
    private final ReentrantLock reentrantLock = new ReentrantLock();

    public MfSchemeDtoToEntityMapperHelper(MFSchemeTypeService mFSchemeTypeService, MfAmcService mfAmcService) {
        this.mFSchemeTypeService = mFSchemeTypeService;
        this.mfAmcService = mfAmcService;
    }

    @AfterMapping
    void updateMFScheme(MFSchemeDTO mfSchemeDTO, @MappingTarget MfFundScheme mfScheme) {
        MFSchemeNav mfSchemeNav = new MFSchemeNav();
        mfSchemeNav.setNav("N.A.".equals(mfSchemeDTO.nav()) ? BigDecimal.ZERO : new BigDecimal(mfSchemeDTO.nav()));
        // Use the flexible formatter to parse the date
        LocalDate parsedDate = LocalDate.parse(mfSchemeDTO.date(), SchemeConstants.FLEXIBLE_DATE_FORMATTER);
        mfSchemeNav.setNavDate(parsedDate);
        mfScheme.addSchemeNav(mfSchemeNav);

        MFSchemeType mfSchemeType = null;
        String schemeType = mfSchemeDTO.schemeType();
        Matcher matcher = TYPE_CATEGORY_SUBCATEGORY_PATTERN.matcher(schemeType);
        if (matcher.find()) {
            String type = matcher.group(1).strip();

            // Split only once, store the result in an array
            String[] splitArray = type.split("\\s+");

            // Check if there are more than 2 elements
            if (splitArray.length > 2) {
                // Join all elements except the last one using a space
                type = String.join(" ", Arrays.copyOf(splitArray, splitArray.length - 1));
            }
            String category = matcher.group(2).strip();
            String subCategory = matcher.group(3).strip();
            mfSchemeType = findOrCreateMFSchemeTypeEntity(type, category, subCategory);
        } else {
            if (!schemeType.contains("-")) {
                String type = schemeType.substring(0, schemeType.indexOf('('));
                String category = schemeType.substring(schemeType.indexOf('(') + 1, schemeType.length() - 1);
                mfSchemeType = findOrCreateMFSchemeTypeEntity(type, category, null);
            } else {
                LOGGER.error("Unable to parse schemeType :{}", schemeType);
            }
        }
        mfScheme.setMfSchemeType(mfSchemeType);
        if (mfScheme.getAmc().getId() == null) {
            mfScheme.setAmc(findOrCreateAmcEntity(mfSchemeDTO.amc()));
        }
    }

    private MfAmc findOrCreateAmcEntity(String amc) {
        return mfAmcService.findOrCreateByName(amc);
    }

    public MFSchemeType findOrCreateMFSchemeTypeEntity(String type, String category, @Nullable String subCategory) {
        MFSchemeType byTypeAndCategoryAndSubCategory =
                mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
        if (byTypeAndCategoryAndSubCategory == null) {
            reentrantLock.lock(); // Acquiring the lock
            try {
                // Double-check within the locked section
                byTypeAndCategoryAndSubCategory =
                        mFSchemeTypeService.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

                if (byTypeAndCategoryAndSubCategory == null) {
                    MFSchemeType mfSchemeType = new MFSchemeType();
                    mfSchemeType.setType(type);
                    mfSchemeType.setCategory(category);
                    mfSchemeType.setSubCategory(subCategory);
                    byTypeAndCategoryAndSubCategory = mFSchemeTypeService.saveCategory(mfSchemeType);
                }
            } finally {
                reentrantLock.unlock(); // Ensure the lock is released in the finally block
            }
        }
        return byTypeAndCategoryAndSubCategory;
    }
}
