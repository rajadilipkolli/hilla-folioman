package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeTypeProjection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
class MfSchemeServiceImpl implements MfSchemeService {

    // Use the lowest ISIN as a stable representative without preferring a security type.
    private static final String SELECT_SCHEME = """
            SELECT s.scheme_code, s.plan, s.category, sec.isin
            FROM schemes s
            LEFT JOIN LATERAL (
                SELECT isin FROM securities WHERE scheme_code = s.scheme_code ORDER BY isin LIMIT 1
            ) sec ON TRUE
            """;

    private static final RowMapper<FundDetailProjection> FUND_DETAIL_MAPPER = (rs, rowNum) ->
            new FundDetailRec(rs.getString("scheme_name"), rs.getLong("scheme_code"), rs.getString("amc"));

    private final JdbcTemplate jdbcTemplate;

    MfSchemeServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    record FundDetailRec(String schemeName, Long amfiCode, String amcName) implements FundDetailProjection {
        @Override
        public String getSchemeName() {
            return schemeName;
        }

        @Override
        public Long getAmfiCode() {
            return amfiCode;
        }

        @Override
        public String getAmcName() {
            return amcName;
        }
    }

    record MFSchemeTypeRec(String category, String subCategory) implements MFSchemeTypeProjection {
        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getSubCategory() {
            return subCategory;
        }
    }

    record MFSchemeRec(Long amfiCode, String isin, String plan, String rta, MFSchemeTypeProjection type)
            implements MFSchemeProjection {
        @Override
        public Long getAmfiCode() {
            return amfiCode;
        }

        @Override
        public String getIsin() {
            return isin;
        }

        @Override
        public String getPlan() {
            return plan;
        }

        @Override
        public String getRta() {
            return rta;
        }

        @Override
        public MFSchemeTypeProjection getMfSchemeTypeEntity() {
            return type;
        }
    }

    public Optional<com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO> getMfSchemeDTO(
            Long schemeCode, java.time.LocalDate navDate) {
        java.util.List<com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO> results = jdbcTemplate.query(
                "SELECT s.amc, s.scheme_code, sec.isin, s.scheme_name, n.nav, n.nav_date, s.category "
                        + "FROM schemes s LEFT JOIN securities sec ON s.scheme_code = sec.scheme_code "
                        + "LEFT JOIN nav n ON sec.isin = n.isin AND n.nav_date <= ? "
                        + "WHERE s.scheme_code = ? "
                        + "ORDER BY n.nav_date DESC LIMIT 1",
                (rs, rowNum) -> new com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO(
                        rs.getString("amc"),
                        rs.getLong("scheme_code"),
                        rs.getString("isin"),
                        rs.getString("scheme_name"),
                        rs.getString("nav"),
                        rs.getString("nav_date"),
                        rs.getString("category")),
                java.sql.Date.valueOf(navDate),
                schemeCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<MFSchemeProjection> findByPayOut(String isin) {
        List<MFSchemeProjection> results = jdbcTemplate.query(
                "SELECT s.scheme_code, s.plan, s.category, sec.isin FROM schemes s JOIN securities sec ON s.scheme_code = sec.scheme_code WHERE sec.isin = ?",
                (rs, rowNum) -> new MFSchemeRec(
                        rs.getLong("scheme_code"),
                        rs.getString("isin"),
                        rs.getString("plan"),
                        "UNKNOWN",
                        new MFSchemeTypeRec(rs.getString("category"), "")),
                isin);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<MFSchemeProjection> findByAmfiCode(Long amfiCode) {
        List<MFSchemeProjection> results = jdbcTemplate.query(
                SELECT_SCHEME + "WHERE s.scheme_code = ?",
                (rs, rowNum) -> new MFSchemeRec(
                        rs.getLong("scheme_code"),
                        rs.getString("isin"),
                        rs.getString("plan"),
                        "UNKNOWN",
                        new MFSchemeTypeRec(rs.getString("category"), "")),
                amfiCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<MFSchemeProjection> findByAmfiCodeIn(List<Long> amfiCodes) {
        if (amfiCodes == null || amfiCodes.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(amfiCodes.size(), "?"));
        return jdbcTemplate.query(
                SELECT_SCHEME + "WHERE s.scheme_code IN (" + placeholders + ")",
                (rs, rowNum) -> new MFSchemeRec(
                        rs.getLong("scheme_code"),
                        rs.getString("isin"),
                        rs.getString("plan"),
                        "UNKNOWN",
                        new MFSchemeTypeRec(rs.getString("category"), "")),
                amfiCodes.toArray());
    }

    @Override
    public List<FundDetailProjection> fetchSchemes(String scheme) {
        String query = scheme != null ? scheme.strip() : "";
        if (query.isBlank()) return List.of();

        String select = "SELECT scheme_code, scheme_name, amc FROM schemes WHERE ";
        if (query.matches("\\d{6}")) {
            return jdbcTemplate.query(select + "scheme_code = ?", FUND_DETAIL_MAPPER, Long.parseLong(query));
        }

        boolean amcSearch = query.toLowerCase(Locale.ROOT).startsWith("amc ");
        String cleanQuery = amcSearch ? query.substring(4).strip() : query;
        String pattern = "%" + cleanQuery + "%";
        String textPredicates = "LOWER(scheme_name) LIKE LOWER(?) OR LOWER(amc) LIKE LOWER(?)";
        if (amcSearch) {
            String firstWord = cleanQuery.split("\\s+", 2)[0];
            return jdbcTemplate.query(
                    select + textPredicates + " OR LOWER(amc) LIKE LOWER(?)",
                    FUND_DETAIL_MAPPER,
                    pattern,
                    pattern,
                    "%" + firstWord + "%");
        }
        return jdbcTemplate.query(select + textPredicates, FUND_DETAIL_MAPPER, pattern, pattern);
    }
}
