package org.example.shoppingapp.model.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum UnitType {
    LITER("l", true, "l"),
    MILLILITER("ml", false, "l"),
    KILOGRAM("kg", true, "kg"),
    GRAM("g", false, "kg"),
    PIECE("buc", true, "buc");

    private final String csvValue;
    private final boolean isBaseUnit;
    private final String normalizedForm;
    private static final Logger logger = LoggerFactory.getLogger(UnitType.class);
    UnitType(String csvValue, boolean isBaseUnit, String normalizedForm) {
        this.csvValue=csvValue;
        this.isBaseUnit=isBaseUnit;
        this.normalizedForm=normalizedForm;
    }

    public static UnitType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String lowerText = text.trim().toLowerCase();
        for (UnitType unit : values()) {
            if (unit.csvValue.equals(lowerText)) {
                return unit;
            }
        }
        return null;
    }
    public String getCsvValue() {
        return csvValue;
    }

    public boolean isBaseUnit() {
        return isBaseUnit;
    }

    public String getNormalizedForm() {
        return normalizedForm;
    }

    public UnitType getBaseUnitType() {
        switch (this) {
            case MILLILITER:
                return LITER;
            case GRAM:
                return KILOGRAM;
            case LITER:
            case KILOGRAM:
            case PIECE:
            default:
                return this;
        }
    }

    public double getNormalizedValue(double quantity) {
        switch (this) {
            case MILLILITER, GRAM:
                return quantity/1000.0;
            case LITER:
            case KILOGRAM:
            case PIECE:
            default:
                return quantity;
        }
    }
}
