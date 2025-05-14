package org.example.shoppingapp.model;

import org.example.shoppingapp.model.enums.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import java.util.Objects;

public class PriceEntry {
    private Product product;
    private String storeName;
    private LocalDate entryDate;
    private double price;
    private Currency currency;
    private double pricePerNormalizedUnit;
    private static final Logger logger = LoggerFactory.getLogger(PriceEntry.class);
    public PriceEntry(Product product, String storeName, LocalDate entryDate, double price, String currency) {
        this.product = product;
        this.storeName = storeName;
        this.entryDate = entryDate;
        this.currency = Currency.valueOf(currency);
        this.price = price;
        calculatePricePerNormalizedUnit();
    }

    private void calculatePricePerNormalizedUnit() {
        if (product == null || product.getNormalizedUnitType() == null) {
            this.pricePerNormalizedUnit = this.price;
            return;
        }
        double normQty = product.getNormalizedQuantity();
        if (normQty <= 0) {
            this.pricePerNormalizedUnit = this.price;
            return;
        }
        this.pricePerNormalizedUnit = this.price / normQty;
    }

    public Product getProduct() { return product; }
    public String getStoreName() { return storeName; }
    public LocalDate getEntryDate() { return entryDate; }
    public double getPrice() { return price; }
    public Currency getCurrency() { return currency; }
    public double getPricePerNormalizedUnit() { return pricePerNormalizedUnit; }

    public String getUnitForNormalizedPrice() {
        if (product != null && product.getNormalizedUnitType() != null) {
            return product.getNormalizedUnitType().getNormalizedForm();
        }
        return product != null && product.getPackageUnitInput() != null ? product.getPackageUnitInput().getCsvValue() : "pachet";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceEntry that = (PriceEntry) o;
        return Double.compare(that.price, price) == 0 &&
                Objects.equals(product, that.product) &&
                Objects.equals(storeName, that.storeName) &&
                Objects.equals(entryDate, that.entryDate) &&
                Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, storeName, entryDate, price, currency);
    }

    @Override
    public String toString() {
        String productInfo = "null_product";
        if (product != null) {
            productInfo = product.getProductId() + " - " + product.getProductName();
        }

        return "PriceEntry{" +
                "product=" + productInfo +
                ", store='" + storeName + '\'' +
                ", date=" + entryDate +
                ", price=" + String.format("%.2f", price) + " " + currency +
                ", pricePerNormUnit=" + String.format("%.2f", pricePerNormalizedUnit) + "/" + getUnitForNormalizedPrice() +
                '}';
    }
}