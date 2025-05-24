package org.example.shoppingapp.model;

import org.example.shoppingapp.model.enums.UnitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private static final double DELTA = 0.0001; // Value for double comparisons

    @Test
    @DisplayName("Constructor should correctly set basic product details")
    void constructor_SetsBasicDetails() {
        Product product = new Product("P001", "Test Milk", "Dairy", "BrandX", 1.0, "l");
        assertEquals("P001", product.getProductId());
        assertEquals("Test Milk", product.getProductName());
        assertEquals("Dairy", product.getProductCategory());
        assertEquals("BrandX", product.getBrand());
        assertEquals(1.0, product.getPackageQuantityInput(), DELTA);
        assertEquals(UnitType.LITER, product.getPackageUnitInput());
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, l, 1.0, LITER",
            "1000.0, ml, 1.0, LITER",
            "500.0, ml, 0.5, LITER",
            "2.0, kg, 2.0, KILOGRAM",
            "1500.0, g, 1.5, KILOGRAM",
            "250.0, g, 0.25, KILOGRAM",
            "10.0, buc, 10.0, PIECE",
            "1.0, L, 1.0, LITER",
            "750.0, G, 0.75, KILOGRAM"
    })
    @DisplayName("Constructor should correctly normalize quantity and unit")
    void constructor_NormalizesCorrectly(double pkgQty, String unitStr, double expectedNormQty, UnitType expectedNormUnit) {
        Product product = new Product("P001", "Test", "Cat", "Brand", pkgQty, unitStr);

        assertEquals(pkgQty, product.getPackageQuantityInput(), DELTA);
        assertEquals(UnitType.fromString(unitStr.toLowerCase()), product.getPackageUnitInput());
        assertEquals(expectedNormQty, product.getNormalizedQuantity(), DELTA);
        assertEquals(expectedNormUnit, product.getNormalizedUnitType());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Constructor with null or empty unit string should result in null packageUnitInput and skip normalization")
    void constructor_NullOrEmptyUnitString_HandlesGracefully(String unitString) {
        Product product = new Product("P002", "Test No Unit", "Unknown", "BrandY", 5.0, unitString);

        assertNull(product.getPackageUnitInput(), "packageUnitInput should be null for null/empty unit string");
        assertEquals(5.0, product.getNormalizedQuantity(), DELTA, "normalizedQuantity should be same as packageQuantityInput");
        assertNull(product.getNormalizedUnitType(), "normalizedUnitType should be null");
    }

    @Test
    @DisplayName("Constructor with unrecognized unit string should result in null packageUnitInput and skip normalization")
    void constructor_UnrecognizedUnitString_HandlesGracefully() {
        Product product = new Product("P003", "Test Bad Unit", "Misc", "BrandZ", 3.0, "xyz");

        assertNull(product.getPackageUnitInput(), "packageUnitInput should be null for unrecognized unit string");
        assertEquals(3.0, product.getNormalizedQuantity(), DELTA, "normalizedQuantity should be same as packageQuantityInput");
        assertNull(product.getNormalizedUnitType(), "normalizedUnitType should be null");
    }


    @Test
    @DisplayName("Getters should return correct values after construction")
    void getters_ReturnCorrectValues() {
        Product product = new Product("P123", "Coffee", "Beverage", "CafeBrand", 250.0, "g");

        assertEquals("P123", product.getProductId());
        assertEquals("Coffee", product.getProductName());
        assertEquals("Beverage", product.getProductCategory());
        assertEquals("CafeBrand", product.getBrand());
        assertEquals(250.0, product.getPackageQuantityInput(), DELTA);
        assertEquals(UnitType.GRAM, product.getPackageUnitInput());
        assertEquals(0.25, product.getNormalizedQuantity(), DELTA);
        assertEquals(UnitType.KILOGRAM, product.getNormalizedUnitType());
    }

    @Test
    @DisplayName("Equals should be true for products with same productId")
    void equals_SameProductId_ReturnsTrue() {
        Product product1 = new Product("ID001", "Product A", "Cat1", "Brand1", 1, "l");
        Product product2 = new Product("ID001", "Product B", "Cat2", "Brand2", 2, "kg"); // Different details, same ID
        assertTrue(product1.equals(product2));
        assertTrue(product2.equals(product1));
    }

    @Test
    @DisplayName("Equals should be false for products with different productId")
    void equals_DifferentProductId_ReturnsFalse() {
        Product product1 = new Product("ID001", "Product A", "Cat1", "Brand1", 1, "l");
        Product product2 = new Product("ID002", "Product A", "Cat1", "Brand1", 1, "l");
        assertFalse(product1.equals(product2));
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void equals_SameInstance_ReturnsTrue() {
        Product product1 = new Product("ID001", "Product A", "Cat1", "Brand1", 1, "l");
        assertTrue(product1.equals(product1));
    }

    @Test
    @DisplayName("Equals should be false for null")
    void equals_Null_ReturnsFalse() {
        Product product1 = new Product("ID001", "Product A", "Cat1", "Brand1", 1, "l");
        assertFalse(product1.equals(null));
    }

    @Test
    @DisplayName("Equals should be false for different type")
    void equals_DifferentType_ReturnsFalse() {
        Product product1 = new Product("ID001", "Product A", "Cat1", "Brand1", 1, "l");
        Object other = new Object();
        assertFalse(product1.equals(other));
    }

    @Test
    @DisplayName("HashCode should be same for products with same productId")
    void hashCode_SameProductId_ReturnsSameHashCode() {
        Product product1 = new Product("ID001", "Product A", "Cat1", "Brand1", 1, "l");
        Product product2 = new Product("ID001", "Product B", "Cat2", "Brand2", 2, "kg");
        assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    @DisplayName("HashCode should (ideally) be different for products with different productId")
    void hashCode_DifferentProductId_IdeallyReturnsDifferentHashCode() {
        Product product1 = new Product("ID001", "Product A", "Cat1", "Brand1", 1, "l");
        Product product2 = new Product("ID002", "Product A", "Cat1", "Brand1", 1, "l");
        if (!product1.equals(product2)) {
            assertNotEquals(product1.hashCode(), product2.hashCode());
        }
    }

    @Test
    @DisplayName("ToString should not throw an exception and return a non-null string")
    void toString_ReturnsNonNullString() {
        Product product = new Product("P001", "Test Milk", "Dairy", "BrandX", 1.0, "l");
        assertNotNull(product.toString());
        assertTrue(product.toString().startsWith("Product{"));
    }

    @Test
    @DisplayName("ToString should handle null packageUnitInput gracefully")
    void toString_HandlesNullPackageUnitInput() {
        Product product = new Product("P002", "Test No Unit", "Unknown", "BrandY", 5.0, null);
        assertNotNull(product.toString());
        assertTrue(product.toString().contains("[UNKNOWN_UNIT]"));
        assertTrue(product.toString().contains("[UNKNOWN_BASE]"));
    }
}