# Minibank System - Backend Implementation

A robust Spring Boot banking application designed with a "Fortress" mindset. This project manages monetary transfers, account balances, and transaction history with high precision and transactional integrity.

## 🚀 Key Features

### 1. Financial Integrity & Precision
* **Decimal Accuracy:** Uses `BigDecimal` for all monetary calculations to prevent floating-point errors common with `double` or `float`.
* **Currency Support:** Full support for multi-currency accounts (EUR, USD, GBP, RON) with automated conversion based on startup-loaded exchange rates.
* **Transaction Ledger:** Every transfer generates a dual-entry ledger (Source: NEGATIVE, Target: POSITIVE) to ensure the sum of all transactions reflects the current balance.

### 2. The "Fortress" Security Layer
* **Concurrency Control:** Implements **Pessimistic Locking** (`SELECT FOR UPDATE`) on account rows during transfers to prevent "Double Spend" attacks and race conditions.
* **Idempotency:** Protects against duplicate network requests using an `Idempotency-Key` mechanism, ensuring a transfer is only processed once.
* **Input Validation:** Strict validation using Jakarta Validation annotations (`@Positive`, `@NotNull`, `@Size`) and custom IBAN checksum verification.

### 3. Advanced Querying & Filtering
* **Dynamic Filtering:** Uses **JPA Specifications** to allow users to filter transaction history by IBAN (as source or target) and specific date ranges (`Instant` UTC).
* **Pagination:** High-performance paginated responses for transaction history to ensure scalability even with thousands of records.

## 🛠️ Tech Stack
* **Java 21**
* **Spring Boot 3.4.x**
* **Spring Data JPA** (Hibernate 7)
* **H2 Database** (In-memory for rapid evaluation)
* **Jackson** (JSON Serialization/Deserialization)

## 📋 API Highlights

### Transfers
`POST /api/transfers`
* Handles cross-currency logic.
* Validates daily savings limits.
* Atomic operations (Rollback on any failure).

### Transaction History
`GET /api/transfers?iban={iban}&fromDate={iso_date}&toDate={iso_date}`
* Returns a paginated list of all transfers matching the criteria.

### Exchange Rates
`GET /api/exchange-rates`
* Returns current rates loaded from `exchange-rates.yml`.

## ⚙️ Configuration
The system loads exchange rates at startup from `src/main/resources/exchange-rates.yml`.
Example:
```yaml
exchange:
  rates:
    EUR: 4.97
    USD: 4.56
    RON: 1.00