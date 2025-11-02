package com.app.folioman.portfolio.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class CasTypeEnumTest {

    @Test
    void testValues() {
        CasTypeEnum[] values = CasTypeEnum.values();

        assertEquals(2, values.length);
        assertEquals(CasTypeEnum.DETAILED, values[0]);
        assertEquals(CasTypeEnum.SUMMARY, values[1]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DETAILED", "SUMMARY"})
    void testValueOfWithValidInput(String enumName) {
        CasTypeEnum result = CasTypeEnum.valueOf(enumName);

        assertNotNull(result);
        assertEquals(enumName, result.name());
    }

    @Test
    void testValueOfDetailed() {
        CasTypeEnum result = CasTypeEnum.valueOf("DETAILED");

        assertEquals(CasTypeEnum.DETAILED, result);
    }

    @Test
    void testValueOfSummary() {
        CasTypeEnum result = CasTypeEnum.valueOf("SUMMARY");

        assertEquals(CasTypeEnum.SUMMARY, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "detailed", "summary", "", "NULL"})
    void testValueOfWithInvalidInput(String invalidName) {
        assertThrows(IllegalArgumentException.class, () -> {
            CasTypeEnum.valueOf(invalidName);
        });
    }

    @Test
    void testValueOfWithNullInput() {
        assertThrows(NullPointerException.class, () -> {
            CasTypeEnum.valueOf(null);
        });
    }

    @ParameterizedTest
    @EnumSource(CasTypeEnum.class)
    void testName(CasTypeEnum casType) {
        String name = casType.name();

        assertNotNull(name);
        assertTrue(name.equals("DETAILED") || name.equals("SUMMARY"));
    }

    @Test
    void testDetailedName() {
        assertEquals("DETAILED", CasTypeEnum.DETAILED.name());
    }

    @Test
    void testSummaryName() {
        assertEquals("SUMMARY", CasTypeEnum.SUMMARY.name());
    }

    @ParameterizedTest
    @EnumSource(CasTypeEnum.class)
    void testOrdinal(CasTypeEnum casType) {
        int ordinal = casType.ordinal();

        assertTrue(ordinal >= 0 && ordinal < 2);
    }

    @Test
    void testDetailedOrdinal() {
        assertEquals(0, CasTypeEnum.DETAILED.ordinal());
    }

    @Test
    void testSummaryOrdinal() {
        assertEquals(1, CasTypeEnum.SUMMARY.ordinal());
    }

    @Test
    void testEnumEquality() {
        assertEquals(CasTypeEnum.DETAILED, CasTypeEnum.DETAILED);
        assertEquals(CasTypeEnum.SUMMARY, CasTypeEnum.SUMMARY);
        assertNotEquals(CasTypeEnum.DETAILED, CasTypeEnum.SUMMARY);
    }

    @Test
    void testEnumHashCode() {
        assertEquals(CasTypeEnum.DETAILED.hashCode(), CasTypeEnum.DETAILED.hashCode());
        assertEquals(CasTypeEnum.SUMMARY.hashCode(), CasTypeEnum.SUMMARY.hashCode());
    }

    @Test
    void testEnumToString() {
        assertEquals("DETAILED", CasTypeEnum.DETAILED.toString());
        assertEquals("SUMMARY", CasTypeEnum.SUMMARY.toString());
    }
}
