package org.example.shoppingapp.service;

import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.dto.PriceHistoryDataPointDTO;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PriceHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(PriceHistoryService.class);

    private final PriceEntryRepository priceEntryRepository;
    private final ProductRepository productRepository;

    public PriceHistoryService(PriceEntryRepository priceEntryRepository, ProductRepository productRepository) {
        this.priceEntryRepository = priceEntryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Obține istoricul prețurilor pentru un produs sau un set de produse filtrate.
     * @param productId ID-ul produsului specific (poate fi null dacă se filtrează după categorie/brand).
     * @param storeNameOpt Numele magazinului (opțional).
     * @param categoryOpt Categoria produsului (opțional).
     * @param brandOpt Brandul produsului (opțional).
     * @param fromDateOpt Data de început a intervalului (opțional).
     * @param toDateOpt Data de sfârșit a intervalului (opțional).
     * @return O listă de PriceHistoryDataPointDTO.
     */
    public List<PriceHistoryDataPointDTO> getPriceHistory(String productId,
                                                          Optional<String> storeNameOpt,
                                                          Optional<String> categoryOpt,
                                                          Optional<String> brandOpt,
                                                          Optional<LocalDate> fromDateOpt,
                                                          Optional<LocalDate> toDateOpt) {

        logger.debug("Fetching price history for productId: {}, store: {}, category: {}, brand: {}, from: {}, to: {}",
                productId, storeNameOpt, categoryOpt, brandOpt, fromDateOpt, toDateOpt);

        Stream<PriceEntry> entriesStream = priceEntryRepository.findAll().stream();

        if (productId != null && !productId.isBlank()) {
            entriesStream = entriesStream.filter(pe -> pe.getProduct() != null && productId.equals(pe.getProduct().getProductId()));
        } else if (categoryOpt.isPresent() || brandOpt.isPresent()) {
            if (categoryOpt.isPresent()) {
                String category = categoryOpt.get().toLowerCase();
                entriesStream = entriesStream.filter(pe -> pe.getProduct() != null &&
                        pe.getProduct().getProductCategory() != null &&
                        pe.getProduct().getProductCategory().toLowerCase().equals(category));
            }
            if (brandOpt.isPresent()) {
                String brand = brandOpt.get().toLowerCase();
                entriesStream = entriesStream.filter(pe -> pe.getProduct() != null &&
                        pe.getProduct().getBrand() != null &&
                        pe.getProduct().getBrand().toLowerCase().equals(brand));
            }
        } else {
            logger.warn("Price history request without productId, category, or brand. Returning empty list.");
            return List.of();
        }

        if (storeNameOpt.isPresent()) {
            String storeName = storeNameOpt.get().toLowerCase();
            entriesStream = entriesStream.filter(pe -> pe.getStoreName() != null &&
                    pe.getStoreName().toLowerCase().equals(storeName));
        }
        if (fromDateOpt.isPresent()) {
            LocalDate fromDate = fromDateOpt.get();
            entriesStream = entriesStream.filter(pe -> !pe.getEntryDate().isBefore(fromDate));
        }
        if (toDateOpt.isPresent()) {
            LocalDate toDate = toDateOpt.get();
            entriesStream = entriesStream.filter(pe -> !pe.getEntryDate().isAfter(toDate));
        }

        List<PriceEntry> filteredEntries = entriesStream
                .sorted(Comparator.comparing(PriceEntry::getEntryDate))
                .collect(Collectors.toList());

        if (filteredEntries.isEmpty()){
            logger.info("No price entries found for the given criteria.");
        }

        return filteredEntries.stream()
                .map(pe -> new PriceHistoryDataPointDTO(
                        pe.getEntryDate(),
                        BigDecimal.valueOf(pe.getPrice()).setScale(2, RoundingMode.HALF_UP),
                        pe.getStoreName()))
                .collect(Collectors.toList());
    }
}