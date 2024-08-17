package com.example.application.mfschemes.mapper;

import com.example.application.mfschemes.MFSchemeDTO;
import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.mfschemes.entities.MFSchemeNav;
import com.example.application.mfschemes.entities.MFSchemeType;
import com.example.application.mfschemes.repository.MFSchemeTypeRepository;
import com.example.application.mfschemes.util.SchemeConstants;
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
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class MfSchemeDtoToEntityMapperHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeDtoToEntityMapperHelper.class);

    // Define the regular expressions
    private static final Pattern TYPE_CATEGORY_SUBCATEGORY_PATTERN =
            Pattern.compile("^([^()]+)\\(([^()]+)\\s*-\\s*([^()]+)\\)$");

    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final TransactionTemplate transactionTemplate;
    private final ReentrantLock lock = new ReentrantLock();

    public MfSchemeDtoToEntityMapperHelper(
            MFSchemeTypeRepository mfSchemeTypeRepository, TransactionTemplate transactionTemplate) {
        this.mfSchemeTypeRepository = mfSchemeTypeRepository;
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        this.transactionTemplate = transactionTemplate;
    }

    @AfterMapping
    void updateMFScheme(MFSchemeDTO scheme, @MappingTarget MFScheme mfScheme) {
        MFSchemeNav mfSchemeNav = new MFSchemeNav();
        mfSchemeNav.setNav(
                "N.A.".equals(scheme.nav()) ? BigDecimal.ZERO : BigDecimal.valueOf(Double.parseDouble(scheme.nav())));
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

    MFSchemeType findOrCreateMFSchemeTypeEntity(String type, String category, @Nullable String subCategory) {
        MFSchemeType byTypeAndCategoryAndSubCategory =
                mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
        if (byTypeAndCategoryAndSubCategory == null) {
            lock.lock(); // Acquiring the lock
            try {
                // Double-check within the locked section
                byTypeAndCategoryAndSubCategory =
                        mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);

                if (byTypeAndCategoryAndSubCategory == null) {
                    MFSchemeType mfSchemeType = new MFSchemeType();
                    mfSchemeType.setType(type);
                    mfSchemeType.setCategory(category);
                    mfSchemeType.setSubCategory(subCategory);
                    byTypeAndCategoryAndSubCategory =
                            transactionTemplate.execute(status -> mfSchemeTypeRepository.save(mfSchemeType));
                }
            } finally {
                lock.unlock(); // Ensure the lock is released in the finally block
            }
        }
        return byTypeAndCategoryAndSubCategory;
    }
}
