package com.app.folioman.mfschemes.mapper;

import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.service.MFSchemeTypeService;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.app.folioman.shared.MFSchemeDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final ReentrantLock reentrantLock = new ReentrantLock();

    public MfSchemeDtoToEntityMapperHelper(MFSchemeTypeService mFSchemeTypeService) {
        this.mFSchemeTypeService = mFSchemeTypeService;
    }

    @AfterMapping
    void updateMFScheme(MFSchemeDTO scheme, @MappingTarget MfFundScheme mfScheme) {
        MFSchemeNav mfSchemeNav = new MFSchemeNav();
        mfSchemeNav.setNav("N.A.".equals(scheme.nav()) ? BigDecimal.ZERO : new BigDecimal(scheme.nav()));
        // Use the flexible formatter to parse the date
        LocalDate parsedDate = LocalDate.parse(scheme.date(), SchemeConstants.FLEXIBLE_DATE_FORMATTER);
        mfSchemeNav.setNavDate(parsedDate);
        mfScheme.addSchemeNav(mfSchemeNav);

        MFSchemeType mfSchemeType = null;
        String schemeType = scheme.schemeType();
        Matcher matcher = TYPE_CATEGORY_SUBCATEGORY_PATTERN.matcher(schemeType);
        if (matcher.find()) {
            String type = matcher.group(1).strip();
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
