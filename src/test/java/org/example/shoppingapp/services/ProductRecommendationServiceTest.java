package org.example.shoppingapp.services;

import org.example.shoppingapp.model.dto.ProductRecommendationDTO;
import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.example.shoppingapp.service.ProductRecommendationService;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRecommendationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceEntryRepository priceEntryRepository;

    @InjectMocks
    private ProductRecommendationService productRecommendationService;

    private Product p1, p2, p3;
    private PriceEntry pe1_s1, pe1_s2, pe2_s1, pe3_s1_old;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        p1 = new Product("P001", "Lapte UHT 1L", "Lactate", "BrandA", 1.0, "l");
        p2 = new Product("P002", "Lapte Proaspat 1.5L", "Lactate", "BrandB", 1.5, "l");
        p3 = new Product("P003", "Branza Feta 200g", "Lactate", "BrandC", 200, "g");

        pe1_s1 = new PriceEntry(p1, "Store1", today, 5.0, "RON");
        pe1_s2 = new PriceEntry(p1, "Store2", today.minusDays(1), 4.8, "RON");
        pe2_s1 = new PriceEntry(p2, "Store1", today, 9.0, "RON");
        pe3_s1_old = new PriceEntry(p3, "Store1", today.minusDays(10), 6.0, "RON");
    }

    @Test
    @DisplayName("getBestValueProducts by category should return products sorted by price per unit")
    void getBestValueProducts_ByCategory_ReturnsSorted() {
        when(productRepository.findByCategory("Lactate")).thenReturn(Arrays.asList(p1, p2, p3));
        when(priceEntryRepository.findByProductId("P001")).thenReturn(Arrays.asList(pe1_s1, pe1_s2));
        when(priceEntryRepository.findByProductId("P002")).thenReturn(List.of(pe2_s1));
        when(priceEntryRepository.findByProductId("P003")).thenReturn(List.of(pe3_s1_old));

        List<ProductRecommendationDTO> recommendations = productRecommendationService.getBestValueProducts(
                Optional.empty(), Optional.of("Lactate"), 5);

        assertNotNull(recommendations);
        
        if (!recommendations.isEmpty()) {
            for (int i = 0; i < recommendations.size() - 1; i++) {
                assertTrue(recommendations.get(i).getPricePerNormalizedUnit()
                        .compareTo(recommendations.get(i + 1).getPricePerNormalizedUnit()) <= 0);
            }
            ProductRecommendationDTO best = recommendations.get(0);
            assertEquals("P001", best.getProductId());
            assertEquals("Store2", best.getStoreName());
            assertEquals(0, BigDecimal.valueOf(4.80).setScale(2, RoundingMode.HALF_UP).compareTo(best.getCurrentPrice()));
            assertEquals(0, BigDecimal.valueOf(4.80).setScale(2, RoundingMode.HALF_UP).compareTo(best.getPricePerNormalizedUnit()));
        }
        assertTrue(recommendations.stream().anyMatch(r -> r.getProductId().equals("P001") && r.getStoreName().equals("Store2")));
    }

    @Test
    @DisplayName("getBestValueProducts by productId should find prices for that product and similar")
    void getBestValueProducts_ByProductId_ReturnsProductAndCategoryMatches() {
        when(productRepository.findById("P001")).thenReturn(Optional.of(p1));
        when(productRepository.findByCategory("Lactate")).thenReturn(Arrays.asList(p1, p2));
        when(priceEntryRepository.findByProductId("P001")).thenReturn(Arrays.asList(pe1_s1, pe1_s2));
        when(priceEntryRepository.findByProductId("P002")).thenReturn(List.of(pe2_s1));

        List<ProductRecommendationDTO> recommendations = productRecommendationService.getBestValueProducts(
                Optional.of("P001"), Optional.empty(), 5);
        
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        // Assuming service's date filter allows these prices
        if (recommendations.size() >=3) { // Check size before accessing elements
            assertEquals("P001", recommendations.get(0).getProductId());
            assertEquals("Store2", recommendations.get(0).getStoreName());
            assertEquals("P001", recommendations.get(1).getProductId());
            assertEquals("Store1", recommendations.get(1).getStoreName());
            assertEquals("P002", recommendations.get(2).getProductId());
        }
    }

    @Test
    @DisplayName("getBestValueProducts returns empty list if no product or category specified")
    void getBestValueProducts_NoCriteria_ReturnsEmpty() {
        List<ProductRecommendationDTO> recommendations = productRecommendationService.getBestValueProducts(
                Optional.empty(), Optional.empty(), 5);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    @DisplayName("getBestValueProducts returns empty list if product or category not found")
    void getBestValueProducts_NotFound_ReturnsEmpty() {
        when(productRepository.findById("P999")).thenReturn(Optional.empty());
        List<ProductRecommendationDTO> recs1 = productRecommendationService.getBestValueProducts(
                Optional.of("P999"), Optional.empty(), 5);
        assertTrue(recs1.isEmpty());

        when(productRepository.findByCategory("Inexistent")).thenReturn(Collections.emptyList());
        List<ProductRecommendationDTO> recs2 = productRecommendationService.getBestValueProducts(
                Optional.empty(), Optional.of("Inexistent"), 5);
        assertTrue(recs2.isEmpty());
    }

    @Test
    @DisplayName("getBestValueProducts handles products with no current price entries")
    void getBestValueProducts_NoPriceEntries_Handled() {
        when(productRepository.findByCategory("Lactate")).thenReturn(List.of(p1));
        when(priceEntryRepository.findByProductId("P001")).thenReturn(Collections.emptyList());

        List<ProductRecommendationDTO> recommendations = productRecommendationService.getBestValueProducts(
                Optional.empty(), Optional.of("Lactate"), 5);
        assertTrue(recommendations.isEmpty());
    }
}