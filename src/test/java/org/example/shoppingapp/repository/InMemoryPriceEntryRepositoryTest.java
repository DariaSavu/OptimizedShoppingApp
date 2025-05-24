package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.enums.Currency;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class InMemoryPriceEntryRepositoryTest {

    private PriceEntryRepository priceEntryRepository;
    private Product p1, p2;
    private PriceEntry pe1, pe2, pe3, pe4;
    private LocalDate date1, date2;

    @BeforeEach
    void setUp() {
        priceEntryRepository = new InMemoryPriceEntryRepository();
        p1 = mock(Product.class);
        when(p1.getProductId()).thenReturn("P001");
        p2 = mock(Product.class);
        when(p2.getProductId()).thenReturn("P002");

        date1 = LocalDate.of(2023, 10, 1);
        date2 = LocalDate.of(2023, 10, 2);

        pe1 = new PriceEntry(p1, "Lidl", date1, 10.0, "RON");
        pe2 = new PriceEntry(p2, "Lidl", date1, 5.50, "RON");
        pe3 = new PriceEntry(p1, "Kaufland", date1, 10.20, "RON");
        pe4 = new PriceEntry(p1, "Lidl", date2, 9.80, "RON"); // Same product, store, different date
    }

    @Test
    @DisplayName("Save should add price entry")
    void save_AddsPriceEntry() {
        priceEntryRepository.save(pe1);
        assertEquals(1, priceEntryRepository.findAll().size());
        assertTrue(priceEntryRepository.findAll().contains(pe1));
    }
    
    @Test
    @DisplayName("Save should throw IllegalArgumentException for null entry")
    void save_NullEntry_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> priceEntryRepository.save(null));
    }

    @Test
    @DisplayName("SaveAll should add multiple price entries")
    void saveAll_AddsMultipleEntries() {
        priceEntryRepository.saveAll(Arrays.asList(pe1, pe2, pe3));
        assertEquals(3, priceEntryRepository.findAll().size());
    }
    
    @Test
    @DisplayName("SaveAll should skip null entries in list")
    void saveAll_SkipsNullEntriesInList() {
        priceEntryRepository.saveAll(Arrays.asList(pe1, null, pe2));
        assertEquals(2, priceEntryRepository.findAll().size());
        assertTrue(priceEntryRepository.findAll().contains(pe1));
        assertTrue(priceEntryRepository.findAll().contains(pe2));
    }


    @Test
    @DisplayName("FindAll should return all entries")
    void findAll_ReturnsAllEntries() {
        assertTrue(priceEntryRepository.findAll().isEmpty());
        priceEntryRepository.saveAll(Arrays.asList(pe1, pe2));
        assertEquals(2, priceEntryRepository.findAll().size());
    }

    @Test
    @DisplayName("FindByProductId should return correct entries")
    void findByProductId_ReturnsCorrectEntries() {
        priceEntryRepository.saveAll(Arrays.asList(pe1, pe2, pe3, pe4));
        List<PriceEntry> p1Entries = priceEntryRepository.findByProductId("P001");
        assertEquals(3, p1Entries.size());
        assertTrue(p1Entries.containsAll(Arrays.asList(pe1, pe3, pe4)));

        List<PriceEntry> p2Entries = priceEntryRepository.findByProductId("P002");
        assertEquals(1, p2Entries.size());
        assertTrue(p2Entries.contains(pe2));
        
        assertTrue(priceEntryRepository.findByProductId("P999").isEmpty());
        assertTrue(priceEntryRepository.findByProductId(null).isEmpty());
    }

    @Test
    @DisplayName("FindByStoreName should return correct entries (case-insensitive)")
    void findByStoreName_ReturnsCorrectEntries() {
        priceEntryRepository.saveAll(Arrays.asList(pe1, pe2, pe3, pe4));
        List<PriceEntry> lidlEntries = priceEntryRepository.findByStoreName("Lidl");
        assertEquals(3, lidlEntries.size());
        assertTrue(lidlEntries.containsAll(Arrays.asList(pe1, pe2, pe4)));

        List<PriceEntry> kauflandEntries = priceEntryRepository.findByStoreName("kaufland"); // Test case-insensitivity
        assertEquals(1, kauflandEntries.size());
        assertTrue(kauflandEntries.contains(pe3));

        assertTrue(priceEntryRepository.findByStoreName(null).isEmpty());
    }

    @Test
    @DisplayName("FindByEntryDate should return correct entries")
    void findByEntryDate_ReturnsCorrectEntries() {
        priceEntryRepository.saveAll(Arrays.asList(pe1, pe2, pe3, pe4));
        List<PriceEntry> date1Entries = priceEntryRepository.findByEntryDate(date1);
        assertEquals(3, date1Entries.size());
        assertTrue(date1Entries.containsAll(Arrays.asList(pe1, pe2, pe3)));

        List<PriceEntry> date2Entries = priceEntryRepository.findByEntryDate(date2);
        assertEquals(1, date2Entries.size());
        assertTrue(date2Entries.contains(pe4));
        
        assertTrue(priceEntryRepository.findByEntryDate(null).isEmpty());
    }
    
    @Test
    @DisplayName("FindByStoreNameAndEntryDate should return correct entries")
    void findByStoreNameAndEntryDate_ReturnsCorrectEntries() {
        priceEntryRepository.saveAll(Arrays.asList(pe1, pe2, pe3, pe4));
        
        List<PriceEntry> lidlDate1 = priceEntryRepository.findByStoreNameAndEntryDate("Lidl", date1);
        assertEquals(2, lidlDate1.size());
        assertTrue(lidlDate1.containsAll(Arrays.asList(pe1, pe2)));

        List<PriceEntry> kauflandDate1 = priceEntryRepository.findByStoreNameAndEntryDate("Kaufland", date1);
        assertEquals(1, kauflandDate1.size());
        assertTrue(kauflandDate1.contains(pe3));

        assertTrue(priceEntryRepository.findByStoreNameAndEntryDate(null, date1).isEmpty());
        assertTrue(priceEntryRepository.findByStoreNameAndEntryDate("Lidl", null).isEmpty());
    }

    @Test
    @DisplayName("DeleteAll should clear all entries")
    void deleteAll_ClearsAllEntries() {
        priceEntryRepository.saveAll(Arrays.asList(pe1, pe2));
        assertFalse(priceEntryRepository.findAll().isEmpty());
        priceEntryRepository.deleteAll();
        assertTrue(priceEntryRepository.findAll().isEmpty());
    }
}