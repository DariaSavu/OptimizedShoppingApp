package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.Discount;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.repository.interfaces.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryDiscountRepositoryTest {

    private DiscountRepository discountRepository;
    private Product p1, p2;
    private Discount d1, d2, d3, d4;
    private LocalDate today, yesterday, tomorrow, nextWeek;

    @BeforeEach
    void setUp() {
        discountRepository = new InMemoryDiscountRepository();
        p1 = mock(Product.class);
        when(p1.getProductId()).thenReturn("P001");
        p2 = mock(Product.class);
        when(p2.getProductId()).thenReturn("P002");

        today = LocalDate.now();
        yesterday = today.minusDays(1);
        tomorrow = today.plusDays(1);
        nextWeek = today.plusWeeks(1);

        d1 = new Discount(p1, "Lidl", yesterday, tomorrow, 10.0, yesterday);
        d2 = new Discount(p1, "Lidl", yesterday.minusDays(5), yesterday, 15.0, yesterday.minusDays(5));
        d3 = new Discount(p2, "Kaufland", tomorrow, nextWeek, 20.0, today);
        d4 = new Discount(p2, "Profi", today, nextWeek, 5.0, today);
    }

    @Test
    @DisplayName("Save should add discount")
    void save_AddsDiscount() {
        discountRepository.save(d1);
        assertEquals(1, discountRepository.findAll().size());
        assertTrue(discountRepository.findAll().contains(d1));
    }
    
    @Test
    @DisplayName("Save should throw IllegalArgumentException for null discount")
    void save_NullDiscount_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> discountRepository.save(null));
    }


    @Test
    @DisplayName("SaveAll should add multiple discounts")
    void saveAll_AddsMultipleDiscounts() {
        discountRepository.saveAll(Arrays.asList(d1, d2, d3));
        assertEquals(3, discountRepository.findAll().size());
    }
    
    @Test
    @DisplayName("SaveAll should skip null discounts in list")
    void saveAll_SkipsNullDiscountsInList() {
        discountRepository.saveAll(Arrays.asList(d1, null, d3));
        assertEquals(2, discountRepository.findAll().size());
        assertTrue(discountRepository.findAll().contains(d1));
        assertTrue(discountRepository.findAll().contains(d3));
    }


    @Test
    @DisplayName("FindAll should return all discounts")
    void findAll_ReturnsAllDiscounts() {
        assertTrue(discountRepository.findAll().isEmpty());
        discountRepository.saveAll(Arrays.asList(d1, d2));
        assertEquals(2, discountRepository.findAll().size());
    }

    @Test
    @DisplayName("FindByProductId should return correct discounts")
    void findByProductId_ReturnsCorrectDiscounts() {
        discountRepository.saveAll(Arrays.asList(d1, d2, d3, d4));
        List<Discount> p1Discounts = discountRepository.findByProductId("P001");
        assertEquals(2, p1Discounts.size());
        assertTrue(p1Discounts.containsAll(Arrays.asList(d1, d2)));

        List<Discount> p2Discounts = discountRepository.findByProductId("P002");
        assertEquals(2, p2Discounts.size());
        assertTrue(p2Discounts.containsAll(Arrays.asList(d3, d4)));
        
        assertTrue(discountRepository.findByProductId("P999").isEmpty());
        assertTrue(discountRepository.findByProductId(null).isEmpty());
    }

    @Test
    @DisplayName("FindByStoreName should return correct discounts (case-insensitive)")
    void findByStoreName_ReturnsCorrectDiscounts() {
        discountRepository.saveAll(Arrays.asList(d1, d2, d3, d4));
        List<Discount> lidlDiscounts = discountRepository.findByStoreName("Lidl");
        assertEquals(2, lidlDiscounts.size());
        assertTrue(lidlDiscounts.containsAll(Arrays.asList(d1,d2)));

        List<Discount> kauflandDiscounts = discountRepository.findByStoreName("kaufland");
        assertEquals(1, kauflandDiscounts.size());
        assertTrue(kauflandDiscounts.contains(d3));

        assertTrue(discountRepository.findByStoreName(null).isEmpty());
    }

    @Test
    @DisplayName("FindActiveOnDate should return only active discounts")
    void findActiveOnDate_ReturnsActiveDiscounts() {
        discountRepository.saveAll(Arrays.asList(d1, d2, d3, d4));
        List<Discount> activeToday = discountRepository.findActiveOnDate(today);
        assertEquals(2, activeToday.size());
        assertTrue(activeToday.contains(d1));
        assertTrue(activeToday.contains(d4));
        assertFalse(activeToday.contains(d2));
        assertFalse(activeToday.contains(d3));

        List<Discount> activeNextWeek = discountRepository.findActiveOnDate(nextWeek.minusDays(1));
        assertTrue(activeNextWeek.stream().anyMatch(d -> d.equals(d3)));
        assertTrue(activeNextWeek.stream().anyMatch(d -> d.equals(d4)));
        
        assertTrue(discountRepository.findActiveOnDate(null).isEmpty());
    }

    @Test
    @DisplayName("FindByDateRange should return discounts overlapping the range")
    void findByDateRange_ReturnsOverlappingDiscounts() {
        discountRepository.saveAll(Arrays.asList(d1, d2, d3, d4));
        List<Discount> rangeDiscounts = discountRepository.findByDateRange(yesterday, tomorrow);
        assertEquals(4, rangeDiscounts.size());
        assertTrue(rangeDiscounts.contains(d1));
        assertTrue(rangeDiscounts.contains(d2));
        assertTrue(rangeDiscounts.contains(d3));
        assertTrue(rangeDiscounts.contains(d4));

        List<Discount> rangeD3 = discountRepository.findByDateRange(tomorrow, nextWeek);
        assertEquals(3, rangeD3.size());
        assertTrue(rangeD3.contains(d3));
        assertTrue(rangeD3.contains(d4));

        assertTrue(discountRepository.findByDateRange(null, tomorrow).isEmpty());
        assertTrue(discountRepository.findByDateRange(yesterday, null).isEmpty());
    }

    @Test
    @DisplayName("DeleteAll should clear all discounts")
    void deleteAll_ClearsAllDiscounts() {
        discountRepository.saveAll(Arrays.asList(d1, d2));
        assertFalse(discountRepository.findAll().isEmpty());
        discountRepository.deleteAll();
        assertTrue(discountRepository.findAll().isEmpty());
    }
}