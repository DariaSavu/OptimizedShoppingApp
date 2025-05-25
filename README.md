# Price Comparator - Market Backend

This project is the backend for a "Price Comparator - Market" application.

## Data Models and Normalization

To accurately perform comparisons between products, they need to have the same unit when it comes to packaging. This is why KG, L, and PIECE are chosen as the base units, and G and ML are converted to KG and L respectively.

### Core Data Entities

The main data entities are defined in the `org.example.shoppingapp.model` package:

1.  **`Product.java`**:
    *   Represents a unique stock-keeping unit (SKU).
    *   **Attributes**: `productId`, `productName`, `productCategory`, `brand`, `packageQuantityInput` (original quantity from CSV), `packageUnitInput` (original unit from CSV as `UnitType` enum).
    *   **Normalization**: This class is responsible for normalizing the product's quantity and unit to a consistent base for comparison.
        *   `normalizedQuantity`: The quantity converted to its base unit (e.g., 500g becomes 0.5).
        *   `normalizedUnitType`: The base `UnitType` (e.g., `KILOGRAM`, `LITER`, `PIECE`).
    *   The `productId` is considered the unique identifier for a product across different stores and data files if it refers to the exact same item and package.

2.  **`PriceEntry.java`**:
    *   Represents the price of a specific `Product` at a given `storeName` on a particular `entryDate`.
    *   **Attributes**: `Product product` (a reference to the `Product` object), `storeName` (String), `entryDate` (LocalDate), `price` (double), `currency` (Enum `Currency`).
    *   **Derived Value**:
        *   `pricePerNormalizedUnit`: Calculated by dividing the `price` by the `product.getNormalizedQuantity()`. This allows for direct "value-for-money" comparisons (e.g., price per kg, price per liter, price per piece).

3.  **`Discount.java`**:
    *   Represents a discount offer for a `Product` at a `storeName`.
    *   **Attributes**: `Product product`, `storeName`, `startDate`, `endDate`, `discountPercentage`, `discountObservationDate` (the date from the discount CSV filename).
    *   Includes a method `isActiveOnDate(LocalDate)` to check if the discount is currently active.

4.  **`User.java`**:
    *   Represents a user of the application. This entity is foundational for user-specific features like shopping lists and price alerts. User data is loaded from `users.csv`.
    *   **Attributes**: `userId` (Integer), `username` (String), `firstName` (String), `lastName` (String).

5.  **`UnitType.java` (Enum)**:
    *   Defines the recognized units of measure (`LITER`, `MILLILITER`, `KILOGRAM`, `GRAM`, `PIECE`).
    *   Handles mapping from CSV strings (e.g., "l", "g") and provides logic for normalization to base units.
    *   Unrecognized units from CSVs are logged, and product normalization might be skipped for such cases.

6.  **`Currency.java` (Enum)**:
    *   Defines supported currencies (e.g., `RON`). Currently, only RON is used based on sample data.

7.  **`PriceAlert.java`**:
    *   Represents a price alert set by a `User` for a `Product` at a specific `targetPrice`.
    *   **Attributes**: `userId`, `product`, `targetPrice`, `active`, `dateCreated`.

## Repositories

The data access layer is defined by interfaces in `org.example.shoppingapp.repository.interfaces` and implemented as in-memory stores in `org.example.shoppingapp.repository.inmemory`. This approach promotes loose coupling and testability.

*   **`ProductRepository`**: Manages `Product` entities.
*   **`UserRepository`**: Manages `User` entities.
*   **`PriceEntryRepository`**: Manages `PriceEntry` entities, storing price history.
*   **`DiscountRepository`**: Manages `Discount` entities.

Each in-memory repository uses appropriate Java Collections (e.g., `ConcurrentHashMap`, `synchronizedList`) to store data and provides methods for CRUD operations and specific queries.

## Data Parsing (CSV)

*   **`CsvDataParser.java`** (in `org.example.shoppingapp.parser` or `org.example.shoppingapp.utils`): This class is responsible for reading and parsing data from CSV files.
    *   It handles different CSV formats for product prices, discounts, and users.
    *   It converts raw string data into `Product`, `PriceEntry`, `Discount`, and `User` model objects.
    *   It collaborates with `ProductRepository` to ensure `Product` instances are unique and reused across different data entries.
    *   The parser is designed to return lists of parsed objects, which are then saved by the `DataLoadingService`.

## Business Services

The business logic is encapsulated in service classes located in `org.example.shoppingapp.service`. These services use the repositories to access data and implement the core features.

*   **`DataLoadingService.java`**: Orchestrates the initial loading of data. It uses `CsvDataParser` to read all CSV files from a configured directory and populates the respective in-memory repositories. It also handles the initial loading of users from `users.csv`.
*   **`DiscountService.java`**:
    *   `getBestCurrentDiscounts()`: Finds active discounts and sorts them by the highest percentage.
    *   `getNewDiscounts()`: Identifies discounts that were observed recently (based on discount file date) and are currently active.
*   **`PriceHistoryService.java`**:
    *   `getPriceHistory()`: Provides a list of `PriceHistoryDataPointDTO` objects, allowing filtering by product ID, store, category, brand, and date range. This data is suitable for generating price trend graphs.
*   **`ProductRecommendationService.java`**:
    *   `getBestValueProducts()`: Recommends products based on the best "price per normalized unit" within a given category or for a specific product and its category alternatives.
*   **`ShoppingListOptimizerService.java`**:
    *   `optimizeShoppingBasket()`: For a given list of product IDs and quantities, this service (in its current simplified version) finds the store where each product is cheapest and groups items into per-store shopping lists.
