package org.example.shoppingapp.model.dto;

import java.math.BigDecimal;

public class ShoppingListItemDTO {
    private String productId;
    private String productName;
    private String brand;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String packageInfo;

    public ShoppingListItemDTO(String productId, String productName, String brand, int quantity,
                               BigDecimal unitPrice, BigDecimal subtotal, String packageInfo) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.packageInfo = packageInfo;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getPackageInfo() { return packageInfo; }


    @Override
    public String toString() {
        return String.format("%d x %s (%s) [%s] @ %.2f = %.2f",
                quantity, productName, brand, packageInfo, unitPrice, subtotal);
    }
}