package org.example.shoppingapp.service;

import org.example.shoppingapp.model.PriceAlert;
import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.User;
import org.example.shoppingapp.model.dto.TriggeredAlertDTO;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.example.shoppingapp.repository.interfaces.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PriceAlertService {
    private static final Logger logger = LoggerFactory.getLogger(PriceAlertService.class);

    private final Map<Integer, List<PriceAlert>> userAlerts = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;

    public PriceAlertService(UserRepository userRepository,
                             ProductRepository productRepository,
                             PriceEntryRepository priceEntryRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    public boolean setAlert(Integer userId, String productId, double targetPrice) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (userOpt.isEmpty()) {
            logger.warn("Cannot set alert. User with ID {} not found.", userId);
            return false;
        }
        if (productOpt.isEmpty()) {
            logger.warn("Cannot set alert. Product with ID {} not found.", productId);
            return false;
        }
        if (targetPrice <= 0) {
            logger.warn("Cannot set alert. Target price {} must be positive.", targetPrice);
            return false;
        }

        Product product = productOpt.get();
        PriceAlert newAlert = new PriceAlert(userId, product, targetPrice, true, LocalDate.now());

        userAlerts.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()))
                  .removeIf(alert -> alert.getProduct().getProductId().equals(productId));
        userAlerts.get(userId).add(newAlert);

        logger.info("Price alert set for User ID {}, Product ID {}, Target Price: {}", userId, productId, targetPrice);
        return true;
    }
    
    public List<PriceAlert> getAlertsForUser(Integer userId) {
        return new ArrayList<>(userAlerts.getOrDefault(userId, Collections.emptyList()));
    }


    public List<TriggeredAlertDTO> checkTriggeredAlerts() {
        LocalDate today = LocalDate.now();
        List<TriggeredAlertDTO> triggered = new ArrayList<>();

        userAlerts.forEach((userId, alerts) -> {
            alerts.stream()
                .filter(PriceAlert::isActive)
                .forEach(alert -> {
                    Product product = alert.getProduct();

                    Optional<PriceEntry> cheapestCurrentPriceOpt = priceEntryRepository
                            .findByProductId(product.getProductId()).stream()
                            .filter(pe -> !pe.getEntryDate().isBefore(today.minusDays(7)))
                            .min(Comparator.comparingDouble(PriceEntry::getPrice));

                    cheapestCurrentPriceOpt.ifPresent(cheapestEntry -> {
                        if (cheapestEntry.getPrice() <= alert.getTargetPrice()) {
                            triggered.add(new TriggeredAlertDTO(
                                    userId,
                                    product.getProductId(),
                                    product.getProductName(),
                                    product.getBrand(),
                                    cheapestEntry.getStoreName(),
                                    BigDecimal.valueOf(cheapestEntry.getPrice()),
                                    BigDecimal.valueOf(alert.getTargetPrice())
                            ));
                        }
                    });
                });
        });
        if(!triggered.isEmpty()){
            logger.info("Found {} triggered price alerts.", triggered.size());
        }
        return triggered;
    }
    
     public boolean removeAlert(Integer userId, String productId) {
        List<PriceAlert> alertsForUser = userAlerts.get(userId);
        if (alertsForUser != null) {
            boolean removed = alertsForUser.removeIf(alert -> alert.getProduct().getProductId().equals(productId) && alert.isActive());
            if (removed) {
                logger.info("Price alert removed for User ID {}, Product ID {}", userId, productId);
                if (alertsForUser.isEmpty()) {
                    userAlerts.remove(userId);
                }
            }
            return removed;
        }
        return false;
    }
}