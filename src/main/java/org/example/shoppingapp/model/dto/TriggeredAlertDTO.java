package org.example.shoppingapp.model.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class TriggeredAlertDTO {
    private Integer userId;
    private String productId;
    private String productName;
    private String brand;
    private String storeName; // Magazinul unde s-a găsit prețul care a declanșat alerta
    private BigDecimal currentPrice; // Prețul actual găsit, care a declanșat alerta
    private BigDecimal targetPriceSetByUser; // Prețul țintă setat inițial de utilizator

    public TriggeredAlertDTO(Integer userId, String productId, String productName, String brand, String storeName,
                             BigDecimal currentPrice, BigDecimal targetPriceSetByUser) {
        this.userId = Objects.requireNonNull(userId);
        this.productId = Objects.requireNonNull(productId);
        this.productName = Objects.requireNonNull(productName);
        this.brand = brand; // Brandul poate fi null dacă produsul nu are brand
        this.storeName = Objects.requireNonNull(storeName);
        this.currentPrice = Objects.requireNonNull(currentPrice);
        this.targetPriceSetByUser = Objects.requireNonNull(targetPriceSetByUser);
    }

    // Getters
    public Integer getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getBrand() {
        return brand;
    }

    public String getStoreName() {
        return storeName;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getTargetPriceSetByUser() {
        return targetPriceSetByUser;
    }

    @Override
    public String toString() {
        return String.format(
            "ALERT TRIGGERED for User ID %d: Product '%s' (ID: %s, Brand: %s) is now %.2f at %s. (Target was <= %.2f)",
            userId,
            productName,
            productId,
            brand != null ? brand : "N/A",
            currentPrice,
            storeName,
            targetPriceSetByUser
        );
    }

    // Optional: equals and hashCode if you plan to store these in sets or use them in comparisons
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggeredAlertDTO that = (TriggeredAlertDTO) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(productId, that.productId) &&
               Objects.equals(productName, that.productName) &&
               Objects.equals(brand, that.brand) &&
               Objects.equals(storeName, that.storeName) &&
               Objects.equals(currentPrice, that.currentPrice) &&
               Objects.equals(targetPriceSetByUser, that.targetPriceSetByUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId, productName, brand, storeName, currentPrice, targetPriceSetByUser);
    }
}