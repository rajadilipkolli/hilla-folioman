package com.app.folioman.mfschemes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.models.response.SchemeNAVDataDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class SchemeNAVDataDtoToEntityMapperTest {

    private final SchemeNAVDataDtoToEntityMapper mapper = Mappers.getMapper(SchemeNAVDataDtoToEntityMapper.class);

    @Test
    void schemeNAVDataDTOToEntity_WithValidDTO_ShouldMapCorrectly() {
        SchemeNAVDataDTO dto = new SchemeNAVDataDTO(LocalDate.of(2023, 12, 15), "150.75", 12345L);

        MFSchemeNav result = mapper.schemeNAVDataDTOToEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getNavDate()).isEqualTo(LocalDate.of(2023, 12, 15));
        assertThat(result.getNav()).isEqualTo(new BigDecimal("150.75"));
        assertThat(result.getMfScheme()).isNotNull();
        assertThat(result.getMfScheme().getAmfiCode()).isEqualTo(12345L);
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getCreatedDate()).isNull();
        assertThat(result.getLastModifiedBy()).isNull();
        assertThat(result.getLastModifiedDate()).isNull();
    }

    @Test
    void schemeNAVDataDTOToEntity_WithNullDTO_ShouldReturnNull() {
        MFSchemeNav result = mapper.schemeNAVDataDTOToEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void schemeNAVDataDTOToEntity_WithNullFields_ShouldMapNullValues() {
        SchemeNAVDataDTO dto = new SchemeNAVDataDTO(null, null, null);

        MFSchemeNav result = mapper.schemeNAVDataDTOToEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getNavDate()).isNull();
        assertThat(result.getNav()).isNull();
        assertThat(result.getMfScheme()).isNotNull();
        assertThat(result.getMfScheme().getAmfiCode()).isNull();
    }

    @Test
    void schemeNAVDataDTOToEntity_WithMinimalValidData_ShouldCreateBasicEntity() {
        SchemeNAVDataDTO dto = new SchemeNAVDataDTO(LocalDate.of(2023, 12, 15), "150.75", 1L);

        MFSchemeNav result = mapper.schemeNAVDataDTOToEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getMfScheme()).isNotNull();
        assertThat(result.getMfScheme().getAmfiCode()).isEqualTo(1L);
        assertThat(result.getNavDate()).isEqualTo(LocalDate.of(2023, 12, 15));
        assertThat(result.getNav()).isEqualTo(new BigDecimal("150.75"));
    }
}
