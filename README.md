<h1 align="center">Minibank System</h1>

<p align="center">
  Built with Spring Boot, JPA, and H2
</p>

<p align="center">
  <strong>Developed by Reti Antonio</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Hibernate-JPA-59666C?style=flat-square&logo=hibernate" alt="Hibernate">
  <img src="https://img.shields.io/badge/Security-Pessimistic_Locking-red?style=flat-square" alt="Security">
</p>

This project manages monetary transfers, account balances, and transaction history with high precision, transactional integrity, and strict business rule enforcement.

## 🏗️ Data Models & Domain "Perks"

The system is built around three core entities, each enforcing specific banking regulations and technical guards:

### 1. Account Entity
* **IBAN Validation:** Implements a strict validator for International Bank Account Numbers, supporting varying lengths (e.g., RO: 24, BR: 29) and country-specific checksums.
* **SEPA Compatibility:** Designed to recognize and validate SEPA-zone country codes, ensuring standard-compliant routing.
* **Polymorphism:** Supports different account types (e.g., `CHECKING`, `SAVINGS`) using a clean inheritance strategy.
* **Balance Protection:** Enforced via database constraints and Java validation to ensure an account never enters an illegal negative state.


### 2. Transfer Entity
* **Idempotency Shield:** Every transfer requires a unique `Idempotency-Key`. This prevents duplicate charges if a user clicks "Send" twice or if a network retry occurs.
* **Daily Savings Limit:** Enforces business logic where specific account types (Savings) are restricted to a maximum number of transfers or a maximum total amount within a 24-hour UTC window.
* **Cross-Currency Engine:** Automatically detects if the source and target accounts belong to different currencies, triggering the exchange rate engine to calculate the `convertedAmount` and `exchangeRate`.
* **Atomic Collision Handling:** Employs **Pessimistic Write Locking** at the database level to serialize balance updates. This ensures that concurrent transfers involving the same account are queued, preventing race conditions and "Double Spend" anomalies.

### 3. Transaction Entity
* **Directional Signage:** Automatically handles the signage logic where `TRANSFER_OUT` and `WITHDRAWAL` are recorded as **negative** values, while `TRANSFER_IN` and `DEPOSIT` are **positive**.
* **Audit Trail:** Captures the `balanceAfter` at the exact moment of the transaction, providing a reliable historical audit trail.
* **Dynamic Filtering:** Integrated with **JPA Specifications** to allow filtering by IBAN (source or target) and date ranges.
* **External Transaction Logic:** For `DEPOSIT` and `WITHDRAW` operations, the system correctly handles **null IBANs** for the counterparty, signifying a transaction with an external source or physical cash.

## 🚀 Key Features

### Financial Integrity & Precision
* **Decimal Accuracy:** Uses `BigDecimal` for all monetary calculations to prevent floating-point errors common with `double` or `float`.
* **Transaction Ledger:** Every transfer generates a double-entry ledger (Source: NEGATIVE, Target: POSITIVE) to ensure the account history accurately reflects balance changes.
* **Global Time Standard:** Uses `Instant` (UTC) for all timestamps to ensure consistency across different time zones and daylight savings changes.


### The Security Layer
* **Concurrency Control:** Implements **Pessimistic Locking** on account rows during transfers to prevent "Double Spend" attacks and race conditions.
* **Input Validation:** Strict validation using Jakarta Validation annotations (`@Positive`, `@NotNull`, `@NotBlank`) and custom IBAN checksum verification.
* **Transactional Integrity:** All multi-step operations are wrapped in `@Transactional` blocks, ensuring an "all-or-nothing" approach to data persistence.

## 🛠️ Tech Stack
* **Java 21**
* **Spring Boot 4.0.3**
* **Spring Data JPA** (Hibernate 7)
* **H2 Database** (In-memory for evaluation)
* **Jackson** (JSON Serialization/Deserialization)

## 📋 API Highlights

### Accounts
`POST /api/accounts`
{
  "ownerName": "Andrei Savings",
  "iban": "RO94BTRLEURCRT0566398301",
  "currency": "EUR",
  "accountType": "SAVINGS"
}

`GET /api/accounts/{id}`

`GET /api/accounts`


### Transfers
`POST /api/transfers`
{
  "sourceIban": "RO98BTRLUSDCRT0566398301",
  "targetIban": "RO47BTRLRONCRT0566398301",
  "amount": 100.00,
  "idempotencyKey" : "txn-abc-001"
}

`GET /api/transfers/{id}`

`GET /api/transfers?iban={iban}&fromDate={iso_date}&toDate={iso_date}`

### Transaction History
`GET /api/accounts/{id}/transactions`

### Exchange Rates
`GET /api/exchange-rates`
* Returns current rates loaded from `exchange-rates.yml` at application startup.

## 🧪 Performance & Concurrency Testing
The system has been stress-tested for race conditions. Using concurrent requests (via Postman Performance Testing), the **Pessimistic Locking** ensures that even if 10 requests hit the server at the same millisecond, the account balance remains consistent, and only valid transactions are processed.