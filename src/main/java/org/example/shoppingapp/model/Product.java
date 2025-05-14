package org.example.shoppingapp.model;

import org.example.shoppingapp.model.enums.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
public class Product {
    private String productId;
    private String productName;
    private String productCategory;
    private String brand;
    private double packageQuantityInput;
    private UnitType packageUnitInput;

    private double normalizedQuantity;
    private UnitType normalizedUnitType;
    private static final Logger logger = LoggerFactory.getLogger(Product.class);

    public Product(String productId, String productName, String productCategory, String brand,
                   double packageQuantity, String packageUnitString) {
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.brand = brand;
        this.packageQuantityInput = packageQuantity;

        this.packageUnitInput = UnitType.fromString(packageUnitString);
        normalize();
    }

    private void normalize() {
        if (this.packageUnitInput == null) {
            this.normalizedQuantity = this.packageQuantityInput;
            this.normalizedUnitType = null;
            logger.warn("Normalization skipped for product " + productId + " due to null packageUnitInput.");
            return;
        }

        this.normalizedUnitType = this.packageUnitInput.getBaseUnitType();
        this.normalizedQuantity = this.packageUnitInput.getNormalizedValue(this.packageQuantityInput);
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductCategory() { return productCategory; }
    public String getBrand() { return brand; }
    public double getPackageQuantityInput() { return packageQuantityInput; }
    public UnitType getPackageUnitInput() { return packageUnitInput; }
    public double getNormalizedQuantity() { return normalizedQuantity; }
    public UnitType getNormalizedUnitType() { return normalizedUnitType; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        String pkgUnitStr = (packageUnitInput != null) ? packageUnitInput.getCsvValue() : "[UNKNOWN_UNIT]";
        String normUnitStr = (normalizedUnitType != null) ? normalizedUnitType.getNormalizedForm() : "[UNKNOWN_BASE]";

        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", brand='" + brand + '\'' +
                ", packageQtyInput=" + packageQuantityInput + " " + pkgUnitStr +
                ", normalizedQty=" + String.format("%.3f", normalizedQuantity) + " " + normUnitStr +
                '}';
    }
}