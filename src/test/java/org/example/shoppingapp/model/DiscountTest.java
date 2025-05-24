package org.example.shoppingapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscountTest {

    private static final double DELTA = 0.0001;
    private Product mockProduct;
    private LocalDate obsDate;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        mockProduct = mock(Product.class);
        when(mockProduct.getProductId()).thenReturn("P123");
        when(mockProduct.getProductName()).thenReturn("Test Product");

        obsDate = LocalDate.of(2023, 1, 1);
        startDate = LocalDate.of(2023, 10, 1);
        endDate = LocalDate.of(2023, 10, 10);
    }

    @Test
    @DisplayName("Constructor should set all fields correctly")
    void constructor_SetsAllFields() {
        Discount discount = new Discount(mockProduct, "Lidl", startDate, endDate, 10.0, obsDate);

        assertSame(mockProduct, discount.getProduct());
        assertEquals("Lidl", discount.getStoreName());
        assertEquals(startDate, discount.getStartDate());
        assertEquals(endDate, discount.getEndDate());
        assertEquals(10.0, discount.getDiscountPercentage(), DELTA);
        assertEquals(obsDate, discount.getDiscountObservationDate());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-5.0, 105.0, -0.01, 100.01})
    @DisplayName("Constructor should store discount percentage as is, even if out of 0-100 range (logs warning)")
    void constructor_StoresOutOfRangePercentage(double percentage) {
        Discount discount = new Discount(mockProduct, "Lidl", startDate, endDate, percentage, obsDate);
        assertEquals(percentage, discount.getDiscountPercentage(), DELTA);
    }

    @Test
    @DisplayName("Constructor should accept 0 and 100 for discount percentage")
    void constructor_AcceptsZeroAndHundredPercentage() {
        Discount discountZero = new Discount(mockProduct, "Lidl", startDate, endDate, 0.0, obsDate);
        assertEquals(0.0, discountZero.getDiscountPercentage(), DELTA);

        Discount discountHundred = new Discount(mockProduct, "Lidl", startDate, endDate, 100.0, obsDate);
        assertEquals(100.0, discountHundred.getDiscountPercentage(), DELTA);
    }

    @Test
    @DisplayName("isActiveOnDate should return true when checkDate is within range (inclusive)")
    void isActiveOnDate_WithinRange_ReturnsTrue() {
        Discount discount = new Discount(mockProduct, "Lidl", startDate, endDate, 10.0, obsDate);
        assertTrue(discount.isActiveOnDate(startDate));
        assertTrue(discount.isActiveOnDate(endDate));
        assertTrue(discount.isActiveOnDate(LocalDate.of(2023, 10, 5)));
    }

    @Test
    @DisplayName("isActiveOnDate should return false when checkDate is before startDate")
    void isActiveOnDate_BeforeStartDate_ReturnsFalse() {
        Discount discount = new Discount(mockProduct, "Lidl", startDate, endDate, 10.0, obsDate);
        assertFalse(discount.isActiveOnDate(startDate.minusDays(1)));
        assertFalse(discount.isActiveOnDate(LocalDate.of(2023, 9, 30)));
    }

    @Test
    @DisplayName("isActiveOnDate should return false when checkDate is after endDate")
    void isActiveOnDate_AfterEndDate_ReturnsFalse() {
        Discount discount = new Discount(mockProduct, "Lidl", startDate, endDate, 10.0, obsDate);
        assertFalse(discount.isActiveOnDate(endDate.plusDays(1)));
        assertFalse(discount.isActiveOnDate(LocalDate.of(2023, 10, 11)));
    }

    @Test
    @DisplayName("Getters should return correct values")
    void getters_ReturnCorrectValues() {
        Discount discount = new Discount(mockProduct, "Mega Image", startDate, endDate, 25.5, obsDate);
        assertSame(mockProduct, discount.getProduct());
        assertEquals("Mega Image", discount.getStoreName());
        assertEquals(startDate, discount.getStartDate());
        assertEquals(endDate, discount.getEndDate());
        assertEquals(25.5, discount.getDiscountPercentage(), DELTA);
        assertEquals(obsDate, discount.getDiscountObservationDate());
    }


    @Test
    @DisplayName("Equals and HashCode contract")
    void equalsAndHashCodeContract() {
        Product mockProduct2 = mock(Product.class);
        when(mockProduct2.getProductId()).thenReturn("P456");

        Discount d1 = new Discount(mockProduct, "StoreA", startDate, endDate, 10.0, obsDate);
        Discount d2 = new Discount(mockProduct, "StoreA", startDate, endDate, 10.0, obsDate.plusDays(1)); // Same by equals contract
        Discount d3 = new Discount(mockProduct2, "StoreA", startDate, endDate, 10.0, obsDate); // Different product
        Discount d4 = new Discount(mockProduct, "StoreB", startDate, endDate, 10.0, obsDate); // Different store
        Discount d5 = new Discount(mockProduct, "StoreA", startDate.minusDays(1), endDate, 10.0, obsDate); // Different start date
        Discount d6 = new Discount(mockProduct, "StoreA", startDate, endDate.plusDays(1), 10.0, obsDate); // Different end date
        Discount d7 = new Discount(mockProduct, "StoreA", startDate, endDate, 15.0, obsDate); // Different percentage

        assertEquals(d1, d2, "Discounts should be equal even if observation date differs");
        assertEquals(d1.hashCode(), d2.hashCode(), "Hashcodes should be equal for equal discounts");

        assertNotEquals(d1, d3);
        assertNotEquals(d1, d4);
        assertNotEquals(d1, d5);
        assertNotEquals(d1, d6);
        assertNotEquals(d1, d7);

        assertFalse(d1.equals(null));
        assertFalse(d1.equals(new Object()));
    }


    @Test
    @DisplayName("ToString should not throw exception and return a non-null string")
    void toString_ReturnsNonNullString() {
        Discount discount = new Discount(mockProduct, "Lidl", startDate, endDate, 10.0, obsDate);
        String str = discount.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("Discount{"));
        assertTrue(str.contains("product=" + mockProduct.getProductId()));
        assertTrue(str.contains("store='Lidl'"));
        assertTrue(str.contains("percentage=10.0%"));
    }

    @Test
    @DisplayName("ToString handles null product gracefully")
    void toString_HandlesNullProduct() {
        Discount discount = new Discount(null, "Lidl", startDate, endDate, 10.0, obsDate);
        String str = discount.toString();
        assertNotNull(str);
        assertTrue(str.contains("product=null_product"));
    }
}