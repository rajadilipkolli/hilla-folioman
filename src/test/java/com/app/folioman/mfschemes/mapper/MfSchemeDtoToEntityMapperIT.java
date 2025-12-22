package com.app.folioman.mfschemes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class MfSchemeDtoToEntityMapperIT extends AbstractIntegrationTest {

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithNullInput_ReturnsNull() {
        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(null);
        assertThat(result).isNull();
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithValidDTO_MapsCorrectly() {
        // Provide nav, date and schemeType to match mapper helper expectations
        MFSchemeDTO dto = new MFSchemeDTO(
                "HSBC Asset Management (India) Private Ltd.",
                104706L,
                "INF336L01AZ3",
                "HSBC TAX SAVER EQUITY FUND - IDCW PAYOUT",
                "10.00",
                "2020-01-01",
                "Open Ended(Equity Scheme - ELSS)");

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAmc()).isNotNull();
        // AMC resolution may use fuzzy matching / DB lookup; just ensure an AMC name exists
        assertThat(result.getAmc().getName()).isNotNull();
        // amfiCode maps to Long on entity
        assertThat(result.getAmfiCode()).isEqualTo(104706L);
        assertThat(result.getName()).isEqualTo("HSBC TAX SAVER EQUITY FUND - IDCW PAYOUT");
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithNullFields_HandlesNullsCorrectly() {
        MFSchemeDTO dto = new MFSchemeDTO("", null, null, null, "0.00", "1970-01-01", "Open Ended(Test - Sample)");

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAmfiCode()).isNull();
        assertThat(result.getName()).isNull();
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_IgnoredFieldsRemainNull() {
        MFSchemeDTO dto = new MFSchemeDTO(
                "Test AMC", 12345L, "INA100009859", "Test Scheme", "0.00", "2020-01-01", "Open Ended(Test - Sample)");

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertThat(result).isNotNull();
        // id should be null (not persisted)
        assertThat(result.getId()).isNull();
        // sid is primitive int default 0
        assertThat(result.getSid()).isZero();
        assertThat(result.getStartDate()).isNull();
        assertThat(result.getEndDate()).isNull();
        assertThat(result.getRtaCode()).isNull();
        assertThat(result.getRta()).isNull();
        assertThat(result.getPlan()).isNull();
        assertThat(result.getAmcCode()).isNull();
        assertThat(result.getVersion()).isNull();
        // mapper helper creates/assigns scheme type when schemeType present
        assertThat(result.getMfSchemeType()).isNotNull();
        // mapper helper also adds a nav entry when nav/date provided
        assertThat(result.getMfSchemeNavs()).isNotNull();
        assertThat(result.getMfSchemeNavs()).isNotEmpty();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getCreatedDate()).isNull();
        assertThat(result.getLastModifiedBy()).isNull();
        assertThat(result.getLastModifiedDate()).isNull();
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithEmptyStrings_MapsEmptyStrings() {
        MFSchemeDTO dto = new MFSchemeDTO("", null, null, "", "0.00", "1970-01-01", "Open Ended(Test - Sample)");

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAmc()).isNotNull();
        // AMC name may be resolved from DB/cache; ensure an AMC entity exists
        assertThat(result.getAmc().getName()).isNotNull();
        // schemeCode is Long now; null maps to null amfiCode
        assertThat(result.getAmfiCode()).isNull();
        assertThat(result.getName()).isEmpty();
    }
}
