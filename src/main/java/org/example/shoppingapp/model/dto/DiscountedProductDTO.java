package org.example.shoppingapp.model.dto;

import java.math.BigDecimal;

public class DiscountedProductDTO {
    private String productId;
    private String productName;
    private String brand;
    private String storeName;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private double discountPercentage;
    private String packageInfo;

    public DiscountedProductDTO(String productId, String productName, String brand, String storeName,
                                BigDecimal originalPrice, BigDecimal discountedPrice, double discountPercentage,
                                String packageInfo) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.storeName = storeName;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.discountPercentage = discountPercentage;
        this.packageInfo = packageInfo;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getStoreName() { return storeName; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public double getDiscountPercentage() { return discountPercentage; }
    public String getPackageInfo() { return packageInfo; }

    @Override
    public String toString() {
        return String.format("%s (%s) at %s: Was %.2f, Now %.2f (%.0f%% off) [%s]",
                productName, brand, storeName, originalPrice, discountedPrice, discountPercentage, packageInfo);
    }
}