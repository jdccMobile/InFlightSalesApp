# Inflight Sales App
An Android application to simulate the management of onboard product sales with support for multiple currencies and discounts by customer type.

## Project Setup
### Requirements
- Android Studio Otter (2025.2.1) 
- Minimum SDK: 31 (Android 12)
- Target SDK: 36 (Android 16)
- Kotlin: 2.2.21
- Java: 11
- Emulator: Pixel 9 (Android 16)

## Architecture
### Clean Architecture + MVVM
The project follows Clean Architecture principles with MVVM pattern:

- Presentation Layer 
  - ViewModels
  - Screens
  - Presentation Models
- Domain Layer
  - Use Cases
  - Domain Models
  - Domain Logic
- Data Layer
  - Local Data Sources
  - Remote Data Sources
  - Repository
  - Mappers
  - Data Models
- Dependency Injection

### Key Design Decisions
- Layered Architecture

The app follows a simple layered architecture (Presentation, Domain, Data) to separate responsibilities. This ensures that UI, business logic, and data handling are independent, making the app easier to maintain, test, and extend.
- State Management with MVVM
 
The app uses MVVM to manage state, keeping a clear separation between UI and business logic. This makes the app easier to maintain and test.
- Room as Source of Truth

Room is used as the single source of truth. Every time the app is opened, it syncs data from the API. This simulates the crew having synced data before the flight, and ensures the app works offline during the flight.
- Simple Error Handling with Arrow

Arrow is used for type-safe functional error handling without relying on exceptions. This simplifies error management and makes it more predictable.
- No Modularization

Modularization was skipped because the app is simple and adding modules would increase complexity. As a result, some elements may not be strictly placed in their intended layer, but this is acceptable for a small project.
- Dependency Injection

DI was added to provide easy access to repositories, data sources, and navigators, improving testability and flexibility.

---

## Running Tests

### Run all unit tests
./gradlew test

### Run specific test class
./gradlew test --tests ProductViewModelTest
./gradlew test --tests ReceiptViewModelTest

### Key Tests
- ProductViewModel
  - Loading state updates correctly after products are loaded 
  - Filtering products by category 
  - Adding products to the cart updates cart items and summary 
- ReceiptViewModel
  - Products and total are loaded correctly
  - Cash payment validation and success flow 
  - Card payment validation with incomplete data 
  - Stock updates after successful payments 

---

### Assumptions
- The minimum supported Android version could be restrictive, but this app is intended for private use, so it is not expected to be an issue.
- Internet connection is required only for the initial sync, simulating the crew having synced all data before the flight; the app works offline afterward.
- The app assumes there is only one crew member using it, so stock updates from other users or devices are not handled.

### Known Limitations
- The app is portrait-only; rotating the device may cause layout issues.
- Designed for light mode; dark mode may look bad.
- Field validations are not fully implemented.
- Detailed error messages are not shown to the user.
- Accessibility is not fully optimized in some parts of the app.
- Payments are simulated with a delay, not real transactions.
- No retry mechanism if the network connection fails.
- Database is simulated using My JSON Server.
