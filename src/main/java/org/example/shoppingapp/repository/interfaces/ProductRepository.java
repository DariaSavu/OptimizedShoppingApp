package org.example.shoppingapp.repository.interfaces;

import org.example.shoppingapp.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);

    List<Product> saveAll(Iterable<Product> products);

    Optional<Product> findById(String productId);

    boolean existsById(String productId);
    List<Product> findAll();

    long count();

    void deleteById(String productId);

    void delete(Product product);

    void deleteAll();
    List<Product> findByCategory(String category);

    List<Product> findByBrand(String brand);
    Optional<Product> findByProductNameAndBrand(String productName, String brand);

    List<Product> findByProductNameContaining(String nameSubstring);
}