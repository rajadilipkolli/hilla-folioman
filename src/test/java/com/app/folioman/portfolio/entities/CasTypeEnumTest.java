package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class CasTypeEnumTest {

    @Test
    void values() {
        CasTypeEnum[] values = CasTypeEnum.values();

        assertThat(values.length).isEqualTo(2);
        assertThat(values[0]).isEqualTo(CasTypeEnum.DETAILED);
        assertThat(values[1]).isEqualTo(CasTypeEnum.SUMMARY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DETAILED", "SUMMARY"})
    void valueOfWithValidInput(String enumName) {
        CasTypeEnum result = CasTypeEnum.valueOf(enumName);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(enumName);
    }

    @Test
    void valueOfDetailed() {
        CasTypeEnum result = CasTypeEnum.valueOf("DETAILED");

        assertThat(result).isEqualTo(CasTypeEnum.DETAILED);
    }

    @Test
    void valueOfSummary() {
        CasTypeEnum result = CasTypeEnum.valueOf("SUMMARY");

        assertThat(result).isEqualTo(CasTypeEnum.SUMMARY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "detailed", "summary", "", "NULL"})
    void valueOfWithInvalidInput(String invalidName) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> CasTypeEnum.valueOf(invalidName));
    }

    @Test
    void valueOfWithNullInput() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> CasTypeEnum.valueOf(null));
    }

    @ParameterizedTest
    @EnumSource(CasTypeEnum.class)
    void name(CasTypeEnum casType) {
        String name = casType.name();

        assertThat(name).isNotNull();
        assertThat(name.equals("DETAILED") || name.equals("SUMMARY")).isTrue();
    }

    @Test
    void detailedName() {
        assertThat(CasTypeEnum.DETAILED.name()).isEqualTo("DETAILED");
    }

    @Test
    void summaryName() {
        assertThat(CasTypeEnum.SUMMARY.name()).isEqualTo("SUMMARY");
    }

    @ParameterizedTest
    @EnumSource(CasTypeEnum.class)
    void ordinal(CasTypeEnum casType) {
        int ordinal = casType.ordinal();

        assertThat(ordinal >= 0 && ordinal < 2).isTrue();
    }

    @Test
    void detailedOrdinal() {
        assertThat(CasTypeEnum.DETAILED.ordinal()).isZero();
    }

    @Test
    void summaryOrdinal() {
        assertThat(CasTypeEnum.SUMMARY.ordinal()).isOne();
    }

    @Test
    void enumEquality() {
        assertThat(CasTypeEnum.DETAILED).isEqualTo(CasTypeEnum.DETAILED);
        assertThat(CasTypeEnum.SUMMARY).isEqualTo(CasTypeEnum.SUMMARY);
        assertThat(CasTypeEnum.SUMMARY).isNotEqualTo(CasTypeEnum.DETAILED);
    }

    @Test
    void enumHashCode() {
        assertThat(CasTypeEnum.DETAILED).hasSameHashCodeAs(CasTypeEnum.DETAILED);
        assertThat(CasTypeEnum.SUMMARY).hasSameHashCodeAs(CasTypeEnum.SUMMARY);
    }

    @Test
    void enumToString() {
        assertThat(CasTypeEnum.DETAILED).hasToString("DETAILED");
        assertThat(CasTypeEnum.SUMMARY).hasToString("SUMMARY");
    }
}
