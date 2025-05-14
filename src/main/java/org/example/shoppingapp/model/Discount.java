package org.example.shoppingapp.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Objects;

public class Discount {
    private Product product;
    private String storeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double discountPercentage;
    private LocalDate discountObservationDate;
    private static final Logger logger = LoggerFactory.getLogger(Discount.class);

    public Discount(Product product, String storeName, LocalDate startDate, LocalDate endDate,
                    double discountPercentage, LocalDate discountObservationDate) {
        this.product = product;
        this.storeName = storeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discountObservationDate = discountObservationDate;

        if (discountPercentage < 0 || discountPercentage > 100) {
            logger.warn("Warning: Discount percentage " + discountPercentage + "% for product " + product.getProductId() + " is outside the 0-100 range. Using as is.");
        }
        this.discountPercentage = discountPercentage;
    }

    public boolean isActiveOnDate(LocalDate checkDate) {
        return !checkDate.isBefore(startDate) && !checkDate.isAfter(endDate);
    }

    public Product getProduct() { return product; }
    public String getStoreName() { return storeName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getDiscountPercentage() { return discountPercentage; }
    public LocalDate getDiscountObservationDate() { return discountObservationDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discount discount = (Discount) o;
        return Double.compare(discount.discountPercentage, discountPercentage) == 0 &&
                Objects.equals(product, discount.product) &&
                Objects.equals(storeName, discount.storeName) &&
                Objects.equals(startDate, discount.startDate) &&
                Objects.equals(endDate, discount.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, storeName, startDate, endDate, discountPercentage);
    }

    @Override
    public String toString() {
        String productInfo = "null_product";
        if (product != null) {
            productInfo = product.getProductId() + " - " + product.getProductName();
        }
        return "Discount{" +
                "product=" + productInfo +
                ", store='" + storeName + '\'' +
                ", activePeriod=" + startDate + " to " + endDate +
                ", percentage=" + discountPercentage + "%" +
                ", observedOn=" + discountObservationDate +
                '}';
    }
}