package org.example.shoppingapp.repository.interfaces;

import org.example.shoppingapp.model.PriceEntry;
import java.time.LocalDate;
import java.util.List;

public interface PriceEntryRepository {
    PriceEntry save(PriceEntry priceEntry);
    List<PriceEntry> saveAll(Iterable<PriceEntry> priceEntries);
    List<PriceEntry> findAll();
    List<PriceEntry> findByProductId(String productId);
    List<PriceEntry> findByStoreName(String storeName);
    List<PriceEntry> findByEntryDate(LocalDate date);
    List<PriceEntry> findByStoreNameAndEntryDate(String storeName, LocalDate date);
    void deleteAll(); // Sau clear()
}