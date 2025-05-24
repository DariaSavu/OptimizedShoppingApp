package org.example.shoppingapp.repository.interfaces;

import org.example.shoppingapp.model.Discount;
import java.time.LocalDate;
import java.util.List;

public interface DiscountRepository {
    Discount save(Discount discount);
    List<Discount> saveAll(Iterable<Discount> discounts);
    List<Discount> findAll();
    List<Discount> findByProductId(String productId);
    List<Discount> findByStoreName(String storeName);
    List<Discount> findActiveOnDate(LocalDate date);
    List<Discount> findByDateRange(LocalDate from, LocalDate to);
    void deleteAll(); // Sau clear()
}