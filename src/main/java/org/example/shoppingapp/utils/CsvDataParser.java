package org.example.shoppingapp.utils;

import org.example.shoppingapp.model.Discount;
import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.User;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CsvDataParser {
    private static final Logger logger = LoggerFactory.getLogger(CsvDataParser.class);
    private static final String CSV_DELIMITER = ";";
    private static final DateTimeFormatter FILENAME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISCOUNT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ProductRepository productRepository;

    public CsvDataParser(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ParsedFileData parseAllDataFromFile(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString().toLowerCase();
        List<Product> newOrUpdatedProducts = new ArrayList<>();
        List<PriceEntry> priceEntries = new ArrayList<>();
        List<Discount> discounts = new ArrayList<>();

        if (fileName.contains("_discounts_")) {
            discounts.addAll(parseDiscountFile(filePath, newOrUpdatedProducts));
        } else if (fileName.matches("^[a-zA-Z0-9]+_\\d{4}-\\d{2}-\\d{2}\\.csv$")) {
            priceEntries.addAll(parsePriceFile(filePath, newOrUpdatedProducts));
        } else {
            logger.warn("Skipping file with unrecognized name format: {}", fileName);
        }
        return new ParsedFileData(newOrUpdatedProducts, priceEntries, discounts);
    }


    private List<PriceEntry> parsePriceFile(Path filePath, List<Product> newOrUpdatedProductsCollector) throws IOException {
        List<PriceEntry> parsedEntries = new ArrayList<>();
        String fileName = filePath.getFileName().toString();
        logger.info("Parsing price file: {}", fileName);

        String[] nameParts = fileName.replace(".csv", "").split("_");
        String storeName = nameParts[0];
        LocalDate entryDate;
        try {
            entryDate = LocalDate.parse(nameParts[nameParts.length -1], FILENAME_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Could not parse date from price file name: {}. Error: {}", fileName, e.getMessage());
            return parsedEntries;
        }

        try (InputStream is = Files.newInputStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            reader.readLine(); // Skip header
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split(CSV_DELIMITER, -1);
                if (fields.length < 8) {
                    logger.warn("Skipping malformed line {} in {}: Insufficient fields. Line: {}", lineNumber, fileName, line);
                    continue;
                }
                try {
                    String productId = fields[0].trim();
                    String productName = fields[1].trim();
                    String productCategory = fields[2].trim();
                    String brand = fields[3].trim();
                    double packageQuantity = Double.parseDouble(fields[4].trim());
                    String packageUnitString = fields[5].trim();
                    double price = Double.parseDouble(fields[6].trim().replace(",","."));
                    String currency = fields[7].trim();

                    Optional<Product> existingProductOpt = productRepository.findById(productId);
                    Product product;
                    if (existingProductOpt.isPresent()) {
                        product = existingProductOpt.get();
                    } else {
                        product = new Product(productId, productName, productCategory, brand, packageQuantity, packageUnitString);
                        newOrUpdatedProductsCollector.add(product);
                    }

                    PriceEntry priceEntry = new PriceEntry(product, storeName, entryDate, price, currency);
                    parsedEntries.add(priceEntry);

                } catch (Exception e) {
                    logger.error("Error parsing line {} in price file {}: {}. Line: {}", lineNumber, fileName, e.getMessage(), line, e);
                }
            }
        }
        logger.info("Parsed {} price entries from {}", parsedEntries.size(), fileName);
        return parsedEntries;
    }

    private List<Discount> parseDiscountFile(Path filePath, List<Product> newOrUpdatedProductsCollector) throws IOException {
        List<Discount> parsedDiscounts = new ArrayList<>();
        String fileName = filePath.getFileName().toString();
        logger.info("Parsing discount file: {}", fileName);

        String[] nameParts = fileName.replace(".csv", "").split("_");
        String storeName = nameParts[0];
        LocalDate observationDate;
        try {
            observationDate = LocalDate.parse(nameParts[nameParts.length -1], FILENAME_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Could not parse date from discount file name: {}. Error: {}", fileName, e.getMessage());
            return parsedDiscounts;
        }

        try (InputStream is = Files.newInputStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            reader.readLine(); // Skip header
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split(CSV_DELIMITER, -1);
                if (fields.length < 9) {
                    logger.warn("Skipping malformed line {} in {}: Insufficient fields. Line: {}", lineNumber, fileName, line);
                    continue;
                }
                try {
                    String productId = fields[0].trim();
                    String productName = fields[1].trim();
                    String brand = fields[2].trim();
                    double packageQuantity = Double.parseDouble(fields[3].trim());
                    String packageUnitString = fields[4].trim();
                    String productCategory = fields[5].trim();
                    LocalDate fromDate = LocalDate.parse(fields[6].trim(), DISCOUNT_DATE_FORMATTER);
                    LocalDate toDate = LocalDate.parse(fields[7].trim(), DISCOUNT_DATE_FORMATTER);
                    double percentage = Double.parseDouble(fields[8].trim());

                    Optional<Product> existingProductOpt = productRepository.findById(productId);
                    Product product;
                    if (existingProductOpt.isPresent()) {
                        product = existingProductOpt.get();
                    } else {
                        product = new Product(productId, productName, productCategory, brand, packageQuantity, packageUnitString);
                        newOrUpdatedProductsCollector.add(product);
                        logger.warn("Product with ID {} created from discount file {} data.", productId, fileName);
                    }

                    Discount discount = new Discount(product, storeName, fromDate, toDate, percentage, observationDate);
                    parsedDiscounts.add(discount);

                } catch (Exception e) {
                    logger.error("Error parsing line {} in discount file {}: {}. Line: {}", lineNumber, fileName, e.getMessage(), line, e);
                }
            }
        }
        logger.info("Parsed {} discounts from {}", parsedDiscounts.size(), fileName);
        return parsedDiscounts;
    }

    public List<User> parseUsersFile(Path filePath) throws IOException {
        List<User> parsedUsers = new ArrayList<>();
        String fileName = filePath.getFileName().toString();
        logger.info("Parsing users file: {}", fileName);

        try (InputStream is = Files.newInputStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || !headerLine.trim().equalsIgnoreCase("userId;username;firstName;lastName")) {
                logger.error("Invalid or missing header in users file: {}. Expected 'userId;username;firstName;lastName'", fileName);
                return parsedUsers;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split(CSV_DELIMITER, -1);
                if (fields.length < 4) {
                    logger.warn("Skipping malformed line {} in {}: Insufficient fields (expected 4, got {}). Line: {}", lineNumber, fileName, fields.length, line);
                    continue;
                }

                try {
                    Integer userId = Integer.parseInt(fields[0].trim());
                    String username = fields[1].trim();
                    String firstName = fields[2].trim();
                    String lastName = fields[3].trim();

                    if (username.isEmpty()) {
                        logger.warn("Skipping line {} in {}: Username is empty. Line: {}", lineNumber, fileName, line);
                        continue;
                    }

                    User user = new User(userId, username, firstName, lastName);
                    parsedUsers.add(user);

                } catch (NumberFormatException e) {
                    logger.warn("Skipping line {} in {}: Error parsing userId. Line: {}. Error: {}", lineNumber, fileName, line, e.getMessage());
                } catch (Exception e) {
                    logger.error("Unexpected error parsing line {} in {}: {}. Line: {}", lineNumber, fileName, e.getMessage(), line, e);
                }
            }
        }
        logger.info("Parsed {} users from {}", parsedUsers.size(), fileName);
        return parsedUsers;
    }
    public static class ParsedFileData {
        public final List<Product> products;
        public final List<PriceEntry> priceEntries;
        public final List<Discount> discounts;

        public ParsedFileData(List<Product> products, List<PriceEntry> priceEntries, List<Discount> discounts) {
            this.products = products;
            this.priceEntries = priceEntries;
            this.discounts = discounts;
        }
    }
}