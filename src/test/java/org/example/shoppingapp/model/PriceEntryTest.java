package org.example.shoppingapp.model;

import org.example.shoppingapp.model.enums.Currency;
import org.example.shoppingapp.model.enums.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PriceEntryTest {

    private static final double DELTA = 0.0001;
    private Product mockProductLiter;
    private Product mockProductKg;
    private Product mockProductPiece;
    private Product mockProductNoUnit;
    private Product mockProductZeroNormQty;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2023, 10, 27);

        mockProductLiter = mock(Product.class);
        when(mockProductLiter.getNormalizedQuantity()).thenReturn(1.0);
        when(mockProductLiter.getNormalizedUnitType()).thenReturn(UnitType.LITER);
        when(mockProductLiter.getPackageUnitInput()).thenReturn(UnitType.LITER); // For getUnitForNormalizedPrice fallback

        mockProductKg = mock(Product.class);
        when(mockProductKg.getNormalizedQuantity()).thenReturn(0.5); // e.g., 500g
        when(mockProductKg.getNormalizedUnitType()).thenReturn(UnitType.KILOGRAM);
        when(mockProductKg.getPackageUnitInput()).thenReturn(UnitType.GRAM);

        mockProductPiece = mock(Product.class);
        when(mockProductPiece.getNormalizedQuantity()).thenReturn(10.0); // e.g., 10 pieces
        when(mockProductPiece.getNormalizedUnitType()).thenReturn(UnitType.PIECE);
        when(mockProductPiece.getPackageUnitInput()).thenReturn(UnitType.PIECE);

        mockProductNoUnit = mock(Product.class);
        when(mockProductNoUnit.getNormalizedUnitType()).thenReturn(null); // Simulates product with unrecognized unit
        when(mockProductNoUnit.getPackageUnitInput()).thenReturn(null);

        mockProductZeroNormQty = mock(Product.class);
        when(mockProductZeroNormQty.getNormalizedUnitType()).thenReturn(UnitType.KILOGRAM);
        when(mockProductZeroNormQty.getNormalizedQuantity()).thenReturn(0.0); // Edge case
        when(mockProductZeroNormQty.getPackageUnitInput()).thenReturn(UnitType.GRAM);
    }

    @Test
    @DisplayName("Constructor should set fields and convert currency string")
    void constructor_SetsFieldsAndConvertsCurrency() {
        PriceEntry entry = new PriceEntry(mockProductLiter, "Lidl", testDate, 9.99, "RON");

        assertSame(mockProductLiter, entry.getProduct());
        assertEquals("Lidl", entry.getStoreName());
        assertEquals(testDate, entry.getEntryDate());
        assertEquals(9.99, entry.getPrice(), DELTA);
        assertEquals(Currency.RON, entry.getCurrency());
    }

    @Test
    @DisplayName("Constructor should throw IllegalArgumentException for invalid currency string")
    void constructor_InvalidCurrencyString_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new PriceEntry(mockProductLiter, "Lidl", testDate, 9.99, "XYZ")
        );
    }

    @ParameterizedTest
    @CsvSource({
        // Price, ExpectedPricePerUnit (assuming mockProductLiter with 1.0 normalized quantity)
        "10.0, 10.0",
        "7.50, 7.50",
        "0.0, 0.0"
    })
    @DisplayName("calculatePricePerNormalizedUnit for LITER product")
    void calculatePrice_ProductLiter(double price, double expectedPricePerUnit) {
        PriceEntry entry = new PriceEntry(mockProductLiter, "Store", testDate, price, "RON");
        assertEquals(expectedPricePerUnit, entry.getPricePerNormalizedUnit(), DELTA);
    }

    @ParameterizedTest
    @CsvSource({
        // Price, ExpectedPricePerUnit (assuming mockProductKg with 0.5 normalized quantity)
        "10.0, 20.0",  // 10.0 / 0.5
        "3.25, 6.50",  // 3.25 / 0.5
        "0.0, 0.0"
    })
    @DisplayName("calculatePricePerNormalizedUnit for KILOGRAM product (from GRAM)")
    void calculatePrice_ProductKg(double price, double expectedPricePerUnit) {
        PriceEntry entry = new PriceEntry(mockProductKg, "Store", testDate, price, "RON");
        assertEquals(expectedPricePerUnit, entry.getPricePerNormalizedUnit(), DELTA);
    }

    @ParameterizedTest
    @CsvSource({
        // Price, ExpectedPricePerUnit (assuming mockProductPiece with 10.0 normalized quantity)
        "20.0, 2.0",   // 20.0 / 10.0
        "15.0, 1.5",
        "0.0, 0.0"
    })
    @DisplayName("calculatePricePerNormalizedUnit for PIECE product")
    void calculatePrice_ProductPiece(double price, double expectedPricePerUnit) {
        PriceEntry entry = new PriceEntry(mockProductPiece, "Store", testDate, price, "RON");
        assertEquals(expectedPricePerUnit, entry.getPricePerNormalizedUnit(), DELTA);
    }

    @Test
    @DisplayName("calculatePricePerNormalizedUnit should use product price if product or normalized unit is null")
    void calculatePrice_ProductOrNormalizedUnitNull_UsesProductPrice() {
        PriceEntry entryWithNullProduct = new PriceEntry(null, "Store", testDate, 15.0, "RON");
        assertEquals(15.0, entryWithNullProduct.getPricePerNormalizedUnit(), DELTA);

        PriceEntry entryWithNoUnitProduct = new PriceEntry(mockProductNoUnit, "Store", testDate, 25.0, "RON");
        assertEquals(25.0, entryWithNoUnitProduct.getPricePerNormalizedUnit(), DELTA);
    }

    @Test
    @DisplayName("calculatePricePerNormalizedUnit should use product price if normalized quantity is zero or negative")
    void calculatePrice_NormalizedQuantityZeroOrNegative_UsesProductPrice() {
        PriceEntry entryZeroQty = new PriceEntry(mockProductZeroNormQty, "Store", testDate, 30.0, "RON");
        assertEquals(30.0, entryZeroQty.getPricePerNormalizedUnit(), DELTA);

        Product mockProductNegativeNormQty = mock(Product.class);
        when(mockProductNegativeNormQty.getNormalizedUnitType()).thenReturn(UnitType.KILOGRAM);
        when(mockProductNegativeNormQty.getNormalizedQuantity()).thenReturn(-0.5);
        PriceEntry entryNegativeQty = new PriceEntry(mockProductNegativeNormQty, "Store", testDate, 35.0, "RON");
        assertEquals(35.0, entryNegativeQty.getPricePerNormalizedUnit(), DELTA);
    }

    @Test
    @DisplayName("getUnitForNormalizedPrice should return correct unit string")
    void getUnitForNormalizedPrice_ReturnsCorrectString() {
        when(mockProductLiter.getNormalizedUnitType()).thenReturn(UnitType.LITER);
        PriceEntry entryLiter = new PriceEntry(mockProductLiter, "S", testDate, 1, "RON");
        assertEquals(UnitType.LITER.getNormalizedForm(), entryLiter.getUnitForNormalizedPrice());

        when(mockProductKg.getNormalizedUnitType()).thenReturn(UnitType.KILOGRAM);
        PriceEntry entryKg = new PriceEntry(mockProductKg, "S", testDate, 1, "RON");
        assertEquals(UnitType.KILOGRAM.getNormalizedForm(), entryKg.getUnitForNormalizedPrice());

        when(mockProductNoUnit.getNormalizedUnitType()).thenReturn(null);
        when(mockProductNoUnit.getPackageUnitInput()).thenReturn(null);
        PriceEntry entryNoUnit = new PriceEntry(mockProductNoUnit, "S", testDate, 1, "RON");
        assertEquals("pachet", entryNoUnit.getUnitForNormalizedPrice());

        Product mockProductWithPackageUnitOnly = mock(Product.class);
        when(mockProductWithPackageUnitOnly.getNormalizedUnitType()).thenReturn(null);
        when(mockProductWithPackageUnitOnly.getPackageUnitInput()).thenReturn(UnitType.GRAM);
        PriceEntry entryPkgUnitOnly = new PriceEntry(mockProductWithPackageUnitOnly, "S", testDate, 1, "RON");
        assertEquals(UnitType.GRAM.getCsvValue(), entryPkgUnitOnly.getUnitForNormalizedPrice()); // Should be "g"
    }


    @Test
    @DisplayName("Equals and HashCode contract")
    void equalsAndHashCodeContract() {
        PriceEntry entry1 = new PriceEntry(mockProductLiter, "Lidl", testDate, 9.99, "RON");
        PriceEntry entry2 = new PriceEntry(mockProductLiter, "Lidl", testDate, 9.99, "RON"); // Same
        PriceEntry entry3 = new PriceEntry(mockProductKg, "Lidl", testDate, 9.99, "RON");   // Different product
        PriceEntry entry4 = new PriceEntry(mockProductLiter, "Kauf", testDate, 9.99, "RON"); // Different store
        PriceEntry entry5 = new PriceEntry(mockProductLiter, "Lidl", testDate.plusDays(1), 9.99, "RON"); // Different date
        PriceEntry entry6 = new PriceEntry(mockProductLiter, "Lidl", testDate, 10.50, "RON"); // Different price
        PriceEntry entry7 = new PriceEntry(mockProductLiter, "Lidl", testDate, 9.99, "EUR"); // Different currency


        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());

        assertNotEquals(entry1, entry3);
        assertNotEquals(entry1, entry4);
        assertNotEquals(entry1, entry5);
        assertNotEquals(entry1, entry6);
        assertNotEquals(entry1, entry7);

        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals(new Object()));
    }

    @Test
    @DisplayName("ToString should not throw exception")
    void toString_DoesNotThrowException() {
        PriceEntry entry = new PriceEntry(mockProductLiter, "Lidl", testDate, 9.99, "RON");
        assertNotNull(entry.toString());
        assertTrue(entry.toString().startsWith("PriceEntry{"));
    }

    @Test
    @DisplayName("ToString handles null product gracefully")
    void toString_HandlesNullProduct() {
        PriceEntry entry = new PriceEntry(null, "Lidl", testDate, 9.99, "RON");
        assertNotNull(entry.toString());
        assertTrue(entry.toString().contains("product=null_product"));
    }
}