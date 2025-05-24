package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProductRepositoryTest {

    private ProductRepository productRepository;

    private Product p1, p2, p3;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository(); // Creează o instanță nouă pentru fiecare test

        // Produse de test
        p1 = new Product("P001", "Lapte Zuzu", "Lactate", "Zuzu", 1.0, "l");
        p2 = new Product("P002", "Pâine albă", "Panificație", "Vel Pitar", 0.5, "kg");
        p3 = new Product("P003", "Iaurt de băut Zuzu", "Lactate", "Zuzu", 0.33, "kg");
    }

    @Test
    @DisplayName("Save should add a new product or update an existing one")
    void save_NewProduct_ShouldAddIt() {
        productRepository.save(p1);
        assertEquals(1, productRepository.count());
        assertTrue(productRepository.findById("P001").isPresent());

        Product updatedP1 = new Product("P001", "Lapte Zuzu 1.5%", "Lactate", "Zuzu", 1.0, "l");
        productRepository.save(updatedP1);
        assertEquals(1, productRepository.count()); // Numărul nu ar trebui să crească
        assertEquals("Lapte Zuzu 1.5%", productRepository.findById("P001").get().getProductName());
    }

    @Test
    @DisplayName("Save should throw IllegalArgumentException for null product or product with null ID")
    void save_NullProductOrId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> productRepository.save(null));
        Product productWithNullId = new Product(null, "Test", "Cat", "Brand", 1, "l");
        assertThrows(IllegalArgumentException.class, () -> productRepository.save(productWithNullId));
    }

    @Test
    @DisplayName("SaveAll should add multiple products")
    void saveAll_AddsMultipleProducts() {
        List<Product> productsToSave = Arrays.asList(p1, p2);
        productRepository.saveAll(productsToSave);
        assertEquals(2, productRepository.count());
        assertTrue(productRepository.findById("P001").isPresent());
        assertTrue(productRepository.findById("P002").isPresent());
    }

    @Test
    @DisplayName("SaveAll should handle null or empty iterable gracefully")
    void saveAll_NullOrEmptyIterable_DoesNotThrowError() {
        assertDoesNotThrow(() -> productRepository.saveAll(null));
        assertEquals(0, productRepository.count());
        assertDoesNotThrow(() -> productRepository.saveAll(Arrays.asList()));
        assertEquals(0, productRepository.count());
    }
    
    @Test
    @DisplayName("SaveAll should skip null products in the list")
    void saveAll_SkipsNullProductsInList() {
        List<Product> productsWithNull = Arrays.asList(p1, null, p2);
        productRepository.saveAll(productsWithNull);
        assertEquals(2, productRepository.count());
        assertTrue(productRepository.findById(p1.getProductId()).isPresent());
        assertTrue(productRepository.findById(p2.getProductId()).isPresent());
    }


    @Test
    @DisplayName("FindById should return product if exists, else empty")
    void findById_ProductExists_ReturnsProduct() {
        productRepository.save(p1);
        Optional<Product> found = productRepository.findById("P001");
        assertTrue(found.isPresent());
        assertEquals(p1, found.get());

        Optional<Product> notFound = productRepository.findById("P999");
        assertFalse(notFound.isPresent());
    }
     @Test
    @DisplayName("FindById should return empty for null ID")
    void findById_NullId_ReturnsEmpty() {
        Optional<Product> found = productRepository.findById(null);
        assertFalse(found.isPresent());
    }


    @Test
    @DisplayName("ExistsById should return true if product exists, else false")
    void existsById_ReturnsCorrectBoolean() {
        productRepository.save(p1);
        assertTrue(productRepository.existsById("P001"));
        assertFalse(productRepository.existsById("P999"));
        assertFalse(productRepository.existsById(null));
    }

    @Test
    @DisplayName("FindAll should return all saved products")
    void findAll_ReturnsAllProducts() {
        assertEquals(0, productRepository.findAll().size());
        productRepository.save(p1);
        productRepository.save(p2);
        List<Product> allProducts = productRepository.findAll();
        assertEquals(2, allProducts.size());
        assertTrue(allProducts.contains(p1));
        assertTrue(allProducts.contains(p2));
    }

    @Test
    @DisplayName("Count should return the number of products")
    void count_ReturnsCorrectNumber() {
        assertEquals(0, productRepository.count());
        productRepository.save(p1);
        assertEquals(1, productRepository.count());
        productRepository.save(p2);
        assertEquals(2, productRepository.count());
    }

    @Test
    @DisplayName("DeleteById should remove the product")
    void deleteById_RemovesProduct() {
        productRepository.save(p1);
        assertTrue(productRepository.findById("P001").isPresent());
        productRepository.deleteById("P001");
        assertFalse(productRepository.findById("P001").isPresent());
        assertEquals(0, productRepository.count());
    }
    
    @Test
    @DisplayName("DeleteById should do nothing for non-existent or null ID")
    void deleteById_NonExistentOrNullId_DoesNothing() {
        productRepository.save(p1);
        assertDoesNotThrow(() -> productRepository.deleteById("P999"));
        assertEquals(1, productRepository.count());
        assertDoesNotThrow(() -> productRepository.deleteById(null));
        assertEquals(1, productRepository.count());
    }

    @Test
    @DisplayName("Delete should remove the product by entity")
    void delete_RemovesProductByEntity() {
        productRepository.save(p1);
        productRepository.delete(p1);
        assertFalse(productRepository.findById("P001").isPresent());
    }
    
    @Test
    @DisplayName("Delete should do nothing for null entity or entity with null ID")
    void delete_NullEntityOrEntityWithNullId_DoesNothing() {
        productRepository.save(p1);
        assertDoesNotThrow(() -> productRepository.delete(null));
        Product productWithNullId = new Product(null, "Test", "Cat", "Brand", 1, "l");
        assertDoesNotThrow(() -> productRepository.delete(productWithNullId));
        assertEquals(1, productRepository.count());
    }

    @Test
    @DisplayName("DeleteAll should remove all products")
    void deleteAll_RemovesAllProducts() {
        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.deleteAll();
        assertEquals(0, productRepository.count());
        assertTrue(productRepository.findAll().isEmpty());
    }

    // Teste pentru metodele specifice
    @Test
    @DisplayName("FindByCategory should return products of that category (case-insensitive)")
    void findByCategory_ReturnsMatchingProducts() {
        productRepository.saveAll(Arrays.asList(p1, p2, p3)); // p1, p3 sunt "Lactate"
        List<Product> lactate = productRepository.findByCategory("Lactate");
        assertEquals(2, lactate.size());
        assertTrue(lactate.contains(p1));
        assertTrue(lactate.contains(p3));

        List<Product> lactateLower = productRepository.findByCategory("lactate");
        assertEquals(2, lactateLower.size());

        List<Product> nonExistent = productRepository.findByCategory("Fructe");
        assertTrue(nonExistent.isEmpty());

        assertTrue(productRepository.findByCategory(null).isEmpty());
        assertTrue(productRepository.findByCategory("  ").isEmpty());
    }

    @Test
    @DisplayName("FindByBrand should return products of that brand (case-insensitive)")
    void findByBrand_ReturnsMatchingProducts() {
        productRepository.saveAll(Arrays.asList(p1, p2, p3)); // p1, p3 sunt "Zuzu"
        List<Product> zuzuProducts = productRepository.findByBrand("Zuzu");
        assertEquals(2, zuzuProducts.size());
        assertTrue(zuzuProducts.contains(p1));
        assertTrue(zuzuProducts.contains(p3));

        List<Product> velPitarProducts = productRepository.findByBrand("Vel Pitar");
        assertEquals(1, velPitarProducts.size());
        assertTrue(velPitarProducts.contains(p2));

        assertTrue(productRepository.findByBrand(null).isEmpty());
    }

    @Test
    @DisplayName("FindByProductNameAndBrand should return the specific product (case-insensitive)")
    void findByProductNameAndBrand_ReturnsSpecificProduct() {
        productRepository.saveAll(Arrays.asList(p1, p2, p3));
        Optional<Product> found = productRepository.findByProductNameAndBrand("Lapte Zuzu", "Zuzu");
        assertTrue(found.isPresent());
        assertEquals(p1, found.get());

        Optional<Product> foundLower = productRepository.findByProductNameAndBrand("pâine albă", "vel pitar");
        assertTrue(foundLower.isPresent());
        assertEquals(p2, foundLower.get());
        
        Optional<Product> notFound = productRepository.findByProductNameAndBrand("Lapte Zuzu", "Napolact");
        assertFalse(notFound.isPresent());

        assertFalse(productRepository.findByProductNameAndBrand(null, "Zuzu").isPresent());
        assertFalse(productRepository.findByProductNameAndBrand("Lapte", null).isPresent());
    }

    @Test
    @DisplayName("FindByProductNameContaining should return products with name containing substring (case-insensitive)")
    void findByProductNameContaining_ReturnsMatchingProducts() {
        productRepository.saveAll(Arrays.asList(p1, p2, p3));
        List<Product> foundZuzu = productRepository.findByProductNameContaining("Zuzu");
        assertEquals(2, foundZuzu.size()); // "Lapte Zuzu", "Iaurt de băut Zuzu"
        assertTrue(foundZuzu.contains(p1));
        assertTrue(foundZuzu.contains(p3));

        List<Product> foundLapte = productRepository.findByProductNameContaining("lapte");
        assertEquals(1, foundLapte.size());
        assertTrue(foundLapte.contains(p1));

        List<Product> emptyResult = productRepository.findByProductNameContaining("inexistent");
        assertTrue(emptyResult.isEmpty());

        assertTrue(productRepository.findByProductNameContaining(null).isEmpty());
        // Comportamentul pentru string gol depinde de implementare, aici am făcut să returneze listă goală
        assertTrue(productRepository.findByProductNameContaining("  ").isEmpty());
    }
}