package org.example.shoppingapp.model;

import org.example.shoppingapp.model.enums.UnitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UnitTypeTest {

    @ParameterizedTest
    @CsvSource({
            "l, LITER",
            "L, LITER",
            "ml, MILLILITER",
            "ML, MILLILITER",
            "kg, KILOGRAM",
            "KG, KILOGRAM",
            "g, GRAM",
            "G, GRAM",
            "buc, PIECE",
            "BUC, PIECE",
            "  l  , LITER",
            " ml\t, MILLILITER"
    })
    @DisplayName("fromString should return correct UnitType for valid CSV values")
    void fromString_ValidInput_ReturnsCorrectUnitType(String csvInput, UnitType expectedUnitType) {
        assertEquals(expectedUnitType, UnitType.fromString(csvInput));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "unknown", "liter", "gramm"})
    @DisplayName("fromString should return null for invalid or empty/null CSV values")
    void fromString_InvalidOrEmptyInput_ReturnsNull(String invalidInput) {
        assertNull(UnitType.fromString(invalidInput));
    }

    @Test
    @DisplayName("LITER properties should be correct")
    void literProperties() {
        UnitType unit = UnitType.LITER;
        assertEquals("l", unit.getCsvValue());
        assertTrue(unit.isBaseUnit());
        assertEquals("l", unit.getNormalizedForm());
        assertEquals(UnitType.LITER, unit.getBaseUnitType());
        assertEquals(100.0, unit.getNormalizedValue(100.0));
    }

    @Test
    @DisplayName("MILLILITER properties should be correct")
    void milliliterProperties() {
        UnitType unit = UnitType.MILLILITER;
        assertEquals("ml", unit.getCsvValue());
        assertFalse(unit.isBaseUnit());
        assertEquals("l", unit.getNormalizedForm());
        assertEquals(UnitType.LITER, unit.getBaseUnitType());
        assertEquals(0.5, unit.getNormalizedValue(500.0));
        assertEquals(0.001, unit.getNormalizedValue(1.0));
    }

    @Test
    @DisplayName("KILOGRAM properties should be correct")
    void kilogramProperties() {
        UnitType unit = UnitType.KILOGRAM;
        assertEquals("kg", unit.getCsvValue());
        assertTrue(unit.isBaseUnit());
        assertEquals("kg", unit.getNormalizedForm());
        assertEquals(UnitType.KILOGRAM, unit.getBaseUnitType());
        assertEquals(2.5, unit.getNormalizedValue(2.5));
    }

    @Test
    @DisplayName("GRAM properties should be correct")
    void gramProperties() {
        UnitType unit = UnitType.GRAM;
        assertEquals("g", unit.getCsvValue());
        assertFalse(unit.isBaseUnit());
        assertEquals("kg", unit.getNormalizedForm());
        assertEquals(UnitType.KILOGRAM, unit.getBaseUnitType());
        assertEquals(0.25, unit.getNormalizedValue(250.0));
        assertEquals(1.5, unit.getNormalizedValue(1500.0));
    }

    @Test
    @DisplayName("PIECE properties should be correct")
    void pieceProperties() {
        UnitType unit = UnitType.PIECE;
        assertEquals("buc", unit.getCsvValue());
        assertTrue(unit.isBaseUnit());
        assertEquals("buc", unit.getNormalizedForm());
        assertEquals(UnitType.PIECE, unit.getBaseUnitType());
        assertEquals(10.0, unit.getNormalizedValue(10.0));
    }

    @ParameterizedTest
    @EnumSource(UnitType.class)
    @DisplayName("getBaseUnitType should return a base unit for all unit types")
    void getBaseUnitType_ReturnsBaseUnit(UnitType unit) {
        UnitType baseUnit = unit.getBaseUnitType();
        assertNotNull(baseUnit);
        assertTrue(baseUnit.isBaseUnit(), "The returned base unit should itself be a base unit.");
    }

    @ParameterizedTest
    @CsvSource({
            "LITER, 1.0, 1.0",
            "MILLILITER, 1000.0, 1.0",
            "MILLILITER, 0.0, 0.0",
            "KILOGRAM, 2.5, 2.5",
            "GRAM, 500.0, 0.5",
            "GRAM, 0.0, 0.0",
            "PIECE, 5.0, 5.0"
    })
    @DisplayName("getNormalizedValue should correctly convert quantities")
    void getNormalizedValue_CorrectlyConvertsQuantities(UnitType unit, double quantity, double expectedNormalizedValue) {
        assertEquals(expectedNormalizedValue, unit.getNormalizedValue(quantity), 0.00001);
    }
}