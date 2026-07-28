# NimbusCart — User Service

A cloud-native, microservices-based order & fulfillment platform, built incrementally as a hands-on learning project covering Spring Boot, Spring Data JPA, Spring Security, microservices architecture, and DevOps practices — one real feature at a time, with every part pushed and documented as it's built.

This repository is the **User Service** — the first of several planned microservices in the larger NimbusCart platform.

## Philosophy

This isn't a tutorial clone. Every layer is built, understood, and pushed incrementally — no part of the codebase exists that hasn't been explained and deliberately reasoned through first. Each day adds one real capability, documented with the reasoning behind it, not just the code.

## Tech Stack

**Current**
- Java 17
- Spring Boot 4
- Spring Data JPA (Hibernate)
- H2 (in-memory database, for development)
- Bean Validation (Jakarta Validation)
- Spring Security — full JWT authentication (login, token verification via a custom filter) and role-based authorization (`@PreAuthorize`)
- Maven

**Planned**
- PostgreSQL (replacing H2), with Flyway for versioned schema migrations
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
Config      →  framework-level bean configuration (security filter chain, password encoding)
Security    →  JWT generation, verification, and request-level enforcement
Exception   →  centralized error handling (@RestControllerAdvice)
```

### Why this structure

- **Controllers stay thin.** They never contain business logic — only HTTP concerns: mapping routes, reading input, setting status codes.
- **Services own the rules.** All business logic lives here, independent of HTTP or storage, so it can be tested and reused without a running web server.
- **Repositories are pure data access.** Spring Data JPA generates the implementation from an interface — no hand-written SQL for standard operations, including custom lookups like `findByEmail` (derived query methods).
- **DTOs protect the API contract.** The database entity (`User`) and what a client can send/receive (`UserRequestDto` / `UserResponseDto` / `LoginRequestDto` / `LoginResponseDto`) are deliberately different classes, so the storage model can evolve without ever accidentally leaking internal fields — like `password` — through the API.
- **Config holds framework wiring.** The `SecurityFilterChain` bean (which routes require authentication, CSRF/session policy) and the `PasswordEncoder` bean both live here, separate from business logic.
- **Security holds identity logic.** `JwtUtil` (generating and verifying signed tokens) and `JwtAuthFilter` (intercepting every request to check for one) are grouped together, separate from generic config, since they represent active behavior, not just bean wiring.
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
│   ├── UserResponseDto.java
│   ├── LoginRequestDto.java
│   └── LoginResponseDto.java
├── config
│   └── SecurityConfig.java
├── security
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
├── exception
│   └── GlobalExceptionHandler.java
└── UserServiceApplication.java
```

## API Endpoints

| Method | Endpoint            | Description               | Request Body       | Auth Required        | Success Response |
|--------|----------------------|----------------------------|---------------------|------------------------|-------------------|
| POST   | `/api/users`         | Create a new user (register) | `UserRequestDto`  | No                     | `200 OK` + user   |
| POST   | `/api/users/login`   | Log in, returns a JWT token | `LoginRequestDto`   | No                     | `200 OK` + token / `401` |
| GET    | `/api/users`         | List all users             | —                   | No                     | `200 OK` + list   |
| GET    | `/api/users/{id}`    | Get a user by ID           | —                   | Yes (any authenticated user) | `200 OK` + user / `404` |
| PUT    | `/api/users/{id}`    | Update an existing user (idempotent) | `UserRequestDto` | Yes (any authenticated user) | `200 OK` + user / `404` |
| DELETE | `/api/users/{id}`    | Delete a user               | —                   | Yes — **ADMIN role only** | `204 No Content` / `403` / `404` |
| GET    | `/api/hello`          | Health check                 | —                   | No                     | `200 OK`          |

Authenticated requests must include the JWT from login in the request header:
```
Authorization: Bearer <token>
```

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
# Register a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Vaibhav\",\"email\":\"vaibhav@test.com\",\"password\":\"secret123\"}"

# Log in and get a JWT token
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"vaibhav@test.com\",\"password\":\"secret123\"}"

# Get a user by id (requires a token from login)
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Update a user (requires a token)
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "{\"name\":\"Vaibhav Tripathi\",\"email\":\"vaibhav2@test.com\",\"password\":\"newsecret123\"}"

# Delete a user (requires a token AND the ADMIN role)
curl -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## Progress Log
- **Day 1 — Foundations.** Project scaffolded with Spring Boot, Maven, and Java 17. Established the layered architecture (Model → Service → Controller) and built an in-memory `User` API (create/list/get-by-id) using a plain `List`. Goal: learn Spring's core structure, bean lifecycle, and dependency injection before introducing any persistence complexity.

- **Day 2 — Real Persistence.** Replaced the in-memory list with Spring Data JPA. `User` became a proper `@Entity` with an auto-generated primary key (`@Id` + `@GeneratedValue`) and a unique constraint on email. Introduced `UserRepository`, a zero-implementation interface that gains full CRUD (`save`, `findAll`, `findById`, `deleteById`) purely by extending `JpaRepository`. Backed by an in-memory H2 database so persistence concepts could be learned without needing a separately installed database server.

- **Day 3 — Validation & Error Handling.** Added Bean Validation (`@NotBlank`, `@Email`) directly on the entity, triggered via `@Valid` on the controller's `@RequestBody`. Introduced `GlobalExceptionHandler` using `@RestControllerAdvice` to catch validation failures across every controller in one place, returning structured, field-level `400` responses instead of default Spring stack traces.

- **Day 4 — DTO Pattern & Full CRUD.** Introduced `UserRequestDto` and `UserResponseDto` to fully decouple the public API contract from the database entity — the entity is never exposed directly, protecting against future internal fields leaking through the API. Validation moved from the entity onto `UserRequestDto`, since input rules are an API concern, not a storage concern. Completed full CRUD by adding `PUT` (idempotent update) and `DELETE` (`204 No Content`).

- **Day 5 (Part 1) — Password Security Foundations.** Added `password` and `role` fields to the `User` entity and `UserRequestDto`. Introduced `SecurityConfig` with a `BCryptPasswordEncoder` bean, wired into `UserService` via constructor injection, so every password is hashed (salted, one-way) before it ever reaches the database — plain-text passwords are never stored, and the hash itself is never exposed through `UserResponseDto`.

- **Day 5 (Part 2) — Login & Token Generation.** Added `JwtUtil` to generate and verify signed JWTs (HMAC-SHA256), and a `POST /api/users/login` endpoint that verifies credentials via `passwordEncoder.matches()` and returns a token on success (`401` otherwise). Added `findByEmail` to `UserRepository` as a derived query method. Tokens are stateless — the server verifies each one's signature fresh, with nothing stored server-side.

- **Day 6 — Enforcing Authentication & Authorization.** Added `JwtAuthFilter` (a `OncePerRequestFilter`) that reads the `Authorization: Bearer <token>` header on every request, verifies the token, and populates Spring Security's `SecurityContextHolder`. Configured a stateless `SecurityFilterChain` requiring authentication on `/api/users/**` while keeping registration, login, and health-check routes open. Embedded the user's role as a custom JWT claim, and enabled `@PreAuthorize("hasRole('ADMIN')")` on the delete endpoint via `@EnableMethodSecurity` — the API now genuinely enforces identity and role, not just issues tokens.

## Roadmap

- **Day 7** — Migrate from H2 to PostgreSQL, with Flyway for versioned schema migrations
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
