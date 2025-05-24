package org.example.shoppingapp.service;

import org.example.shoppingapp.model.User;
import org.example.shoppingapp.repository.interfaces.DiscountRepository;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.example.shoppingapp.repository.interfaces.UserRepository;
import org.example.shoppingapp.utils.CsvDataParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DataLoadingService {
    private static final Logger logger = LoggerFactory.getLogger(DataLoadingService.class);

    private final CsvDataParser csvDataParser;
    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final DiscountRepository discountRepository;
    private final UserRepository userRepository;

    @Value("${app.data.directory:./data}")
    private String dataDirectoryPath;

    public DataLoadingService(CsvDataParser csvDataParser,
                              ProductRepository productRepository,
                              PriceEntryRepository priceEntryRepository,
                              DiscountRepository discountRepository,
                              UserRepository userRepository) {
        this.csvDataParser = csvDataParser;
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
        this.discountRepository = discountRepository;
        this.userRepository = userRepository;
    }

    private void loadUsersFromCsv() {
        Path usersFilePath = Paths.get(dataDirectoryPath, "users.csv");
        if (!Files.exists(usersFilePath)) {
            logger.info("Users file (users.csv) not found in {}. No users will be loaded from CSV.", dataDirectoryPath);
            return;
        }

        logger.info("Loading users from users.csv...");
        try {
            List<User> usersFromCsv = csvDataParser.parseUsersFile(usersFilePath);
            int usersAdded = 0;
            for (User userFromFile : usersFromCsv) {
                if (userRepository.findById(userFromFile.getUserId()).isEmpty() &&
                        userRepository.findByUsername(userFromFile.getUsername()).isEmpty()) {

                    userRepository.save(userFromFile);
                    usersAdded++;
                    logger.debug("Saved user from CSV: ID={}, Username={}, Name={} {}",
                            userFromFile.getUserId(), userFromFile.getUsername(),
                            userFromFile.getFirstName(), userFromFile.getLastName());
                } else {
                    logger.warn("User with ID {} or Username {} from CSV already exists. Skipping.",
                            userFromFile.getUserId(), userFromFile.getUsername());
                }
            }
            logger.info("{} new users loaded from users.csv. Total users in repository: {}", usersAdded, userRepository.count());

        } catch (IOException e) {
            logger.error("Error loading users from users.csv: {}", e.getMessage(), e);
        }
    }
    @PostConstruct
    public void loadInitialData() {
        logger.info("Starting initial data load from directory: {}", dataDirectoryPath);
        Path dataDir = Paths.get(dataDirectoryPath);

        if (!Files.exists(dataDir) || !Files.isDirectory(dataDir)) {
            logger.error("Data directory not found or is not a directory: {}", dataDirectoryPath);
            return;
        }

        try (Stream<Path> paths = Files.walk(dataDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                 .sorted()
                 .forEach(this::processFile);
        } catch (IOException e) {
            logger.error("Error reading data directory: {}", dataDirectoryPath, e);
        }

        loadUsersFromCsv();

        logger.info("Initial data load finished.");
        logger.info("Total products loaded: {}", productRepository.count());
        logger.info("Total price entries loaded: {}", priceEntryRepository.findAll().size());
        logger.info("Total discounts loaded: {}", discountRepository.findAll().size());
        logger.info("Total users loaded: {}", userRepository.count());

    }

    private void processFile(Path filePath) {
        logger.info("Processing file: {}", filePath.getFileName());
        try {
            CsvDataParser.ParsedFileData parsedData = csvDataParser.parseAllDataFromFile(filePath);
            
            if (!parsedData.products.isEmpty()) {
                productRepository.saveAll(parsedData.products);
                logger.debug("Saved {} new/updated products from {}", parsedData.products.size(), filePath.getFileName());
            }
            if (!parsedData.priceEntries.isEmpty()) {
                priceEntryRepository.saveAll(parsedData.priceEntries);
                logger.debug("Saved {} price entries from {}", parsedData.priceEntries.size(), filePath.getFileName());
            }
            if (!parsedData.discounts.isEmpty()) {
                discountRepository.saveAll(parsedData.discounts);
                logger.debug("Saved {} discounts from {}", parsedData.discounts.size(), filePath.getFileName());
            }
        } catch (IOException e) {
            logger.error("Error processing file {}: {}", filePath.getFileName(), e.getMessage(), e);
        }
    }

    public void reloadAllData() {
        logger.info("Reloading all data...");
        productRepository.deleteAll();
        priceEntryRepository.deleteAll();
        discountRepository.deleteAll();

        loadInitialData();
        logger.info("Data reload complete.");
    }
}