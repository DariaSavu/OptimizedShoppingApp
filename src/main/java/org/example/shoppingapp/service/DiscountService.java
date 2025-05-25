package org.example.shoppingapp.service;
import org.example.shoppingapp.model.Discount;
import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.dto.DiscountedProductDTO;
import org.example.shoppingapp.repository.interfaces.DiscountRepository;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscountService {
    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);

    private final DiscountRepository discountRepository;
    private final PriceEntryRepository priceEntryRepository;

    public DiscountService(DiscountRepository discountRepository, PriceEntryRepository priceEntryRepository) {
        this.discountRepository = discountRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    /**
     * Găsește produsele cu cele mai mari procente de reducere active în prezent.
     * @param limit Numărul maxim de produse de returnat.
     * @return O listă de DiscountedProductDTO.
     */
    public List<DiscountedProductDTO> getBestCurrentDiscounts(int limit) {
        LocalDate today = LocalDate.now();
        logger.debug("Fetching best current discounts for date: {}, limit: {}", today, limit);

        List<Discount> activeDiscounts = discountRepository.findActiveOnDate(today);
        if (activeDiscounts.isEmpty()) {
            logger.info("No active discounts found for today.");
            return List.of();
        }

        return activeDiscounts.stream()
                .map(this::createDiscountedProductDTO)
                .filter(Objects::nonNull)
                .filter(dto -> dto.getDiscountPercentage() > 0)
                .sorted(Comparator.comparingDouble(DiscountedProductDTO::getDiscountPercentage).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<DiscountedProductDTO> getNewDiscounts(int hoursAgo, int limit) {
        LocalDate today = LocalDate.now();
        long daysAgo = hoursAgo / 24;
        if (hoursAgo % 24 > 0) {
            daysAgo++;
        }
        LocalDate sinceObservationDate = today.minusDays(daysAgo);

        logger.debug("Fetching new discounts active on {} observed since {}, limit: {}", today, sinceObservationDate, limit);

        List<Discount> recentAndActiveDiscounts = discountRepository.findAll().stream()
                .filter(d -> d.isActiveOnDate(today) &&
                             !d.getDiscountObservationDate().isBefore(sinceObservationDate))
                .sorted(Comparator.comparing(Discount::getDiscountObservationDate).reversed()
                                  .thenComparing(Discount::getDiscountPercentage).reversed())
                .collect(Collectors.toList());

        if (recentAndActiveDiscounts.isEmpty()) {
            logger.info("No new and active discounts found since {}.", sinceObservationDate);
            return List.of();
        }

        return recentAndActiveDiscounts.stream()
                .map(this::createDiscountedProductDTO)
                .filter(Objects::nonNull)
                .filter(dto -> dto.getDiscountPercentage() > 0)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private DiscountedProductDTO createDiscountedProductDTO(Discount discount) {
        Product product = discount.getProduct();
        if (product == null) {
            logger.warn("Discount {} has a null product.", discount);
            return null;
        }
        Optional<PriceEntry> referencePriceEntryOpt = priceEntryRepository
                .findByProductId(product.getProductId()).stream()
                .filter(pe -> pe.getStoreName().equalsIgnoreCase(discount.getStoreName()))
                .max(Comparator.comparing(PriceEntry::getEntryDate));


        if (referencePriceEntryOpt.isEmpty()) {
            logger.warn("No price entry found for product {} at store {} to calculate discount details.",
                    product.getProductId(), discount.getStoreName());
            return null;
        }

        PriceEntry referencePriceEntry = referencePriceEntryOpt.get();
        BigDecimal originalPrice = BigDecimal.valueOf(referencePriceEntry.getPrice()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountPercentage = BigDecimal.valueOf(discount.getDiscountPercentage());
        BigDecimal oneHundred = BigDecimal.valueOf(100);

        BigDecimal discountAmount = originalPrice.multiply(discountPercentage).divide(oneHundred, 2, RoundingMode.HALF_UP);
        BigDecimal discountedPrice = originalPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        String packageInfo = String.format("%.2f %s",
                                           product.getPackageQuantityInput(),
                                           product.getPackageUnitInput() != null ? product.getPackageUnitInput().getCsvValue() : "unit");

        return new DiscountedProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getBrand(),
                discount.getStoreName(),
                originalPrice,
                discountedPrice,
                discount.getDiscountPercentage(),
                packageInfo
        );
    }
}