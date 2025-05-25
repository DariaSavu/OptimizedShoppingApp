package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class InMemoryPriceEntryRepository implements PriceEntryRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryPriceEntryRepository.class);
    private final List<PriceEntry> priceEntries = Collections.synchronizedList(new ArrayList<>());

    @Override
    public PriceEntry save(PriceEntry priceEntry) {
        if (priceEntry == null) {
            logger.warn("Attempted to save a null PriceEntry.");
            throw new IllegalArgumentException("PriceEntry cannot be null.");
        }
        priceEntries.add(priceEntry);
        logger.trace("PriceEntry saved for product: {}", priceEntry.getProduct() != null ? priceEntry.getProduct().getProductId() : "N/A");
        return priceEntry;
    }

    @Override
    public List<PriceEntry> saveAll(Iterable<PriceEntry> entries) {
        List<PriceEntry> savedEntries = new ArrayList<>();
        if (entries != null) {
            for (PriceEntry entry : entries) {
                if (entry != null) {
                    savedEntries.add(save(entry));
                }
            }
        }
        return savedEntries;
    }

    @Override
    public List<PriceEntry> findAll() {
        return new ArrayList<>(priceEntries);
    }

    @Override
    public List<PriceEntry> findByProductId(String productId) {
        if (productId == null) return new ArrayList<>();
        return priceEntries.stream()
                .filter(entry -> entry.getProduct() != null && productId.equals(entry.getProduct().getProductId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PriceEntry> findByStoreName(String storeName) {
        if (storeName == null) return new ArrayList<>();
        return priceEntries.stream()
                .filter(entry -> storeName.equalsIgnoreCase(entry.getStoreName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PriceEntry> findByEntryDate(LocalDate date) {
        if (date == null) return new ArrayList<>();
        return priceEntries.stream()
                .filter(entry -> date.equals(entry.getEntryDate()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PriceEntry> findByStoreNameAndEntryDate(String storeName, LocalDate date) {
        if (storeName == null || date == null) return new ArrayList<>();
        return priceEntries.stream()
                .filter(entry -> storeName.equalsIgnoreCase(entry.getStoreName()) && date.equals(entry.getEntryDate()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        priceEntries.clear();
        logger.info("All price entries cleared.");
    }
}