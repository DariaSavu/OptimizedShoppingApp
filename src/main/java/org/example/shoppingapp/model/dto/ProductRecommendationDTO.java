package org.example.shoppingapp.model.dto;

import java.math.BigDecimal;

public class ProductRecommendationDTO {
    private String productId;
    private String productName;
    private String brand;
    private String storeName;
    private BigDecimal currentPrice;
    private String packageInfo; // ex: "1.0 l", "0.5 kg"
    private BigDecimal pricePerNormalizedUnit;
    private String normalizedUnit; // ex: "kg", "l", "buc"
    private String category;


    public ProductRecommendationDTO(String productId, String productName, String brand, String storeName,
                                    BigDecimal currentPrice, String packageInfo,
                                    BigDecimal pricePerNormalizedUnit, String normalizedUnit, String category) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.storeName = storeName;
        this.currentPrice = currentPrice;
        this.packageInfo = packageInfo;
        this.pricePerNormalizedUnit = pricePerNormalizedUnit;
        this.normalizedUnit = normalizedUnit;
        this.category = category;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getStoreName() { return storeName; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public String getPackageInfo() { return packageInfo; }
    public BigDecimal getPricePerNormalizedUnit() { return pricePerNormalizedUnit; }
    public String getNormalizedUnit() { return normalizedUnit; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s at %s: %.2f %s (%.2f/%s)",
                productName, brand, packageInfo, storeName, currentPrice,
                (currentPrice.doubleValue() == 1.0 ? "leu" : "lei"), // Simplificare pentru plural
                pricePerNormalizedUnit, normalizedUnit);
    }
}