*   **`PriceAlertService.java`**:
    *   Manages user-defined price alerts.
    *   `setAlert()`: Allows a (simulated logged-in) user to set a target price for a product.
    *   `getAlertsForUser()`: Lists active alerts for the current user.
    *   `removeAlert()`: Allows a user to remove an alert.
    *   `checkTriggeredAlerts()`: Identifies and returns alerts where the current product price has met or fallen below the target price.

## Command Line Interface (CLI)

A Command Line Interface is provided via the `CliApplicationRunner.java` class (in `org.example.shoppingapp.cli`). This allows users to interact with the backend services and demonstrate the implemented features.

*   **Functionality**: The CLI provides a menu of commands to:
    *   Simulate user login/logout.
    *   List available users.
    *   Access all implemented business features (best discounts, new discounts, price history, best value, optimize basket, set/view/remove/check price alerts).
    *   Reload all data from CSV files.
*   **Usage**: After starting the application, type `help` in the console to see the list of available commands.

## Setup and Running the Application

1.  **Prerequisites**:
    *   Java JDK 17 or higher.
    *   Gradle (the project includes a Gradle wrapper `gradlew`).
2.  **Data Files**:
    *   Create a directory for your CSV data files (e.g., `data_files` in the project root).
    *   Ensure this path is correctly configured in `src/main/resources/application.properties` under the key `app.data.directory` (e.g., `app.data.directory=./data_files`).
    *   Place your product price CSVs (e.g., `storename_date.csv`), discount CSVs (e.g., `storename_discounts_date.csv`), and the `users.csv` file (format: `userId;username;firstName;lastName`) in this directory.
3.  **Build**:
    ```bash
    ./gradlew build
    ```
4.  **Run**:
    *   **Using Gradle:**
        ```bash
        ./gradlew bootRun
        ```
    *   **From IDE:** Locate the `ShoppingAppApplication.java` class and run its `main` method.
    *   **As an executable JAR:**
        1.  Build the JAR: `./gradlew bootJar`
        2.  Run the JAR: `java -jar build/libs/shoppingapp-0.0.1-SNAPSHOT.jar` (adjust JAR name if needed). Ensure the data directory is accessible relative to where you run the JAR or use an absolute path in `application.properties`.

Upon running, the application will load data from the CSVs and present a CLI prompt (`>`).

## Features Implemented

The backend currently implements the following core business requirements:

*   **Best Discounts:** Lists products with the highest current percentage discounts across all tracked stores.
*   **New Discounts:** Lists discounts that have been newly added (e.g., based on the discount file's observation date, considering active discounts).
*   **Dynamic Price History Graphs:** Provides filterable data points (date, price, store) for individual products or groups of products (by category/brand) to allow a frontend to display price trends.
*   **Product Recommendations (Value per Unit):** Highlights products based on their "value per unit" (e.g., price per kg, price per liter) to help identify the best buys, especially when package sizes differ.
*   **Custom Price Alert:** Allows users (simulated via CLI) to set a target price for a product and can identify when a product's price drops to or below that target.
*   **Daily Shopping Basket Monitoring (Simplified):** For a given list of products, it identifies the store where each product is cheapest and groups them, providing a basic cost optimization.

## Technical Stack

*   **Primary Programming Language:** Java (JDK 17)
*   **Framework:** Spring Boot 3
*   **Build Tool:** Gradle
*   **Logging:** SLF4J with Logback
*   **Testing:** JUnit 5, Mockito
*   **Data Storage:** In-memory data structures (Java Collections).
*   **Data Source:** CSV files.

## Project Structure

The project follows a standard layered architecture:
## Sample Data Files

The application expects CSV files for product prices, discounts, and users. Sample CSV files with updated dates (including the current date for active discounts) are used to demonstrate functionality. Refer to the provided sample files in the repository or create your own based on the following schemas:

*   **Product Price Data (`storename_date.csv`)**:
    `product_id;product_name;product_category;brand;package_quantity;package_unit;price;currency`
*   **Discount Data (`storename_discounts_date.csv`)**:
    `product_id;product_name;brand;package_quantity;package_unit;product_category;from_date;to_date;percentage_of_discount`
*   **User Data (`users.csv`)**:
    `userId;username;firstName;lastName`

## Assumptions and Simplifications

*   **In-Memory Storage**: All data is loaded and stored in memory. Data is not persisted across application restarts unless reloaded from CSVs.
*   **User Management**: User "login" in the CLI is a simulation by selecting an existing user from `users.csv`. No authentication or password management is implemented.
*   **Product Uniqueness**: `productId` is assumed to be the primary unique identifier for a product SKU. If the same `productId` appears with different details (name, brand, package) in different files, the latest encountered details might overwrite previous ones during product catalog creation in the parser, or the first one encountered is kept. (Clarify your exact strategy if important).
*   **Discount Price Calculation**: The "original price" for calculating a discounted price in `DiscountService` is based on the most recent price entry found for that product in that store. A more robust system might have a clearer "base price" reference.
*   **"New" Discounts**: Determined by the `discountObservationDate` (derived from the discount CSV filename) and being active. This is an approximation as precise timestamps of discount addition are not available.
*   **Shopping Basket Optimization**: The current implementation is a simplified heuristic (finds the cheapest store for each item individually) and not a global optimization algorithm.
*   **Price Alert Triggering**: In the CLI, `check_alerts` must be run manually. A real application would have a background job or event-driven mechanism.
*   **CSV Format Strictness**: The parser expects CSVs to strictly follow the defined delimiter (`;`) and column order. Error handling for malformed lines is present but might not cover all edge cases.
