# NimbusCart — User Service

A cloud-native, microservices-based order & fulfillment platform, built incrementally as a hands-on learning project covering Spring Boot, Spring Data JPA, Spring Security, microservices architecture, and DevOps practices one real feature at a time, with every part pushed and documented as it's built.

This repository is the **User Service**  the first of several planned microservices in the larger NimbusCart platform.

## Philosophy

This isn't a tutorial clone. Every layer is built, understood, and pushed incrementally  no part of the codebase exists that hasn't been explained and deliberately reasoned through first. Each day adds one real capability, documented with the reasoning behind it, not just the code.

## Tech Stack

**Current**
- Java 17
- Spring Boot 4
- Spring Data JPA (Hibernate)
- H2 (in-memory database, for development)
- Bean Validation (Jakarta Validation)
- Spring Security — password hashing with BCrypt (JWT login in progress)
- Maven

**Planned**
- JWT-based login and route-level authorization (finishing Spring Security)
- PostgreSQL (replacing H2)
- Spring Cloud (Eureka service discovery, API Gateway, Config Server)
- Kafka (event-driven inter-service communication)
- Resilience4j (circuit breakers)
- Docker + Docker Compose
- CI/CD via GitHub Actions

## Architecture

Layered architecture, strictly separated by responsibility — each layer only knows about the one below it, never skipping levels:

```
Controller  →  handles HTTP only (requests/responses, status codes)
Service     →  business logic, orchestration, DTO ↔ Entity conversion
Repository  →  data access (Spring Data JPA)
Model       →  database entity definitions
DTO         →  API request/response contracts (never expose entities directly)
Config      →  framework-level bean configuration (e.g. password encoding)
Exception   →  centralized error handling (@RestControllerAdvice)
```

### Why this structure

- **Controllers stay thin.** They never contain business logic — only HTTP concerns: mapping routes, reading input, setting status codes.
- **Services own the rules.** All business logic lives here, independent of HTTP or storage, so it can be tested and reused without a running web server.
- **Repositories are pure data access.** Spring Data JPA generates the implementation from an interface — no hand-written SQL for standard operations.
- **DTOs protect the API contract.** The database entity (`User`) and what a client can send/receive (`UserRequestDto` / `UserResponseDto`) are deliberately different classes, so the storage model can evolve without ever accidentally leaking internal fields through the API — this is precisely why the newly added `password` field never appears in any API response.
- **Config holds framework wiring.** Beans like the `PasswordEncoder` live here, separate from business logic, so security configuration has one clear home.
- **Errors are centralized.** One `@RestControllerAdvice` class formats every validation failure consistently, instead of scattering try/catch blocks across controllers.

### Package layout

```
com.nimbuscart.user_service
├── controller
│   ├── HelloController.java
│   └── UserController.java
├── service
│   └── UserService.java
├── repository
│   └── UserRepository.java
├── model
│   └── User.java
├── dto
│   ├── UserRequestDto.java
│   └── UserResponseDto.java
├── config
│   └── SecurityConfig.java
├── exception
│   └── GlobalExceptionHandler.java
└── UserServiceApplication.java
```

## API Endpoints

| Method | Endpoint            | Description               | Request Body       | Success Response |
|--------|----------------------|----------------------------|---------------------|-------------------|
| POST   | `/api/users`         | Create a new user          | `UserRequestDto`    | `200 OK` + user   |
| GET    | `/api/users`         | List all users             | —                   | `200 OK` + list   |
| GET    | `/api/users/{id}`    | Get a user by ID           | —                   | `200 OK` + user / `404` |
| PUT    | `/api/users/{id}`    | Update an existing user (idempotent) | `UserRequestDto` | `200 OK` + user / `404` |
| DELETE | `/api/users/{id}`    | Delete a user               | —                   | `204 No Content` / `404` |
| GET    | `/api/hello`          | Health check                 | —                   | `200 OK`          |

> Every endpoint is currently open — JWT-based authentication to require a valid token on these routes is in progress (Day 5, part 2).

### Validation

Every request body is validated before it reaches business logic:
- `name` — required, cannot be blank
- `email` — required, must be a valid email format, must be unique at the database level
- `password` — required, hashed with BCrypt before being stored; never returned in any API response

