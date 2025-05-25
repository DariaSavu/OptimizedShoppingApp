package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryProductRepository.class);
    private final Map<String, Product> productCatalog = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        if (product == null || product.getProductId() == null) {
            logger.warn("Attempted to save a null product or product with null ID.");
            throw new IllegalArgumentException("Product or Product ID cannot be null.");
        }
        productCatalog.put(product.getProductId(), product);
        logger.trace("Product saved/updated: {}", product.getProductId());
        return product;
    }

    @Override
    public List<Product> saveAll(Iterable<Product> products) {
        List<Product> savedEntities = new ArrayList<>();
        if (products != null) {
            for (Product product : products) {
                if (product != null) {
                    savedEntities.add(save(product));
                }
            }
        }
        logger.debug("Saved {} products.", savedEntities.size());
        return savedEntities;
    }

    @Override
    public Optional<Product> findById(String productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(productCatalog.get(productId));
    }

    @Override
    public boolean existsById(String productId) {
        if (productId == null) {
            return false;
        }
        return productCatalog.containsKey(productId);
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(productCatalog.values());
    }

    public List<Product> findAllProductsByIds(Iterable<String> productIds) {
        List<Product> result = new ArrayList<>();
        if (productIds != null) {
            for (String id : productIds) {
                findById(id).ifPresent(result::add);
            }
        }
        return result;
    }


    @Override
    public long count() {
        return productCatalog.size();
    }

    @Override
    public void deleteById(String productId) {
        if (productId != null) {
            Product removed = productCatalog.remove(productId);
            if (removed != null) {
                logger.trace("Product deleted by ID: {}", productId);
            }
        }
    }

    @Override
    public void delete(Product product) {
        if (product != null && product.getProductId() != null) {
            deleteById(product.getProductId());
        }
    }

    public void deleteAllGivenProducts(Iterable<Product> products) {
        if (products != null) {
            products.forEach(this::delete);
        }
    }

    @Override
    public void deleteAll() {
        productCatalog.clear();
        logger.info("All products cleared from repository.");
    }

    @Override
    public List<Product> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerCategory = category.trim().toLowerCase();
        return productCatalog.values().stream()
                .filter(product -> product.getProductCategory() != null &&
                        product.getProductCategory().toLowerCase().equals(lowerCategory))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerBrand = brand.trim().toLowerCase();
        return productCatalog.values().stream()
                .filter(product -> product.getBrand() != null &&
                        product.getBrand().toLowerCase().equals(lowerBrand))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findByProductNameAndBrand(String productName, String brand) {
        if (productName == null || productName.trim().isEmpty() || brand == null || brand.trim().isEmpty()) {
            return Optional.empty();
        }
        String lowerProductName = productName.trim().toLowerCase();
        String lowerBrand = brand.trim().toLowerCase();
        return productCatalog.values().stream()
                .filter(product -> product.getProductName() != null &&
                        product.getProductName().toLowerCase().equals(lowerProductName) &&
                        product.getBrand() != null &&
                        product.getBrand().toLowerCase().equals(lowerBrand))
                .findFirst();
    }

    @Override
    public List<Product> findByProductNameContaining(String nameSubstring) {
        if (nameSubstring == null || nameSubstring.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerSubstring = nameSubstring.trim().toLowerCase();
        return productCatalog.values().stream()
                .filter(product -> product.getProductName() != null &&
                        product.getProductName().toLowerCase().contains(lowerSubstring))
                .collect(Collectors.toList());
    }
}