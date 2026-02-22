# Kafka SAGA Booking System

A distributed booking system implementing the **SAGA pattern** using **Spring Boot**, **Apache Kafka**, and **Kafka Streams** for coordinating transactions across microservices.

## Architecture

The system consists of three microservices that communicate exclusively through Kafka topics:

- **booking-orchestrator** — Creates bookings, orchestrates the SAGA flow using Kafka Streams join, and manages booking lifecycle including expiry
- **payment-service** — Handles customer balance reservation and rollback
- **seat-inventory-service** — Manages seat availability reservation and rollback

### SAGA Flow

```
1. Client creates a booking → published to "bookings-new" topic
2. payment-service and seat-inventory-service process in parallel
3. Each service responds with ACCEPTED/REJECTED to their response topic
4. Kafka Streams joins both responses within a 10-second window
5. If both ACCEPTED → CONFIRMED
   If one REJECTED → ROLLBACK (compensating transaction)
   If both REJECTED → REJECTED
6. Final result published to "bookings-results"
7. Both services receive the result and commit or rollback their local state
```

### Booking Expiry

If both services don't respond within the configured timeout (default 5 minutes), the `BookingExpiryJob` marks the booking as `EXPIRED` and publishes a `ROLLBACK` event.

## Tech Stack

- **Java 21** + **Spring Boot 3.4**
- **Apache Kafka** (KRaft mode, no ZooKeeper)
- **Kafka Streams** for event join/correlation
- **Spring Data JPA** + **H2** (dev) / **PostgreSQL** (docker)
- **Micrometer** + **Prometheus** for metrics
- **JUnit 5** + **EmbeddedKafka** for testing

## Running Locally

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker (for Kafka via Testcontainers)

### Quick Start

```bash
# Build the project
mvn clean install -DskipTests

# Start booking-orchestrator (with Testcontainers Kafka)
cd booking-orchestrator && mvn spring-boot:test-run

# In separate terminals:
cd payment-service && mvn spring-boot:test-run
cd seat-inventory-service && mvn spring-boot:test-run
```

### Docker Compose (Full Stack)

```bash
mvn clean package -DskipTests
docker-compose up -d
```

## API

### Create Booking
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "seatId": 1, "seatCount": 2, "amount": 200}'
```

### Check Booking Status
```bash
curl http://localhost:8080/bookings/{id}
```

### List All Bookings
```bash
curl http://localhost:8080/bookings
```

## Kafka Topics

| Topic | Purpose |
|-------|---------|
| `bookings-new` | New booking requests |
| `payment-responses` | Payment service accept/reject |
| `seat-reservation-responses` | Seat service accept/reject |
| `bookings-results` | Final SAGA outcome |

## Running Tests

```bash
mvn test
```
