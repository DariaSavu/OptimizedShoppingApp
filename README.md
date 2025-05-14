# Price Comparator - Market Backend

This project is the backend for a "Price Comparator - Market" application.

## Data Models and Normalization

To accuratly perform comparisons between products, they need to have the same unit when it comes to packaging. This is why i choose KG,L, and PIECE as the base units and convert G and ML to KG and L.

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
    *   **Attributes**: `Product product` (a reference to the `Product` object), `storeName` (String), `entryDate` (LocalDate), `price` (double), `currency` (String).
    *   **Derived Value**:
        *   `pricePerNormalizedUnit`: Calculated by dividing the `price` by the `product.getNormalizedQuantity()`. This allows for direct "value-for-money" comparisons (e.g., price per kg, price per liter, price per piece).

3.  **`Discount.java`**:
    *   Represents a discount offer for a `Product` at a `storeName`.
    *   **Attributes**: `Product product`, `storeName`, `startDate`, `endDate`, `discountPercentage`, `discountObservationDate` (the date from the discount CSV filename).
    *   Includes a method `isActiveOnDate(LocalDate)` to check if the discount is currently active.

4.  **`User.java`**:
    *   Represents a user of the application. This entity is foundational for user-specific features like shopping lists and price alerts, although user management (authentication, registration) is not part of the current CSV-based data loading.
    *   **Attributes**:
        *   `userId` (Integer): A unique identifier for the user.
        *   `username` (String): The user's chosen username.
        *   `firstName` (String): The user's first name.
        *   `lastName` (String): The user's last name.
    *   This model provides basic user information.

5.  **`UnitType.java` (Enum)**:
    *   Defines the recognized units of measure (e.g., `LITER`, `MILLILITER`, `KILOGRAM`, `GRAM`, `PIECE`).
    *   **Responsibilities**:
        *   Mapping string representations from CSV files (e.g., "l", "ml", "kg", "g", "buc") to their corresponding enum constants.
        *   Providing the logic to determine the `baseUnitType` (e.g., `MILLILITER` normalizes to `LITER`).
        *   Providing the logic to convert a `packageQuantityInput` to its `normalizedValue` (e.g., 500ml becomes 0.5).
    *   Currently supports: `LITER (l)`, `MILLILITER (ml)`, `KILOGRAM (kg)`, `GRAM (g)`, `PIECE (buc)`. Unrecognized units from CSVs are currently logged as warnings and may default to `PIECE` for the `Product`'s `packageUnitInput` to allow processing, or handled as per error strategy.
