package org.example.shoppingapp;

import org.example.shoppingapp.model.User;
import org.example.shoppingapp.model.dto.*;
import org.example.shoppingapp.repository.interfaces.UserRepository;
import org.example.shoppingapp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class CliApplicationRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CliApplicationRunner.class);

    private final DiscountService discountService;
    private final PriceHistoryService priceHistoryService;
    private final ProductRecommendationService productRecommendationService;
    private final ShoppingListOptimizerService shoppingListOptimizerService;
    private final PriceAlertService priceAlertService;
    private final DataLoadingService dataLoadingService;
    private final UserRepository userRepository;

    private User currentUser = null;

    public CliApplicationRunner(DiscountService discountService,
                                PriceHistoryService priceHistoryService,
                                ProductRecommendationService productRecommendationService,
                                ShoppingListOptimizerService shoppingListOptimizerService,
                                PriceAlertService priceAlertService,
                                DataLoadingService dataLoadingService,
                                UserRepository userRepository) {
        this.discountService = discountService;
        this.priceHistoryService = priceHistoryService;
        this.productRecommendationService = productRecommendationService;
        this.shoppingListOptimizerService = shoppingListOptimizerService;
        this.priceAlertService = priceAlertService;
        this.dataLoadingService = dataLoadingService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("CLI Application Runner started.");
        System.out.println("Welcome to the Price Comparator CLI!");
        System.out.println("A command menu has started. Type 'help' for a list of available commands.");
        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            String prompt = currentUser != null ? "[" + currentUser.getUsername() + "] > " : "> ";
            System.out.print("\n" + prompt);
            command = scanner.nextLine().trim().toLowerCase();

            if (command.equals("exit")) {
                break;
            }

            try {
                processCommand(command, scanner);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format. Please enter a valid number.");
                logger.warn("CLI NumberFormatException: {}", e.getMessage());
            }
            catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                logger.error("CLI command processing error", e);
            }
        }
        scanner.close();
        logger.info("CLI Application Runner finished.");
        System.out.println("Exiting application. Goodbye!");
    }

    private void processCommand(String command, Scanner scanner) {
        switch (command) {
            case "help":
                printHelp();
                break;
            case "login":
                handleLogin(scanner);
                break;
            case "logout":
                handleLogout();
                break;
            case "whoami":
                showCurrentUser();
                break;
            case "list_users":
                listUsers();
                break;
            case "best_discounts":
                handleBestDiscounts(scanner);
                break;
            case "new_discounts":
                handleNewDiscounts(scanner);
                break;
            case "price_history":
                handlePriceHistory(scanner);
                break;
            case "best_value":
                handleBestValue(scanner);
                break;
            case "optimize_basket":
                handleOptimizeBasket(scanner);
                break;
            case "set_alert":
                handleSetAlert(scanner);
                break;
            case "my_alerts":
                handleMyAlerts();
                break;
            case "remove_alert":
                handleRemoveAlert(scanner);
                break;
            case "check_alerts":
                handleCheckTriggeredAlerts();
                break;
            case "reload_data":
                System.out.println("Reloading all data from CSV files...");
                dataLoadingService.reloadAllData();
                System.out.println("Data reloaded successfully.");
                break;
            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  User Management:");
        System.out.println("    login                 Log in as a user (by username).");
        System.out.println("    logout                Log out current user.");
        System.out.println("    whoami                Show current logged-in user.");
        System.out.println("    list_users            List available users (from users.csv).");
        System.out.println("  Product & Price Info:");
        System.out.println("    best_discounts        List products with highest current discounts.");
        System.out.println("    new_discounts         List recently added discounts.");
        System.out.println("    price_history         Show price history for products.");
        System.out.println("    best_value            Find best value products (price/unit).");
        System.out.println("  Shopping Features:");
        System.out.println("    optimize_basket       Optimize a shopping basket for cost savings.");
        System.out.println("  Price Alerts (requires login):");
        System.out.println("    set_alert             Set a price alert for a product.");
        System.out.println("    my_alerts             List your active price alerts.");
        System.out.println("    remove_alert          Remove an active price alert.");
        System.out.println("    check_alerts          Check for and display triggered alerts (for all users).");
        System.out.println("  Data Management:");
        System.out.println("    reload_data           Reload all data from CSV files.");
        System.out.println("  General:");
        System.out.println("    help                  Show this help message.");
        System.out.println("    exit                  Exit the application.");
    }

    private void listUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found. Ensure users.csv is loaded.");
            return;
        }
        System.out.println("\nAvailable Users:");
        users.forEach(u -> System.out.printf("ID: %d, Username: %s, Name: %s %s\n",
                u.getUserId(), u.getUsername(), u.getFirstName(), u.getLastName()));
    }

    private void handleLogin(Scanner scanner) {
        if (currentUser != null) {
            System.out.println("You are already logged in as " + currentUser.getUsername() + ". Please 'logout' first.");
            return;
        }
        System.out.print("Enter username to login: ");
        String username = scanner.nextLine().trim();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            System.out.println("Logged in as: " + currentUser.getUsername());
        } else {
            System.out.println("Login failed: User '" + username + "' not found.");
        }
    }

    private void handleLogout() {
        if (currentUser == null) {
            System.out.println("You are not logged in.");
        } else {
            System.out.println("Logged out " + currentUser.getUsername() + ".");
            currentUser = null;
        }
    }

    private void showCurrentUser() {
        if (currentUser != null) {
            System.out.println("Currently logged in as: " + currentUser.getUsername() + " (ID: " + currentUser.getUserId() + ")");
        } else {
            System.out.println("Not logged in.");
        }
    }

    private void handleBestDiscounts(Scanner scanner) {
        System.out.print("Enter limit for best discounts (e.g., 10): ");
        int limit = Integer.parseInt(scanner.nextLine().trim());
        List<DiscountedProductDTO> bestDiscounts = discountService.getBestCurrentDiscounts(limit);
        if (bestDiscounts.isEmpty()) {
            System.out.println("No current discounts found.");
        } else {
            System.out.println("\nBest Current Discounts:");
            bestDiscounts.forEach(System.out::println);
        }
    }

    private void handleNewDiscounts(Scanner scanner) {
        System.out.print("Enter hours ago for 'new' (e.g., 24 for last 24 hours): ");
        int hoursAgo = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter limit (e.g., 10): ");
        int limit = Integer.parseInt(scanner.nextLine().trim());

        List<DiscountedProductDTO> newDiscounts = discountService.getNewDiscounts(hoursAgo, limit);
        if (newDiscounts.isEmpty()) {
            System.out.println("No new discounts found in the last " + hoursAgo + " hours (approx).");
        } else {
            System.out.println("\nNew Discounts (observed in last " + hoursAgo + "h approx.):");
            newDiscounts.forEach(System.out::println);
        }
    }

    private void handlePriceHistory(Scanner scanner) {
        System.out.print("Enter Product ID (optional, press Enter to skip if filtering by category/brand): ");
        String productId = scanner.nextLine().trim();

        System.out.print("Filter by Store Name (optional, press Enter to skip): ");
        String storeNameStr = scanner.nextLine().trim();
        Optional<String> storeNameOpt = storeNameStr.isEmpty() ? Optional.empty() : Optional.of(storeNameStr);

        System.out.print("Filter by Category (optional, press Enter to skip): ");
        String categoryStr = scanner.nextLine().trim();
        Optional<String> categoryOpt = categoryStr.isEmpty() ? Optional.empty() : Optional.of(categoryStr);

        System.out.print("Filter by Brand (optional, press Enter to skip): ");
        String brandStr = scanner.nextLine().trim();
        Optional<String> brandOpt = brandStr.isEmpty() ? Optional.empty() : Optional.of(brandStr);

        System.out.print("From Date (YYYY-MM-DD, optional, press Enter to skip): ");
        String fromDateStr = scanner.nextLine().trim();
        Optional<LocalDate> fromDateOpt = Optional.empty();
        if (!fromDateStr.isEmpty()) {
            try { fromDateOpt = Optional.of(LocalDate.parse(fromDateStr)); }
            catch (DateTimeParseException e) { System.out.println("Invalid from_date format. Use YYYY-MM-DD."); return; }
        }

        System.out.print("To Date (YYYY-MM-DD, optional, press Enter to skip): ");
        String toDateStr = scanner.nextLine().trim();
        Optional<LocalDate> toDateOpt = Optional.empty();
        if (!toDateStr.isEmpty()) {
            try { toDateOpt = Optional.of(LocalDate.parse(toDateStr)); }
            catch (DateTimeParseException e) { System.out.println("Invalid to_date format. Use YYYY-MM-DD."); return; }
        }

        if (productId.isEmpty() && categoryOpt.isEmpty() && brandOpt.isEmpty()){
            System.out.println("Please provide at least a Product ID, Category, or Brand for price history.");
            return;
        }

        List<PriceHistoryDataPointDTO> history = priceHistoryService.getPriceHistory(
                productId.isEmpty() ? null : productId,
                storeNameOpt, categoryOpt, brandOpt, fromDateOpt, toDateOpt);

        if (history.isEmpty()) {
            System.out.println("No price history found for the given criteria.");
        } else {
            System.out.println("\nPrice History:");
            history.forEach(System.out::println);
        }
    }

    private void handleBestValue(Scanner scanner) {
        System.out.print("Enter Product ID (optional, for specific product and its category): ");
        String productIdStr = scanner.nextLine().trim();
        Optional<String> productIdOpt = productIdStr.isEmpty() ? Optional.empty() : Optional.of(productIdStr);

        Optional<String> categoryOpt = Optional.empty();
        if (productIdOpt.isEmpty()) {
            System.out.print("Enter Category Name (if no Product ID provided): ");
            String categoryStr = scanner.nextLine().trim();
            if (!categoryStr.isEmpty()) {
                categoryOpt = Optional.of(categoryStr);
            }
        }

        if (productIdOpt.isEmpty() && categoryOpt.isEmpty()) {
            System.out.println("Please provide either a Product ID or a Category Name.");
            return;
        }

        System.out.print("Enter limit (e.g., 5): ");
        int limit = Integer.parseInt(scanner.nextLine().trim());

        List<ProductRecommendationDTO> recommendations = productRecommendationService.getBestValueProducts(productIdOpt, categoryOpt, limit);
        if (recommendations.isEmpty()) {
            System.out.println("No recommendations found for the given criteria.");
        } else {
            System.out.println("\nBest Value Products (Price/Unit):");
            recommendations.forEach(System.out::println);
        }
    }

    private void handleOptimizeBasket(Scanner scanner) {
        Map<String, Integer> basket = new HashMap<>();
        System.out.println("Enter products for your basket (ProductId Quantity). Type 'done' when finished.");
        while (true) {
            System.out.print("Add item (e.g., P001 2) or 'done': ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("done")) {
                break;
            }
            String[] parts = line.split("\\s+");
            if (parts.length == 2) {
                try {
                    basket.put(parts[0].toUpperCase(), Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity. Please use a number.");
                }
            } else {
                System.out.println("Invalid format. Use: ProductId Quantity");
            }
        }

        if (basket.isEmpty()) {
            System.out.println("Basket is empty. Nothing to optimize.");
            return;
        }

        List<OptimizedShoppingListDTO> optimizedLists = shoppingListOptimizerService.optimizeShoppingBasket(basket);
        if (optimizedLists.isEmpty()) {
            System.out.println("Could not generate optimized shopping lists (maybe no products found or priced).");
        } else {
            System.out.println("\nOptimized Shopping Lists:");
            BigDecimal totalCost = BigDecimal.ZERO;
            for(OptimizedShoppingListDTO listDto : optimizedLists) {
                System.out.print(listDto); // toString() in DTO is formatted for multi-line
                if (!listDto.getStoreName().equals("Items_Not_Found_Or_Priced")) {
                    totalCost = totalCost.add(listDto.getStoreTotalCost());
                }
            }
            System.out.printf("\nEstimated Total Cost for Priced Items: %.2f\n", totalCost);
        }
    }

    private void handleSetAlert(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("You must be logged in to set an alert. Use 'login' command.");
            return;
        }
        System.out.print("Enter Product ID for alert: ");
        String productId = scanner.nextLine().trim();
        System.out.print("Enter Target Price (e.g., 8.50): ");
        double targetPrice = Double.parseDouble(scanner.nextLine().trim());

        boolean success = priceAlertService.setAlert(currentUser.getUserId(), productId, targetPrice);
        if (success) {
            System.out.println("Price alert set successfully for product " + productId + " at target price " + String.format("%.2f", targetPrice));
        } else {
            System.out.println("Failed to set price alert. Check product ID and ensure target price is positive.");
        }
    }

    private void handleMyAlerts() {
        if (currentUser == null) {
            System.out.println("You must be logged in to view your alerts. Use 'login' command.");
            return;
        }
        List<org.example.shoppingapp.model.PriceAlert> alerts = priceAlertService.getAlertsForUser(currentUser.getUserId());
        if (alerts.isEmpty()) {
            System.out.println("You have no active price alerts.");
        } else {
            System.out.println("\nYour Active Price Alerts:");
            alerts.stream().filter(org.example.shoppingapp.model.PriceAlert::isActive).forEach(System.out::println);
        }
    }

    private void handleRemoveAlert(Scanner scanner){
        if (currentUser == null) {
            System.out.println("You must be logged in to remove an alert. Use 'login' command.");
            return;
        }
        System.out.print("Enter Product ID of the alert to remove: ");
        String productId = scanner.nextLine().trim();
        boolean removed = priceAlertService.removeAlert(currentUser.getUserId(), productId);
        if(removed){
            System.out.println("Alert for product " + productId + " removed successfully.");
        } else {
            System.out.println("No active alert found for product " + productId + " for your user, or removal failed.");
        }
    }

    private void handleCheckTriggeredAlerts() {
        System.out.println("Checking for triggered alerts (for all users)...");
        List<TriggeredAlertDTO> triggeredAlerts = priceAlertService.checkTriggeredAlerts();
        if (triggeredAlerts.isEmpty()) {
            System.out.println("No alerts have been triggered recently.");
        } else {
            System.out.println("\nTriggered Price Alerts:");
            triggeredAlerts.forEach(System.out::println);
        }
    }
}