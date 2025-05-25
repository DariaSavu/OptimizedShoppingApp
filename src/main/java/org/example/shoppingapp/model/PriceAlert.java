package org.example.shoppingapp.model;

import java.time.LocalDate;
import java.util.Objects;

public class PriceAlert {
    private Integer userId;
    private Product product;
    private double targetPrice;
    private boolean active;
    private LocalDate dateCreated;
    public PriceAlert(Integer userId, Product product, double targetPrice, boolean active, LocalDate dateCreated) {
        this.userId = userId;
        this.product = product;
        this.targetPrice = targetPrice;
        this.active = active;
        this.dateCreated = dateCreated;
    }
    public Integer getUserId() {
        return userId;
    }

    public Product getProduct() {
        return product;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceAlert that = (PriceAlert) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, product);
    }

    @Override
    public String toString() {
        String productName = (product != null && product.getProductName() != null) ? product.getProductName() : "N/A";
        String productId = (product != null && product.getProductId() != null) ? product.getProductId() : "N/A";
        return String.format("Alert for UserID %d: Product '%s' (ID: %s), Target Price: %.2f, Active: %s, Created: %s",
                userId,
                productName,
                productId,
                targetPrice,
                active,
                dateCreated
        );
    }
}