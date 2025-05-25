package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.Discount;
import org.example.shoppingapp.repository.interfaces.DiscountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class InMemoryDiscountRepository implements DiscountRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryDiscountRepository.class);
    private final List<Discount> discounts = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Discount save(Discount discount) {
        if (discount == null) {
            logger.warn("Attempted to save a null Discount.");
            throw new IllegalArgumentException("Discount cannot be null.");
        }
        discounts.add(discount);
        logger.trace("Discount saved for product: {}", discount.getProduct() != null ? discount.getProduct().getProductId() : "N/A");
        return discount;
    }

    @Override
    public List<Discount> saveAll(Iterable<Discount> newDiscounts) {
        List<Discount> savedDiscounts = new ArrayList<>();
        if (newDiscounts != null) {
            for (Discount discount : newDiscounts) {
                 if (discount != null) {
                    savedDiscounts.add(save(discount));
                }
            }
        }
        return savedDiscounts;
    }

    @Override
    public List<Discount> findAll() {
        return new ArrayList<>(discounts);
    }

    @Override
    public List<Discount> findByProductId(String productId) {
        if (productId == null) return new ArrayList<>();
        return discounts.stream()
                .filter(d -> d.getProduct() != null && productId.equals(d.getProduct().getProductId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Discount> findByStoreName(String storeName) {
        if (storeName == null) return new ArrayList<>();
        return discounts.stream()
                .filter(d -> storeName.equalsIgnoreCase(d.getStoreName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Discount> findActiveOnDate(LocalDate date) {
        if (date == null) return new ArrayList<>();
        return discounts.stream()
                .filter(d -> d.isActiveOnDate(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Discount> findByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) return new ArrayList<>();
        return discounts.stream()
                .filter(d -> !d.getStartDate().isAfter(to) && !d.getEndDate().isBefore(from))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        discounts.clear();
        logger.info("All discounts cleared.");
    }
}