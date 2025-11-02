package com.app.folioman.mfschemes.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MfSchemeDtoToEntityMapperIT extends AbstractIntegrationTest {

    @Autowired
    private MfSchemeDtoToEntityMapper mapper;

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithNullInput_ReturnsNull() {
        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(null);
        assertNull(result);
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

        assertNotNull(result);
        assertNotNull(result.getAmc());
        // AMC resolution may use fuzzy matching / DB lookup; just ensure an AMC name exists
        assertNotNull(result.getAmc().getName());
        // amfiCode maps to Long on entity
        assertEquals(104706L, result.getAmfiCode());
        assertEquals("HSBC TAX SAVER EQUITY FUND - IDCW PAYOUT", result.getName());
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithNullFields_HandlesNullsCorrectly() {
        MFSchemeDTO dto = new MFSchemeDTO("", null, null, null, "0.00", "1970-01-01", "Open Ended(Test - Sample)");

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertNotNull(result);
        assertNull(result.getAmfiCode());
        assertNull(result.getName());
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_IgnoredFieldsRemainNull() {
        MFSchemeDTO dto = new MFSchemeDTO(
                "Test AMC", 12345L, "INA100009859", "Test Scheme", "0.00", "2020-01-01", "Open Ended(Test - Sample)");

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertNotNull(result);
        // id should be null (not persisted)
        assertNull(result.getId());
        // sid is primitive int default 0
        assertEquals(0, result.getSid());
        assertNull(result.getStartDate());
        assertNull(result.getEndDate());
        assertNull(result.getRtaCode());
        assertNull(result.getRta());
        assertNull(result.getPlan());
        assertNull(result.getAmcCode());
        assertNull(result.getVersion());
        // mapper helper creates/assigns scheme type when schemeType present
        assertNotNull(result.getMfSchemeType());
        // mapper helper also adds a nav entry when nav/date provided
        assertNotNull(result.getMfSchemeNavs());
        assertFalse(result.getMfSchemeNavs().isEmpty());
        assertNull(result.getCreatedBy());
        assertNull(result.getCreatedDate());
        assertNull(result.getLastModifiedBy());
        assertNull(result.getLastModifiedDate());
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithEmptyStrings_MapsEmptyStrings() {
        MFSchemeDTO dto = new MFSchemeDTO("", null, null, "", "0.00", "1970-01-01", "Open Ended(Test - Sample)");

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertNotNull(result);
        assertNotNull(result.getAmc());
        // AMC name may be resolved from DB/cache; ensure an AMC entity exists
        assertNotNull(result.getAmc().getName());
        // schemeCode is Long now; null maps to null amfiCode
        assertNull(result.getAmfiCode());
        assertEquals("", result.getName());
    }
}
