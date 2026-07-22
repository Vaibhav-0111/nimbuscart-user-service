# NimbusCart - User Service

Part of **NimbusCart**, a cloud-native microservices-based order & fulfillment platform, built incrementally as a learning project covering Spring Boot, Spring Security, microservices, and DevOps practices.

## Current Status: Day 2
- Layered architecture: Model -> Repository -> Service -> Controller
- Real JPA persistence via Spring Data JPA (H2 in-memory DB for now; PostgreSQL swap planned)
- `User` is a proper `@Entity` with auto-generated ID and unique email constraint
- `UserRepository` extends `JpaRepository` — no manual implementation needed
- H2 console enabled at `/h2-console` for inspecting live data
- Endpoints:
  - `POST /api/users` — create a user
  - `GET /api/users` — list all users
  - `GET /api/users/{id}` — get user by id
  - `GET /api/hello` — health check

## Tech Stack (planned)
- Java 17, Spring Boot 4
- H2 (current) -> PostgreSQL (upcoming), Spring Data JPA
- Spring Security (JWT)
- Spring Cloud (Eureka, Gateway, Config Server)
- Kafka
- Docker

## Progress Log
- **Day 1:** Project setup, layered