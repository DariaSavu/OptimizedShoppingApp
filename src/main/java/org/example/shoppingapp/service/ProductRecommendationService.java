package org.example.shoppingapp.service;

import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.dto.ProductRecommendationDTO;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductRecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(ProductRecommendationService.class);

    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;

    public ProductRecommendationService(ProductRepository productRepository, PriceEntryRepository priceEntryRepository) {
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    public List<ProductRecommendationDTO> getBestValueProducts(Optional<String> productIdOpt, Optional<String> categoryNameOpt, int limit) {
        LocalDate today = LocalDate.now();
        logger.debug("Getting best value products for productId: {}, category: {}, limit: {}", productIdOpt, categoryNameOpt, limit);

        List<Product> targetProducts = new ArrayList<>();

        if (productIdOpt.isPresent()) {
            productRepository.findById(productIdOpt.get()).ifPresent(targetProducts::add);
            if (targetProducts.isEmpty()) {
                logger.warn("Product with ID {} not found for best value recommendations.", productIdOpt.get());
                return List.of();
            }
            String categoryOfProduct = targetProducts.get(0).getProductCategory();
            if (categoryOfProduct != null && !categoryOfProduct.isBlank()) {
                 targetProducts.addAll(productRepository.findByCategory(categoryOfProduct).stream()
                    .filter(p -> !p.getProductId().equals(productIdOpt.get()))
                    .collect(Collectors.toList()));
            }

        } else if (categoryNameOpt.isPresent()) {
            targetProducts.addAll(productRepository.findByCategory(categoryNameOpt.get()));
            if (targetProducts.isEmpty()) {
                logger.info("No products found in category {} for best value recommendations.", categoryNameOpt.get());
                return List.of();
            }
        } else {
            logger.warn("Either productId or categoryName must be provided for best value recommendations.");
            return List.of();
        }
        

        List<PriceEntry> currentPriceEntries = targetProducts.stream()
            .flatMap(product -> priceEntryRepository.findByProductId(product.getProductId()).stream()
                .collect(Collectors.groupingBy(PriceEntry::getStoreName,
                         Collectors.maxBy(Comparator.comparing(PriceEntry::getEntryDate))))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(pe -> !pe.getEntryDate().isBefore(today.minusDays(7)))
            )
            .collect(Collectors.toList());


        return currentPriceEntries.stream()
                .map(priceEntry -> {
                    Product product = priceEntry.getProduct();
                    if (product == null || product.getNormalizedUnitType() == null || product.getNormalizedQuantity() <= 0) {
                        return null;
                    }
                    BigDecimal pricePerUnit = BigDecimal.valueOf(priceEntry.getPricePerNormalizedUnit()).setScale(2, RoundingMode.HALF_UP);
                    String unitNormalized = String.valueOf(product.getNormalizedUnitType());
                    String packageInfo = String.format("%.2f %s",
                                                       product.getPackageQuantityInput(),
                                                       product.getPackageUnitInput() != null ? product.getPackageUnitInput().getCsvValue() : "unit");

                    return new ProductRecommendationDTO(
                            product.getProductId(),
                            product.getProductName(),
                            product.getBrand(),
                            priceEntry.getStoreName(),
                            BigDecimal.valueOf(priceEntry.getPrice()).setScale(2, RoundingMode.HALF_UP),
                            packageInfo,
                            pricePerUnit,
                            unitNormalized,
                            product.getProductCategory()
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProductRecommendationDTO::getPricePerNormalizedUnit))
                .limit(limit)
                .collect(Collectors.toList());
    }
}