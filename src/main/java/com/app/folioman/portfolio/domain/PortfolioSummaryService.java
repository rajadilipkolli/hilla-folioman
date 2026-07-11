package com.app.folioman.portfolio.domain;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import com.app.folioman.portfolio.domain.models.projection.PortfolioValueDateProjection;
import com.app.folioman.portfolio.rest.dtos.CategoryDTO;
import com.app.folioman.portfolio.rest.dtos.ChangeDTO;
import com.app.folioman.portfolio.rest.dtos.FolioSummaryDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioSummaryDTO;
import com.app.folioman.portfolio.rest.dtos.SchemeSummaryDTO;
import com.app.folioman.portfolio.rest.dtos.XirrDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PortfolioSummaryService {

    private final UserCASDetailsRepository userCASDetailsRepository;
    private final SchemeValueRepository schemeValueRepository;
    private final FolioSchemeRepository folioSchemeRepository;
    private final UserPortfolioValueRepository userPortfolioValueRepository;
    private final MfSchemeService mfSchemeService;
    private final MFNavService mfNavService;

    PortfolioSummaryService(
            UserCASDetailsRepository userCASDetailsRepository,
            SchemeValueRepository schemeValueRepository,
            FolioSchemeRepository folioSchemeRepository,
            UserPortfolioValueRepository userPortfolioValueRepository,
            MfSchemeService mfSchemeService,
            MFNavService mfNavService) {
        this.userCASDetailsRepository = userCASDetailsRepository;
        this.schemeValueRepository = schemeValueRepository;
        this.folioSchemeRepository = folioSchemeRepository;
        this.userPortfolioValueRepository = userPortfolioValueRepository;
        this.mfSchemeService = mfSchemeService;
        this.mfNavService = mfNavService;
    }

    public Optional<PortfolioSummaryDTO> getPortfolioSummary(Long casId, String userEmail) {
        Optional<UserCasDetailsEntity> casOpt = userCASDetailsRepository.findUserCasDetailsEntityById(casId);

        if (casOpt.isEmpty()
                || casOpt.get().getInvestorInfoEntity() == null
                || !userEmail.equals(casOpt.get().getInvestorInfoEntity().getEmail())) {
            return Optional.empty();
        }

        UserCasDetailsEntity cas = casOpt.get();

        List<FolioData> allFolioData = new ArrayList<>();
        Set<Long> amfiCodes = cas.getFolios().stream()
                .flatMap(f -> f.getSchemes().stream())
                .map(UserSchemeDetailsEntity::getAmfi)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Batch fetch metadata and navs
        Map<Long, MFSchemeProjection> schemeMetadata =
                mfSchemeService.findByAmfiCodeIn(new ArrayList<>(amfiCodes)).stream()
                        .collect(Collectors.toMap(MFSchemeProjection::getAmfiCode, Function.identity(), (a, b) -> a));

        Map<Long, List<MFSchemeNavProjection>> navMap = mfNavService.getLastTwoNavsForSchemes(amfiCodes);

        for (UserFolioDetailsEntity folio : cas.getFolios()) {
            for (UserSchemeDetailsEntity schemeDetails : folio.getSchemes()) {
                Optional<SchemeValueEntity> schemeValueOpt =
                        schemeValueRepository.findFirstByUserSchemeDetailsEntity_IdOrderByDateDesc(
                                schemeDetails.getId());

                if (schemeValueOpt.isEmpty()) continue;

                SchemeValueEntity schemeValue = schemeValueOpt.get();
                if (schemeValue.getBalance() == null || schemeValue.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                    continue; // Skip zero balance
                }

                BigDecimal xirr = folioSchemeRepository
                        .findByUserSchemeDetails_Id(schemeDetails.getId())
                        .map(FolioSchemeEntity::getXirr)
                        .orElse(null);

                allFolioData.add(new FolioData(folio.getFolio(), schemeDetails, schemeValue, xirr));
            }
        }

        Map<Long, List<FolioData>> groupedByAmfi = allFolioData.stream()
                .filter(fd -> fd.schemeDetails().getAmfi() != null)
                .collect(Collectors.groupingBy(fd -> fd.schemeDetails().getAmfi()));

        List<SchemeSummaryDTO> schemeSummaries = new ArrayList<>();
        BigDecimal portTotalInvested = BigDecimal.ZERO;
        BigDecimal portTotalValue = BigDecimal.ZERO;
        BigDecimal portTotalChangeA = BigDecimal.ZERO;
        BigDecimal portTotalChangeD = BigDecimal.ZERO;

        for (Map.Entry<Long, List<FolioData>> entry : groupedByAmfi.entrySet()) {
            Long amfiCode = entry.getKey();
            List<FolioData> folioDataList = entry.getValue();

            BigDecimal schemeInvested = BigDecimal.ZERO;
            BigDecimal schemeUnits = BigDecimal.ZERO;
            BigDecimal schemeValue = BigDecimal.ZERO;
            BigDecimal schemeAvgNav = null;

            List<FolioSummaryDTO> folioSummaries = new ArrayList<>();

            for (FolioData fd : folioDataList) {
                BigDecimal invested = fd.schemeValue().getInvested() != null
                        ? fd.schemeValue().getInvested()
                        : BigDecimal.ZERO;
                BigDecimal units =
                        fd.schemeValue().getBalance() != null ? fd.schemeValue().getBalance() : BigDecimal.ZERO;
                BigDecimal value =
                        fd.schemeValue().getValue() != null ? fd.schemeValue().getValue() : BigDecimal.ZERO;

                schemeInvested = schemeInvested.add(invested);
                schemeUnits = schemeUnits.add(units);
                schemeValue = schemeValue.add(value);

                Double avgNav = fd.schemeValue().getAvgNav() != null
                        ? fd.schemeValue().getAvgNav().doubleValue()
                        : null;

                folioSummaries.add(new FolioSummaryDTO(
                        fd.folioNumber(), invested.doubleValue(), units.doubleValue(), value.doubleValue(), avgNav));
            }

            if (schemeUnits.compareTo(BigDecimal.ZERO) > 0) {
                schemeAvgNav = schemeInvested.divide(schemeUnits, 4, RoundingMode.HALF_UP);
            }

            MFSchemeProjection meta = schemeMetadata.get(amfiCode);
            String mainCat = meta != null
                            && meta.getMfSchemeTypeEntity() != null
                            && meta.getMfSchemeTypeEntity().getCategory() != null
                    ? meta.getMfSchemeTypeEntity().getCategory()
                    : "Unknown";
            String subCat = meta != null
                            && meta.getMfSchemeTypeEntity() != null
                            && meta.getMfSchemeTypeEntity().getSubCategory() != null
                    ? meta.getMfSchemeTypeEntity().getSubCategory()
                    : "Unknown";
            String rta = meta != null && meta.getRta() != null ? meta.getRta() : "Unknown";
            String plan = meta != null && meta.getPlan() != null ? meta.getPlan() : "Unknown";
            String metaIsin = meta != null ? meta.getIsin() : null;
            String isin = metaIsin != null
                    ? metaIsin
                    : (folioDataList.get(0).schemeDetails().getIsin() != null
                            ? folioDataList.get(0).schemeDetails().getIsin()
                            : amfiCode.toString());
            String name = folioDataList.get(0).schemeDetails().getScheme();

            List<MFSchemeNavProjection> navs = navMap.getOrDefault(amfiCode, List.of());
            BigDecimal nav0 = !navs.isEmpty() ? navs.get(0).nav() : BigDecimal.ZERO;
            BigDecimal nav1 = navs.size() > 1 ? navs.get(1).nav() : nav0;
            LocalDate navDate = !navs.isEmpty() ? navs.get(0).navDate() : LocalDate.now();

            BigDecimal schemeChangeA = schemeValue.subtract(schemeInvested);
            BigDecimal schemeChangeD = schemeValue.subtract(schemeUnits.multiply(nav1));

            BigDecimal schemeChangePctA = BigDecimal.ZERO;
            if (schemeInvested.compareTo(BigDecimal.ZERO) > 0) {
                schemeChangePctA =
                        schemeChangeA.multiply(new BigDecimal("100")).divide(schemeInvested, 2, RoundingMode.HALF_UP);
            }

            BigDecimal schemeChangePctD = BigDecimal.ZERO;
            BigDecimal prevValue = schemeUnits.multiply(nav1);
            if (prevValue.compareTo(BigDecimal.ZERO) > 0) {
                schemeChangePctD =
                        schemeChangeD.multiply(new BigDecimal("100")).divide(prevValue, 2, RoundingMode.HALF_UP);
            }

            BigDecimal schemeXirr = folioDataList.get(0).xirr();

            schemeSummaries.add(new SchemeSummaryDTO(
                    isin,
                    name,
                    schemeXirr != null ? schemeXirr.doubleValue() : null,
                    new CategoryDTO(mainCat, subCat),
                    rta,
                    plan,
                    nav0.doubleValue(),
                    nav1.doubleValue(),
                    navDate,
                    schemeInvested.doubleValue(),
                    schemeUnits.doubleValue(),
                    schemeValue.doubleValue(),
                    schemeAvgNav != null ? schemeAvgNav.doubleValue() : null,
                    new ChangeDTO(schemeChangeD.doubleValue(), schemeChangeA.doubleValue()),
                    new ChangeDTO(schemeChangePctD.doubleValue(), schemeChangePctA.doubleValue()),
                    folioSummaries));

            portTotalInvested = portTotalInvested.add(schemeInvested);
            portTotalValue = portTotalValue.add(schemeValue);
            portTotalChangeA = portTotalChangeA.add(schemeChangeA);
            portTotalChangeD = portTotalChangeD.add(schemeChangeD);
        }

        BigDecimal portChangePctA = BigDecimal.ZERO;
        if (portTotalInvested.compareTo(BigDecimal.ZERO) > 0) {
            portChangePctA =
                    portTotalChangeA.multiply(new BigDecimal("100")).divide(portTotalInvested, 2, RoundingMode.HALF_UP);
        }

        BigDecimal portChangePctD = BigDecimal.ZERO;
        BigDecimal portPrevValue = portTotalValue.subtract(portTotalChangeD);
        if (portPrevValue.compareTo(BigDecimal.ZERO) > 0) {
            portChangePctD =
                    portTotalChangeD.multiply(new BigDecimal("100")).divide(portPrevValue, 2, RoundingMode.HALF_UP);
        }

        Optional<PortfolioValueDateProjection> portValueOpt =
                userPortfolioValueRepository.getLatestPortfolioValueByCasId(casId);

        BigDecimal finalPortValue = portTotalValue;
        BigDecimal finalPortInvested = portTotalInvested;
        LocalDate finalDate = LocalDate.now();
        BigDecimal currentXirr = null;
        BigDecimal overallXirr = null;

        if (portValueOpt.isPresent()) {
            PortfolioValueDateProjection p = portValueOpt.get();
            if (p.getValue() != null) finalPortValue = p.getValue();
            if (p.getDate() != null) finalDate = p.getDate();
            if (p.getXirr() != null) overallXirr = p.getXirr();
        }

        PortfolioSummaryDTO responseDTO = new PortfolioSummaryDTO(
                finalPortInvested.doubleValue(),
                finalPortValue.doubleValue(),
                new XirrDTO(
                        currentXirr != null ? currentXirr.doubleValue() : null,
                        overallXirr != null ? overallXirr.doubleValue() : null),
                new ChangeDTO(portTotalChangeD.doubleValue(), portTotalChangeA.doubleValue()),
                new ChangeDTO(portChangePctD.doubleValue(), portChangePctA.doubleValue()),
                finalDate,
                schemeSummaries);

        return Optional.of(responseDTO);
    }

    private record FolioData(
            String folioNumber,
            UserSchemeDetailsEntity schemeDetails,
            SchemeValueEntity schemeValue,
            @org.jspecify.annotations.Nullable BigDecimal xirr) {}
}
