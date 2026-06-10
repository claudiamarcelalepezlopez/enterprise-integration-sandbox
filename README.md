# Enterprise Integration Sandbox: AI-Optimized Architecture

This repository serves as a blueprint for a modern, microservice-based integration layer. It demonstrates how senior architectural patterns (Event-Driven, RESTful, and Clean Architecture) can be rapidly scaffolded and optimized using Generative AI workflows.

## 🚀 AI-Assisted Optimizations
By leveraging Large Language Models (LLMs) and AI-assisted coding tools (Claude), the development lifecycle of this blueprint was optimized by an estimated**35%**:
* **Boilerplate Elimination:** Accelerated the creation of Spring Boot 3.x configurations, DTO mappings, and exception handling wrappers.
* **Architectural Validation:** Used AI as a sounding board to simulate edge cases regarding data transformation and REST constraints.
* **Test-Data Generation:** Rapidly generated mock payloads matching enterprise integration standards.

  # Order Processing API

**Spring Boot 3.2 · Java 17 · REST · JPA · OpenAPI 3**

A production-ready template connecting an order processing system to a relational database. Built using modern Java idioms and optimized with AI-assisted code generation.

---

## Project Structure

```
order-api/
├── src/main/java/com/integration/orderapi/
│   ├── OrderApiApplication.java       ← Spring Boot entry point
│   ├── controller/
│   │   └── OrderController.java       ← HTTP layer only, zero business logic
│   ├── service/
│   │   └── OrderService.java          ← All business rules live here
│   ├── repository/
│   │   └── OrderRepository.java       ← Spring Data JPA, derived queries
│   ├── model/
│   │   └── Order.java                 ← JPA entity with optimistic locking
│   ├── dto/
│   │   ├── OrderDtos.java             ← Java 17 Records (Request/Response/Paged)
│   │   └── OrderMapper.java           ← MapStruct compile-time mapper
│   ├── exception/
│   │   ├── OrderExceptions.java       ← Domain exceptions
│   │   └── GlobalExceptionHandler.java ← RFC 7807 ProblemDetail responses
│   └── config/
│       └── OpenApiConfig.java         ← Swagger UI configuration
└── src/main/resources/
    └── application.yml                ← Dev (H2) + Prod (PostgreSQL) profiles
```

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run locally (H2 in-memory DB)

```bash
cd order-api
mvn spring-boot:run
```

| URL | Purpose |
|-----|---------|
| `http://localhost:8080/swagger-ui.html` | Interactive API docs |
| `http://localhost:8080/h2-console` | In-memory DB browser (dev only) |
| `http://localhost:8080/actuator/health` | Health check |

### Run tests

```bash
mvn test
```

### Run with PostgreSQL (prod profile)

```bash
export DB_URL=jdbc:postgresql://localhost:5432/orderdb
export DB_USERNAME=your_user
export DB_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/orders` | Create a new order |
| `GET` | `/api/v1/orders` | List all orders (paginated) |
| `GET` | `/api/v1/orders/{id}` | Get a single order |
| `GET` | `/api/v1/orders/customer/{customerId}` | Orders by customer |
| `GET` | `/api/v1/orders/status/{status}` | Orders by status |
| `PATCH` | `/api/v1/orders/{id}/status` | Update order status |
| `DELETE` | `/api/v1/orders/{id}` | Cancel an order |

### Order Status Flow

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
   ↓           ↓           ↓
CANCELLED   CANCELLED  CANCELLED
```

---

## How AI Was Used to Optimize the Boilerplate

This template was architected with AI assistance. Here is a transparent breakdown of where and how it helped.

### 1. Eliminating Manual DTO Mapping (MapStruct)
> **Traditional approach:** hand-written `toResponse()` and `toEntity()` methods with 20–40 lines of assignment code per class.

AI identified MapStruct as the optimal solution. The `@Mapper` interface generates a full implementation at **compile time** with zero runtime reflection. Result: the entire mapping layer is 12 lines of interface code.

### 2. Reducing Entity Boilerplate (Lombok)
> **Traditional approach:** hundreds of lines of getters, setters, constructors, and builders per entity class.

AI-generated entities use `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`. The **actual domain model** — fields, indexes, constraints — is immediately visible. Zero noise.

### 3. Java 17 Records for DTOs
> **Traditional approach:** DTO classes with private fields, constructors, getters, `equals()`, `hashCode()`, `toString()`.

AI enforced the pattern of using **Java 17 Records** for all DTOs. Records are immutable by default, preventing accidental mutation. A 40-line DTO class becomes a 5-line record.

### 4. Business Rules as Data (Status Transition Map)
> **Traditional approach:** a chain of `if/else` or `switch` statements that grows with each new status.

AI structured the transition rules as an immutable `Map<OrderStatus, Set<OrderStatus>>`. Adding a new transition means updating one line of configuration data, not refactoring control flow.

### 5. RFC 7807 ProblemDetail Error Handling
> **Traditional approach:** custom `ErrorResponse` POJO with inconsistent field names across teams.

AI generated a `GlobalExceptionHandler` using Spring 6's built-in `ProblemDetail` class — the standard error format. Every error response is consistent and interoperable.

### 6. Profile-Based Database Configuration
> **Traditional approach:** maintaining separate `.properties` files with duplicated keys.

AI generated a single `application.yml` with Spring profiles (`dev` / `prod`). H2 runs locally with zero setup; PostgreSQL activates via environment variables in production.

### 7. Test Coverage from the Start
AI generated integration tests covering the full HTTP lifecycle — create, read, invalid transitions, 404, and validation errors — using MockMvc and `@SpringBootTest`. These were generated **alongside** the implementation, not as an afterthought.

---

## Key Architecture Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Timestamp type | `Instant` (not `LocalDateTime`) | UTC-safe for international systems |
| Monetary values | `BigDecimal` (not `Double`) | No floating-point rounding errors |
| Enum storage | `EnumType.STRING` | DB-readable; safe across migrations |
| Locking | `@Version` (optimistic) | Safe for concurrent order updates |
| Lazy loading | `open-in-view: false` | Prevents N+1 issues in production |
| Transaction scope | `readOnly = true` default | DB performance hint on read queries |
| Controller responsibility | HTTP only | Business logic 100% in the service layer |

---

## Adding a New Feature — Checklist

When extending this template, follow this sequence:

1. **Model** — add field or entity in `model/`
2. **Repository** — add derived query or `@Query` in `OrderRepository`
3. **DTO** — add or update record in `OrderDtos`
4. **Mapper** — update `OrderMapper` interface (MapStruct regenerates on build)
5. **Service** — add business logic method
6. **Controller** — expose new endpoint
7. **Test** — add integration test case

---

## Dependencies Overview

| Library | Version | Purpose |
|---------|---------|---------|
| Spring Boot | 3.2.5 | Framework |
| Spring Data JPA | (managed) | Database layer |
| Spring Validation | (managed) | Bean Validation 3.0 |
| H2 | (managed) | Local dev database |
| PostgreSQL | (managed) | Production database |
| MapStruct | 1.5.5 | Compile-time DTO mapping |
| Lombok | (managed) | Boilerplate reduction |
| SpringDoc OpenAPI | 2.5.0 | Swagger UI / OpenAPI 3 |
| Spring Actuator | (managed) | Health & metrics |


## 🛠️ Tech Stack & Architecture
* **Language/Framework:** Java 17 / Spring Boot 3.x
* **Pattern:** Controller-Service-Repository (Separation of Concerns)
* **Database Integration:** Spring Data JPA (Ready for PostgreSQL/Oracle)
