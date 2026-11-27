package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeTypeProjection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class MfSchemeServiceImpl implements MfSchemeService {

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
                "SELECT s.scheme_code, s.plan, s.category, sec.isin FROM schemes s LEFT JOIN securities sec ON s.scheme_code = sec.scheme_code WHERE s.scheme_code = ?",
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
        String inSql = amfiCodes.stream().map(String::valueOf).collect(Collectors.joining(","));
        return jdbcTemplate.query(
                "SELECT s.scheme_code, s.plan, s.category, sec.isin FROM schemes s LEFT JOIN securities sec ON s.scheme_code = sec.scheme_code WHERE s.scheme_code IN ("
                        + inSql + ")",
                (rs, rowNum) -> new MFSchemeRec(
                        rs.getLong("scheme_code"),
                        rs.getString("isin"),
                        rs.getString("plan"),
                        "UNKNOWN",
                        new MFSchemeTypeRec(rs.getString("category"), "")));
    }

    @Override
    public List<FundDetailProjection> fetchSchemes(String scheme) {
        String query = scheme != null ? scheme.trim() : "";
        String cleanQuery =
                query.toLowerCase().startsWith("amc ") ? query.substring(4).trim() : query;
        // Check if query is seeking AMC matching like 'amc SBI Fondes Managemnt' or 'amc sbi'
        String[] words = cleanQuery.split("\\s+");
        String firstWord = words.length > 0 ? words[0] : cleanQuery;

        return jdbcTemplate.query(
                "SELECT scheme_code, scheme_name, amc FROM schemes WHERE LOWER(scheme_name) LIKE LOWER(?) OR LOWER(amc) LIKE LOWER(?) OR LOWER(amc) LIKE LOWER(?) OR CAST(scheme_code AS TEXT) = ?",
                (rs, rowNum) ->
                        new FundDetailRec(rs.getString("scheme_name"), rs.getLong("scheme_code"), rs.getString("amc")),
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + firstWord + "%",
                query);
    }

    @Override
    public void fetchSchemeDetails(Long schemeId) {
        // Handled by external microservice syncing
    }

    @Override
    public void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode) {
        // Handled by external microservice syncing
    }

    @Override
    public List<MFSchemeProjection> fetchSchemesByRtaCode(String rtaCode) {
        return List.of(); // RTA logic deprecated
    }
}
