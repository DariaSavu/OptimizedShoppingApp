package org.example.shoppingapp.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class OptimizedShoppingListDTO {
    private String storeName;
    private List<ShoppingListItemDTO> items;
    private BigDecimal storeTotalCost;

    public OptimizedShoppingListDTO(String storeName, List<ShoppingListItemDTO> items, BigDecimal storeTotalCost) {
        this.storeName = storeName;
        this.items = items;
        this.storeTotalCost = storeTotalCost;
    }

    public String getStoreName() { return storeName; }
    public List<ShoppingListItemDTO> getItems() { return items; }
    public BigDecimal getStoreTotalCost() { return storeTotalCost; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shopping List for: ").append(storeName).append("\n");
        sb.append("Total Cost at this store: ").append(String.format("%.2f", storeTotalCost)).append("\n");
        items.forEach(item -> sb.append("  - ").append(item.toString()).append("\n"));
        return sb.toString();
    }
}