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
        MFSchemeDTO dto = new MFSchemeDTO("Test AMC", 12345L, null, "Test Scheme", null, null, null);

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertNotNull(result);
        assertNotNull(result.getAmc());
        assertEquals("Test AMC", result.getAmc().getName());
        assertEquals("12345", result.getAmfiCode());
        assertEquals("Test Scheme", result.getName());
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithNullFields_HandlesNullsCorrectly() {
        MFSchemeDTO dto = new MFSchemeDTO(null, null, null, null, null, null, null);

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertNotNull(result);
        assertNull(result.getAmfiCode());
        assertNull(result.getName());
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_IgnoredFieldsRemainNull() {
        MFSchemeDTO dto = new MFSchemeDTO("Test AMC", 12345L, null, "Test Scheme", null, null, null);

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getSid());
        assertNull(result.getStartDate());
        assertNull(result.getEndDate());
        assertNull(result.getRtaCode());
        assertNull(result.getRta());
        assertNull(result.getPlan());
        assertNull(result.getAmcCode());
        assertNull(result.getVersion());
        assertNull(result.getMfSchemeType());
        assertNull(result.getMfSchemeNavs());
        assertNull(result.getCreatedBy());
        assertNull(result.getCreatedDate());
        assertNull(result.getLastModifiedBy());
        assertNull(result.getLastModifiedDate());
    }

    @Test
    void mapMFSchemeDTOToMfFundScheme_WithEmptyStrings_MapsEmptyStrings() {
        MFSchemeDTO dto = new MFSchemeDTO("", null, null, "", null, null, null);

        MfFundScheme result = mapper.mapMFSchemeDTOToMfFundScheme(dto);

        assertNotNull(result);
        assertNotNull(result.getAmc());
        assertEquals("", result.getAmc().getName());
        // schemeCode is Long now; null maps to null amfiCode
        assertNull(result.getAmfiCode());
        assertEquals("", result.getName());
    }
}
