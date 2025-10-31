## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation & Setup](#installation-and-setup)
- [Making POST Calls](#making-post-calls)
- [Fraud Detection Rules](#fraud-detection-rules)
- [Kafka Integration](#kafka-integration)

## Overview

This application is meant to demo a fraud detection system used to identify suspicious transactions. When a transaction is submitted, the system analyzes it against multiple fraud detection rules, calculates a fraud score, and if fraudulent, publishes an alert to Apache Kafka.
**Use Case**: This system helps identify potentially fraudulent activities such as:
- Unusually high transaction amounts
- Rapid-fire transactions
- Transactions from multiple geographic locations in a short time
- Off-hours transactions that deviate from normal patterns

## Features

- **Real-Time Fraud Detection**: Analysis of transactions is done through configured rules
- **Event-Driven Architecture**: Integrates Kafka for fraud alert processing
- **RESTful API**: Clean REST endpoints for transaction processing
- **Repository Layer**: Transactions and fraudulent transactions are stored in PostgreSQL
- **Database Migration**: Liquibase for version-controlled schema management
- **Unit Testing**: Unit tests for the service classes written in JUnit
- **Easy Setup**: Docker Compose allows the user to launch the application without the need for downloading Kafka or PostgreSQL

## Technologies Used

| Technology | Purpose |
|------------|---------|
| **Java 21** | Programming language |
| **Spring Boot 3.5.5** | Application framework |
| **Spring Data JPA** | Database access layer |
| **PostgreSQL** | Relational database |
| **Liquibase** | Database migration |
| **Apache Kafka** | Event streaming platform |
| **JUnit 5 & Mockito** | Testing framework |
| **Gradle** | Build automation |
| **Docker Compose** | Containerization |

## Installation and Setup

Before running this application, ensure you have the following installed:

- **Java 21** or higher
- **Docker Desktop** (Needed to create PostgreSQL and Kafka instances)
- **A database management tool such as DBeaver (for viewing the transactions table) is recommended**

```

### 1. Clone the Repository

```zsh
git clone https://github.com/yourusername/fraud-detection.git
cd fraud-detection
```

### 2. Start Infrastructure Services

Start PostgreSQL, Kafka, and Zookeeper using Docker Compose:

```zsh
docker compose up -d
```

Verify containers are running:

```zsh
docker ps
```

You should see three containers (ensure these ports aren't already in use):
- `frauddb_postgres` (PostgreSQL on port 5433)
- `frauddb_kafka` (Kafka on port 9092)
- `frauddb_zookeeper` (Zookeeper on port 2181)

### 3. Building and running the application

```zsh
./gradlew build
./gradlew bootRun
```

### 4. Shutting down the application and Docker containers

Kill the bootRun process with `CTRL + C` and then:

```zsh
docker compose down
```

Or if you don't want to save the database:

```zsh
docker compose down -v
```

## Making POST Calls

### Process Transaction

There is a shell script provided called `populate_transactions.sh`, which can be run using:
```zsh
./populate_transactions.sh
```

This script will make ~110+ POST calls to the endpoint.

### Example cURL Commands

See the Fraud Detection Rules section for more information on what makes a transaction fraudulent

#### Normal Transaction
```zsh
curl -X POST http://localhost:8080/fraud-detection/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC001",
    "amount": 500.00,
    "timestamp": "2025-08-24T18:45:00",
    "location": "Toronto"
  }'
```

#### Fraudulent Transaction
```zsh
curl -X POST http://localhost:8080/fraud-detection/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC002",
    "amount": 75000.00,
    "timestamp": "2025-08-25T03:20:00",
    "location": "Unknown"
  }'
```

## Fraud Detection Rules

The system uses a weighted scoring system where each rule contributes to a total fraud score (0.0 - 1.0). A transaction is flagged as fraudulent if the score is greater than or equal to **0.7**.

### Rule 1: High Amount Detection

| Amount Range | Score | Risk Level |
|--------------|-------|------------|
| < $10,000 | 0.0 | Normal |
| $10,000 - $49,999 | 0.3 | Medium-High |
| ≥ $50,000 | 0.5 | High |

### Rule 2: Transaction Frequency

| Frequency | Score | Risk Level |
|-----------|-------|------------|
| ≤ 10 transactions/hour | 0.0 | Normal |
| > 10 transactions/hour | 0.4 | High |
| > 50 transactions/day | 0.3 | High |

### Rule 3: Location Anomaly

| Distinct Locations | Score | Risk Level |
|-------------------|-------|------------|
| ≤ 3 locations/day | 0.0 | Normal |
| > 3 locations/day | 0.3 | Medium |

### Rule 4: Off-Hours Activity

| Time Range | Score | Risk Level |
|------------|-------|------------|
| 7 AM - 1 AM | 0.0 | Normal |
| 2 AM - 6 AM | 0.2 | Low-Medium |

### Example Fraud Score Calculations

**Scenario 1: Normal Transaction**
- Amount: $500 → 0.0
- 5 transactions in the last hour → 0.0
- 1 location → 0.0
- Time: 2 PM → 0.0
- **Total Score: 0.0** 

**Scenario 2: Suspicious but Not Fraudulent**
- Amount: $15,000 → 0.3
- 8 transactions in the last hour → 0.0
- 2 locations → 0.0
- Time: 3 AM → 0.2
- **Total Score: 0.5**

**Scenario 3: Fraudulent Transaction**
- Amount: $60,000 → 0.5
- 25 transactions in the last hour → 0.4
- 2 locations → 0.0
- Time: 10 AM → 0.0
- **Total Score: 0.9**

## Kafka Integration

### How It Works

1. **Transaction Processed** → Fraudulent transaction is detected
2. **If Fraudulent** → `FraudAlertEvent` created
3. **Producer Publishes** → Event sent to `fraud-alerts` topic
4. **Consumer Receives** → Alert logged and processed

### Kafka Topics

| Topic | Purpose | Key | Value |
|-------|---------|-----|-------|
| `fraud-alerts` | Fraud notifications | Account ID | JSON alert event |

### Example Kafka Message

```json
{
  "transactionId": 42,
  "accountId": "ACC12345",
  "amount": 75000.00,
  "timestamp": "2025-10-22T14:30:00",
  "location": "New York",
  "fraudScore": 0.9,
  "alertMessage": "FRAUD ALERT: Suspicious transaction detected for account ACC12345. Amount: $75000.00, Location: New York, Fraud Score: 0.90"
}
```
