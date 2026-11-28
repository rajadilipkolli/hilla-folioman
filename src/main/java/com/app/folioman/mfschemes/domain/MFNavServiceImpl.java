package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class MFNavServiceImpl implements MFNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MFNavServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationProperties applicationProperties;
    private final RestClient restClient;
    private final CircuitBreaker navApiCircuitBreaker;

    MFNavServiceImpl(JdbcTemplate jdbcTemplate, ApplicationProperties applicationProperties, RestClient restClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationProperties = applicationProperties;
        this.restClient = restClient;
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(5)
                .build();
        this.navApiCircuitBreaker = CircuitBreaker.of("mfApiNav", config);
    }

    @Override
    public MFSchemeDTO getNav(Long schemeCode) {
        return getNavOnDate(schemeCode, LocalDate.now());
    }

    @Override
    public MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate) {
        List<MFSchemeDTO> results = jdbcTemplate.query(
                "SELECT s.amc, s.scheme_code, sec.isin, s.scheme_name, n.nav, n.date as nav_date, s.category "
                        + "FROM schemes s LEFT JOIN securities sec ON s.scheme_code = sec.scheme_code "
                        + "LEFT JOIN nav n ON s.scheme_code = n.scheme_code AND n.date <= ? "
                        + "WHERE s.scheme_code = ? ORDER BY n.date DESC LIMIT 1",
                (rs, rowNum) -> new MFSchemeDTO(
                        rs.getString("amc"),
                        rs.getLong("scheme_code"),
                        rs.getString("isin"),
                        rs.getString("scheme_name"),
                        rs.getString("nav"),
                        rs.getString("nav_date"),
                        rs.getString("category")),
                java.sql.Date.valueOf(inputDate),
                schemeCode);
        if (results.isEmpty()) {
            throw new com.app.folioman.mfschemes.NavNotFoundException(
                    "Nav Not Found for schemeCode - " + schemeCode, inputDate);
        }
        MFSchemeDTO dto = results.getFirst();
        if (dto.nav() == null) {
            try {
                String isin = dto.isin();
                LOGGER.debug("ISIN value: {}", isin);
                if (isin != null && !isin.isEmpty()) {
                    String url = applicationProperties.getNav().getMfApi().getDataUrl() + "/nav/" + isin + "/"
                            + inputDate.toString();
                    Map<String, Object> navResp = CircuitBreaker.decorateSupplier(
                                    navApiCircuitBreaker,
                                    () -> restClient
                                            .get()
                                            .uri(url)
                                            .retrieve()
                                            .body(new ParameterizedTypeReference<Map<String, Object>>() {}))
                            .get();
                    if (navResp != null && navResp.get("nav") != null) {
                        return new MFSchemeDTO(
                                dto.amc(),
                                dto.schemeCode(),
                                dto.isin(),
                                dto.schemeName(),
                                String.valueOf(navResp.get("nav")),
                                String.valueOf(navResp.get("date")),
                                dto.schemeType());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Exception fetching from external API", e);
            }
            throw new NavNotFoundException("Nav Not Found for schemeCode - " + schemeCode, inputDate);
        }
        // Preserve the decimal precision of the previous NAV response.
        String decimalNav =
                decodeStoredNav(new BigDecimal(dto.nav())).setScale(5).toPlainString();
        return dto.withNavAndDateAndSchemeType(dto.schemeType(), decimalNav, dto.date());
    }

    @Override
    public MFSchemeDTO getNavByDateWithRetry(Long schemeId, LocalDate asOfDate) {
        return getNavOnDate(schemeId, asOfDate);
    }

    @Override
    public Optional<MFSchemeDTO> findTopBySchemeIdOrderByDateDesc(Long schemeId) {
        return Optional.ofNullable(getNav(schemeId));
    }

    @Override
    public Map<Long, Map<LocalDate, MFSchemeNavProjection>> getNavsForSchemesAndDates(
            Set<Long> schemeCodes, LocalDate startDate, LocalDate endDate) {
        if (schemeCodes.isEmpty()) return Map.of();

        String placeholders = String.join(",", Collections.nCopies(schemeCodes.size(), "?"));
        List<Object> args = new ArrayList<>(schemeCodes);
        args.add(startDate);
        args.add(endDate);
        return jdbcTemplate
                .query(
                        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code IN (" + placeholders
                                + ") AND date BETWEEN ? AND ?",
                        this::mapNav,
                        args.toArray())
                .stream()
                .collect(Collectors.groupingBy(
                        MFSchemeNavProjection::amfiCode,
                        Collectors.toMap(MFSchemeNavProjection::navDate, Function.identity())));
    }

    @Override
    public Map<Long, List<MFSchemeNavProjection>> getLastTwoNavsForSchemes(Set<Long> amfiCodes) {
        if (amfiCodes == null || amfiCodes.isEmpty()) return Map.of();

        String placeholders = String.join(",", Collections.nCopies(amfiCodes.size(), "?"));
        return jdbcTemplate.query("""
                        SELECT scheme_code, date, nav FROM (
                            SELECT scheme_code, date, nav,
                                   ROW_NUMBER() OVER (PARTITION BY scheme_code ORDER BY date DESC) AS nav_rank
                            FROM nav WHERE scheme_code IN (%s) AND date IS NOT NULL
                        ) ranked
                        WHERE nav_rank <= 2
                        ORDER BY scheme_code, date DESC
                        """.formatted(placeholders), this::mapNav, amfiCodes.toArray()).stream()
                .collect(Collectors.groupingBy(MFSchemeNavProjection::amfiCode));
    }

    private MFSchemeNavProjection mapNav(ResultSet rs, int rowNum) throws SQLException {
        return new MFSchemeNavProjection(
                decodeStoredNav(rs.getBigDecimal("nav")),
                rs.getObject("date", LocalDate.class),
                rs.getLong("scheme_code"));
    }

    private BigDecimal decodeStoredNav(BigDecimal storedNav) {
        // The seed database stores NAVs as integers scaled by 10,000.
        return storedNav.movePointLeft(4);
    }
}