Invalid input returns a structured `400 Bad Request`, e.g.:
```json
{
  "name": "Name is required",
  "email": "Email must be valid"
}
```
instead of a raw stack trace — handled centrally by `GlobalExceptionHandler`.

## Running Locally

**Prerequisites:** Java 17+, Maven 3.9+

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

**H2 Console** (inspect live data while the app runs):
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:nimbuscart`
- Username: `sa`
- Password: *(leave blank)*

### Example requests

```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Vaibhav\",\"email\":\"vaibhav@test.com\",\"password\":\"secret123\"}"

# List all users
curl http://localhost:8080/api/users

# Get a user by id
curl http://localhost:8080/api/users/1

# Update a user
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Vaibhav Tripathi\",\"email\":\"vaibhav2@test.com\",\"password\":\"newsecret123\"}"

# Delete a user
curl -X DELETE http://localhost:8080/api/users/1
```

## Progress Log

- **Day 1 — Foundations.** Project scaffolded with Spring Boot, Maven, and Java 17. Established the layered architecture (Model → Service → Controller) and built an in-memory `User` API (create/list/get-by-id) using a plain `List`. Goal: learn Spring's core structure, bean lifecycle, and dependency injection before introducing any persistence complexity.

- **Day 2 — Real Persistence.** Replaced the in-memory list with Spring Data JPA. `User` became a proper `@Entity` with an auto-generated primary key (`@Id` + `@GeneratedValue`) and a unique constraint on email. Introduced `UserRepository`, a zero-implementation interface that gains full CRUD (`save`, `findAll`, `findById`, `deleteById`) purely by extending `JpaRepository`. Backed by an in-memory H2 database so persistence concepts could be learned without needing a separately installed database server.

- **Day 3 — Validation & Error Handling.** Added Bean Validation (`@NotBlank`, `@Email`) directly on the entity, triggered via `@Valid` on the controller's `@RequestBody`. Introduced `GlobalExceptionHandler` using `@RestControllerAdvice` to catch validation failures across every controller in one place, returning structured, field-level `400` responses instead of default Spring stack traces.

- **Day 4 — DTO Pattern & Full CRUD.** Introduced `UserRequestDto` and `UserResponseDto` to fully decouple the public API contract from the database entity — the entity is never exposed directly, protecting against future internal fields leaking through the API. Validation moved from the entity onto `UserRequestDto`, since input rules are an API concern, not a storage concern. Completed full CRUD by adding `PUT` (idempotent update — calling it repeatedly with the same data has the same effect as calling it once) and `DELETE` (returns `204 No Content`, since a successful deletion has nothing meaningful left to return).

- **Day 5 (Part 1) — Password Security Foundations.** Added `password` and `role` fields to the `User` entity and `UserRequestDto`. Introduced `SecurityConfig` with a `BCryptPasswordEncoder` bean, wired into `UserService` via constructor injection, so every password is hashed (salted, one-way) before it ever reaches the database — plain-text passwords are never stored, and the hash itself is never exposed through `UserResponseDto`. JWT-based login and route-level security are Part 2, still pending.

## Roadmap

- **Day 5 (Part 2)** — Generate real JWT tokens on login, verify them via a security filter on every request, and require authentication on `/api/users` routes; add role-based authorization (e.g. only `ADMIN` can delete)
- Migrate from H2 to PostgreSQL, with Flyway for versioned schema migrations
- Split into true microservices: Auth Service, Product Service, Order Service
- Spring Cloud Gateway as a single entry point + Eureka for service discovery
- Kafka for event-driven communication between services (e.g. order placed → inventory updated → notification sent)
- Resilience4j circuit breakers for fault tolerance between services
- Dockerize every service, orchestrate locally with Docker Compose
- CI/CD pipeline via GitHub Actions, deployed to a cloud host

## Author

Vaibhav Tripathi
- GitHub: [github.com/Vaibhav-0111](https://github.com/Vaibhav-0111)
- LinkedIn: [linkedin.com/in/vaibhavtripathi75](https://linkedin.com/in/vaibhavtripathi75)
