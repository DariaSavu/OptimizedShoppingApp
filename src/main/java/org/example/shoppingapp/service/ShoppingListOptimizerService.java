package org.example.shoppingapp.service;

import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.dto.OptimizedShoppingListDTO;
import org.example.shoppingapp.model.dto.ShoppingListItemDTO;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShoppingListOptimizerService {
    private static final Logger logger = LoggerFactory.getLogger(ShoppingListOptimizerService.class);

    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;

    public ShoppingListOptimizerService(ProductRepository productRepository, PriceEntryRepository priceEntryRepository) {
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    public List<OptimizedShoppingListDTO> optimizeShoppingBasket(Map<String, Integer> productIdsWithQuantities) {
        if (productIdsWithQuantities == null || productIdsWithQuantities.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        Map<String, List<ShoppingListItemDTO>> itemsByCheapestStore = new HashMap<>();
        BigDecimal totalBasketCost = BigDecimal.ZERO;

        for (Map.Entry<String, Integer> basketEntry : productIdsWithQuantities.entrySet()) {
            String productId = basketEntry.getKey();
            Integer quantityNeeded = basketEntry.getValue();

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                logger.warn("Product with ID {} in basket not found in catalog. Skipping.", productId);
                continue;
            }
            Product product = productOpt.get();

            Optional<PriceEntry> cheapestPriceEntryOpt = priceEntryRepository.findByProductId(productId).stream()
                    .filter(pe -> !pe.getEntryDate().isBefore(today.minusDays(7)))
                    .min(Comparator.comparingDouble(PriceEntry::getPrice));

            if (cheapestPriceEntryOpt.isPresent()) {
                PriceEntry cheapestEntry = cheapestPriceEntryOpt.get();
                String storeName = cheapestEntry.getStoreName();
                BigDecimal itemPrice = BigDecimal.valueOf(cheapestEntry.getPrice()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(quantityNeeded));

                ShoppingListItemDTO listItem = new ShoppingListItemDTO(
                        product.getProductId(),
                        product.getProductName(),
                        product.getBrand(),
                        quantityNeeded,
                        itemPrice,
                        itemSubtotal,
                        String.format("%.2f %s", product.getPackageQuantityInput(), product.getPackageUnitInput() != null ? product.getPackageUnitInput().getCsvValue() : "")
                );

                itemsByCheapestStore.computeIfAbsent(storeName, k -> new ArrayList<>()).add(listItem);
                totalBasketCost = totalBasketCost.add(itemSubtotal);

            } else {
                logger.warn("No current price found for product ID {}. Skipping from optimization.", productId);
                 ShoppingListItemDTO notFoundItem = new ShoppingListItemDTO(
                        product.getProductId(),
                        product.getProductName(),
                        product.getBrand(),
                        quantityNeeded,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "Price Not Found"
                );
                itemsByCheapestStore.computeIfAbsent("Items_Not_Found_Or_Priced", k-> new ArrayList<>()).add(notFoundItem);
            }
        }
        
        logger.info("Overall estimated basket cost (sum of cheapest items individually): {}", totalBasketCost.setScale(2, RoundingMode.HALF_UP));

        return itemsByCheapestStore.entrySet().stream()
                .map(entry -> {
                    String storeName = entry.getKey();
                    List<ShoppingListItemDTO> items = entry.getValue();
                    BigDecimal storeTotal = items.stream()
                                                 .map(ShoppingListItemDTO::getSubtotal)
                                                 .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new OptimizedShoppingListDTO(storeName, items, storeTotal);
                })
                .sorted(Comparator.comparing(OptimizedShoppingListDTO::getStoreName))
                .collect(Collectors.toList());
    }
}