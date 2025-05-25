package org.example.shoppingapp.services;


import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.dto.OptimizedShoppingListDTO;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.example.shoppingapp.service.ShoppingListOptimizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListOptimizerServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private PriceEntryRepository priceEntryRepository;

    @InjectMocks
    private ShoppingListOptimizerService optimizerService;

    private Product p1, p2, p3;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        p1 = new Product("P001", "Lapte", "Lactate", "BrandA", 1.0, "l");
        p2 = new Product("P002", "Pâine", "Panificație", "BrandB", 0.5, "kg");
        p3 = new Product("P003", "Ouă", "Ouă", "BrandC", 10.0, "buc");
    }

    @Test
    @DisplayName("Optimize basket should group items by cheapest store")
    void optimizeShoppingBasket_GroupsByCheapestStore() {
        when(productRepository.findById("P001")).thenReturn(Optional.of(p1));
        when(productRepository.findById("P002")).thenReturn(Optional.of(p2));

        when(priceEntryRepository.findByProductId("P001")).thenReturn(Arrays.asList(
                new PriceEntry(p1, "Lidl", today, 5.0, "RON"),
                new PriceEntry(p1, "Kaufland", today, 5.2, "RON")
        ));
        when(priceEntryRepository.findByProductId("P002")).thenReturn(Arrays.asList(
                new PriceEntry(p2, "Lidl", today, 3.5, "RON"),
                new PriceEntry(p2, "Kaufland", today, 3.0, "RON")
        ));

        Map<String, Integer> basket = new HashMap<>();
        basket.put("P001", 2);
        basket.put("P002", 1);

        List<OptimizedShoppingListDTO> result = optimizerService.optimizeShoppingBasket(basket);

        assertEquals(2, result.size());

        OptimizedShoppingListDTO lidlList = result.stream().filter(l -> l.getStoreName().equals("Lidl")).findFirst().orElse(null);
        assertNotNull(lidlList);
        assertEquals(1, lidlList.getItems().size());
        assertEquals("P001", lidlList.getItems().get(0).getProductId());
        assertEquals(0, BigDecimal.valueOf(10.0).compareTo(lidlList.getStoreTotalCost()));

        OptimizedShoppingListDTO kauflandList = result.stream().filter(l -> l.getStoreName().equals("Kaufland")).findFirst().orElse(null);
        assertNotNull(kauflandList);
        assertEquals(1, kauflandList.getItems().size());
        assertEquals("P002", kauflandList.getItems().get(0).getProductId());
        assertEquals(0, BigDecimal.valueOf(3.0).compareTo(kauflandList.getStoreTotalCost()));
    }

    @Test
    @DisplayName("Optimize basket with product having no price entry")
    void optimizeShoppingBasket_ProductNoPrice_Handled() {
        when(productRepository.findById("P001")).thenReturn(Optional.of(p1));
        when(productRepository.findById("P003")).thenReturn(Optional.of(p3));
        when(priceEntryRepository.findByProductId("P001")).thenReturn(List.of(new PriceEntry(p1, "Lidl", today, 5.0, "RON")));
        when(priceEntryRepository.findByProductId("P003")).thenReturn(Collections.emptyList());


        Map<String, Integer> basket = new HashMap<>();
        basket.put("P001", 1);
        basket.put("P003", 2);

        List<OptimizedShoppingListDTO> result = optimizerService.optimizeShoppingBasket(basket);
        assertEquals(2, result.size());
        
        OptimizedShoppingListDTO notPricedList = result.stream()
            .filter(l -> l.getStoreName().equals("Items_Not_Found_Or_Priced")).findFirst().get();
        assertEquals(1, notPricedList.getItems().size());
        assertEquals("P003", notPricedList.getItems().get(0).getProductId());
    }


    @Test
    @DisplayName("Optimize empty basket returns empty list")
    void optimizeShoppingBasket_EmptyBasket_ReturnsEmptyList() {
        List<OptimizedShoppingListDTO> result = optimizerService.optimizeShoppingBasket(Collections.emptyMap());
        assertTrue(result.isEmpty());
    }
}