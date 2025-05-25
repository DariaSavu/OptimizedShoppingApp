package org.example.shoppingapp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceHistoryDataPointDTO {
    private LocalDate date;
    private BigDecimal price;
    private String storeName; // Opțional, dacă vrei să arăți și magazinul

    public PriceHistoryDataPointDTO(LocalDate date, BigDecimal price, String storeName) {
        this.date = date;
        this.price = price;
        this.storeName = storeName;
    }

    // Getters
    public LocalDate getDate() { return date; }
    public BigDecimal getPrice() { return price; }
    public String getStoreName() { return storeName; }

    @Override
    public String toString() {
        return String.format("Date: %s, Price: %.2f, Store: %s", date, price, storeName);
    }
}