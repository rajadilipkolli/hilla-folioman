package com.app.folioman.portfolio.domain;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import com.app.folioman.portfolio.config.CapitalGainsTaxProperties;
import com.app.folioman.portfolio.config.ExitLoadProperties;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingRequest;
import com.app.folioman.portfolio.domain.models.CapitalGainsHarvestingResponse;
import com.app.folioman.portfolio.domain.models.HarvestLot;
import com.app.folioman.portfolio.domain.models.HarvestRecommendation;
import com.app.folioman.portfolio.domain.models.HarvestSummary;
import com.app.folioman.portfolio.domain.models.projection.PortfolioDetailsProjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CapitalGainsHarvestingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapitalGainsHarvestingService.class);

    private final UserCASDetailsRepository userCASDetailsRepository;
    private final UserTransactionDetailsRepository userTransactionDetailsRepository;
    private final MfSchemeService mfSchemeService;
    private final MFNavService navService;
    private final CapitalGainsTaxProperties taxProperties;
    private final ExitLoadProperties exitLoadProperties;

    CapitalGainsHarvestingService(
            UserCASDetailsRepository userCASDetailsRepository,
            UserTransactionDetailsRepository userTransactionDetailsRepository,
            MfSchemeService mfSchemeService,
            MFNavService navService,
            CapitalGainsTaxProperties taxProperties,
            ExitLoadProperties exitLoadProperties) {
        this.userCASDetailsRepository = userCASDetailsRepository;
        this.userTransactionDetailsRepository = userTransactionDetailsRepository;
        this.mfSchemeService = mfSchemeService;
        this.navService = navService;
        this.taxProperties = taxProperties;
        this.exitLoadProperties = exitLoadProperties;
    }

    public CapitalGainsHarvestingResponse generateHarvestingPlan(CapitalGainsHarvestingRequest request) {
        LOGGER.info("Generating capital gains harvesting plan for PAN: {}", request.pan());

        LocalDate evaluationDate = request.asOfDate() != null ? request.asOfDate() : LocalDate.now();
        List<PortfolioDetailsProjection> holdings =
                userCASDetailsRepository.getPortfolioDetails(request.pan(), evaluationDate);
        if (holdings == null || holdings.isEmpty()) {
            LOGGER.warn("No portfolio holdings found for PAN: {}", request.pan());
            return emptyResponse("No active portfolio holdings found for the given PAN.");
        }

        List<HarvestRecommendation> allRecommendations = new ArrayList<>();
        BigDecimal totalEquityLtcgRealized =
                request.existingRealizedGains() != null ? request.existingRealizedGains() : BigDecimal.ZERO;
        BigDecimal totalNonEquityLtcgRealized = BigDecimal.ZERO;

        for (PortfolioDetailsProjection holding : holdings) {
            if (holding.getSchemeId() == null) continue;

            // Filters
            if (request.schemeFilters() != null
                    && !request.schemeFilters().isEmpty()
                    && !request.schemeFilters().contains(holding.getSchemeName())) {
                continue;
            }

            try {
                Optional<MFSchemeProjection> schemeOpt = mfSchemeService.findByAmfiCode(holding.getSchemeId());
                boolean isEquity = true;
                if (schemeOpt.isPresent() && schemeOpt.get().getMfSchemeTypeEntity() != null) {
                    String category = schemeOpt.get().getMfSchemeTypeEntity().getCategory();
                    isEquity = category != null && category.toLowerCase().contains("equity");
                }

                CapitalGainsTaxProperties.TaxRuleSet rules =
                        isEquity ? taxProperties.getEquity() : taxProperties.getNonEquity();
                BigDecimal exemptionLimit = request.exemptionOverride() != null
                        ? request.exemptionOverride()
                        : rules.getAnnualLtcgExemptionLimit();
                BigDecimal currentTotalLtcg = isEquity ? totalEquityLtcgRealized : totalNonEquityLtcgRealized;

                HarvestRecommendation rec = evaluateScheme(
                        holding, request, rules, exemptionLimit, currentTotalLtcg, isEquity, evaluationDate);
                if (rec != null && rec.unitsToRedeem().compareTo(BigDecimal.ZERO) > 0) {
                    allRecommendations.add(rec);
                    if (isEquity) {
                        totalEquityLtcgRealized = totalEquityLtcgRealized.add(rec.ltcg());
                    } else {
                        totalNonEquityLtcgRealized = totalNonEquityLtcgRealized.add(rec.ltcg());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error evaluating scheme {} for harvesting", holding.getSchemeName(), e);
            }
        }

        // Sort recommendations by score descending
        allRecommendations.sort(Comparator.comparingDouble(HarvestRecommendation::recommendationScore)
                .reversed());

        return buildResponse(allRecommendations);
    }

    private @org.jspecify.annotations.Nullable HarvestRecommendation evaluateScheme(
            PortfolioDetailsProjection holding,
            CapitalGainsHarvestingRequest request,
            CapitalGainsTaxProperties.TaxRuleSet rules,
            BigDecimal exemptionLimit,
            BigDecimal totalLtcgRealized,
            boolean isEquity,
            LocalDate evaluationDate) {

        List<UserTransactionDetailsEntity> transactions =
                userTransactionDetailsRepository.findByUserSchemeDetails_IdOrderByTransactionDateAsc(
                        holding.getSchemeDetailId());

        HarvestLotTracker tracker = new HarvestLotTracker();
        for (UserTransactionDetailsEntity txn : transactions) {
            tracker.addTransaction(txn);
        }

        List<HarvestLot> openLots = tracker.getOpenLots();
        if (openLots.isEmpty()) {
            return null;
        }

        MFSchemeDTO navDto = navService.getNav(holding.getSchemeId());
        BigDecimal currentNav = navDto != null && navDto.nav() != null ? new BigDecimal(navDto.nav()) : BigDecimal.ZERO;
        if (currentNav.compareTo(BigDecimal.ZERO) <= 0) {
            return null; // Cannot evaluate without valid NAV
        }

        BigDecimal accumulatedUnits = BigDecimal.ZERO;
        BigDecimal accumulatedAmount = BigDecimal.ZERO;
        BigDecimal accumulatedLtcg = BigDecimal.ZERO;
        BigDecimal accumulatedStcg = BigDecimal.ZERO;
        BigDecimal accumulatedTax = BigDecimal.ZERO;
        BigDecimal accumulatedExitLoad = BigDecimal.ZERO;
        List<HarvestLot> consumedLots = new ArrayList<>();

        for (HarvestLot lot : openLots) {
            long holdingDays = ChronoUnit.DAYS.between(lot.acquisitionDate(), evaluationDate);
            boolean isLtcg = isEquity
                    ? (holdingDays > rules.getLongTermThresholdMonths() * 30L)
                    : (holdingDays > rules.getLongTermThresholdMonths() * 30L);

            if (isLtcg && !request.includeLtcg()) break; // FIFO blocked
            if (!isLtcg && !request.includeStcg()) break; // FIFO blocked

            boolean hasExitLoad = holdingDays <= exitLoadProperties.getApplicabilityWindowDays();
            if (hasExitLoad && !request.includeExitLoad()) break; // FIFO blocked

            BigDecimal unitsToConsume = lot.remainingUnits();

            // Check if we need to limit consumption based on target amount or exemption limit
            BigDecimal gainPerUnit = currentNav.subtract(lot.purchaseNav());

            // For simplicity in this greedy pass, consume full lot.
            // A more advanced engine would do partial lot consumption to hit exact targets.

            BigDecimal lotGain = gainPerUnit.multiply(unitsToConsume);
            BigDecimal lotExitLoad = BigDecimal.ZERO;
            if (hasExitLoad) {
                lotExitLoad = currentNav
                        .multiply(unitsToConsume)
                        .multiply(exitLoadProperties.getDefaultPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            BigDecimal lotTax = BigDecimal.ZERO;
            if (isLtcg) {
                if (lotGain.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal availableExemption = exemptionLimit.subtract(totalLtcgRealized.add(accumulatedLtcg));
                    if (availableExemption.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal taxableGain = lotGain.subtract(availableExemption);
                        if (taxableGain.compareTo(BigDecimal.ZERO) > 0) {
                            lotTax = taxableGain.multiply(rules.getLtcgTaxRate());
                        }
                    } else {
                        lotTax = lotGain.multiply(rules.getLtcgTaxRate());
                    }
                }
                accumulatedLtcg = accumulatedLtcg.add(lotGain);
            } else {
                if (lotGain.compareTo(BigDecimal.ZERO) > 0) {
                    lotTax = lotGain.multiply(rules.getStcgTaxRate());
                }
                accumulatedStcg = accumulatedStcg.add(lotGain);
            }

            accumulatedUnits = accumulatedUnits.add(unitsToConsume);
            accumulatedAmount = accumulatedAmount.add(unitsToConsume.multiply(currentNav));
            accumulatedTax = accumulatedTax.add(lotTax);
            accumulatedExitLoad = accumulatedExitLoad.add(lotExitLoad);
            consumedLots.add(lot);

            if (request.targetAmount() != null && accumulatedAmount.compareTo(request.targetAmount()) >= 0) {
                break; // Reached target
            }
        }

        if (accumulatedUnits.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        if (request.minRedemptionAmount() != null && accumulatedAmount.compareTo(request.minRedemptionAmount()) < 0) {
            return null;
        }

        double score = 100.0;
        if (accumulatedTax.compareTo(BigDecimal.ZERO) > 0) {
            score -= accumulatedTax
                            .divide(accumulatedAmount, 4, RoundingMode.HALF_UP)
                            .doubleValue()
                    * 1000;
        }
        if (accumulatedExitLoad.compareTo(BigDecimal.ZERO) > 0) {
            score -= accumulatedExitLoad
                            .divide(accumulatedAmount, 4, RoundingMode.HALF_UP)
                            .doubleValue()
                    * 1000;
        }

        return new HarvestRecommendation(
                holding.getSchemeName(),
                holding.getFolioNumber(),
                accumulatedUnits,
                accumulatedAmount,
                consumedLots,
                accumulatedStcg,
                accumulatedLtcg,
                accumulatedTax,
                accumulatedExitLoad,
                new BigDecimal(holding.getBalanceUnits().toString()).subtract(accumulatedUnits),
                score,
                "Greedy selection based on FIFO availability and tax rules");
    }

    private CapitalGainsHarvestingResponse emptyResponse(String message) {
        LOGGER.info(message);
        return new CapitalGainsHarvestingResponse(
                List.of(),
                new HarvestSummary(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO));
    }

    private CapitalGainsHarvestingResponse buildResponse(List<HarvestRecommendation> recommendations) {
        BigDecimal totalStcg = BigDecimal.ZERO;
        BigDecimal totalLtcg = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalExitLoad = BigDecimal.ZERO;
        BigDecimal totalRedemption = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO; // Would need total portfolio value to calculate accurately

        for (HarvestRecommendation rec : recommendations) {
            totalStcg = totalStcg.add(rec.stcg());
            totalLtcg = totalLtcg.add(rec.ltcg());
            totalTax = totalTax.add(rec.estimatedTax());
            totalExitLoad = totalExitLoad.add(rec.exitLoad());
            totalRedemption = totalRedemption.add(rec.redemptionAmount());
        }

        HarvestSummary summary =
                new HarvestSummary(totalStcg, totalLtcg, totalTax, totalExitLoad, totalRedemption, totalRemaining);

        return new CapitalGainsHarvestingResponse(recommendations, summary);
    }
}
