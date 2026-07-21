# NimbusCart - User Service

Part of **NimbusCart**, a cloud-native microservices-based order & fulfillment platform, built incrementally as a learning project covering Spring Boot, Spring Security, microservices, and DevOps practices.

## Current Status: Day 1
- Spring Boot project scaffolded (Java 17, Maven)
- Layered architecture set up: Model -> Service -> Controller
- In-memory User CRUD-lite API (no DB yet — DB integration is a dedicated upcoming lesson)
- Endpoints:
    - `POST /api/users` — create a user
    - `GET /api/users` — list all users
    - `GET /api/users/{id}` — get user by id
    - `GET /api/hello` — health check

## Tech Stack (planned)
- Java 17, Spring Boot 4
- PostgreSQL, Spring Data JPA
- Spring Security (JWT)
- Spring Cloud (Eureka, Gateway, Config Server)
- Kafka
- Docker

## Progress Log
- **Day 1:** Project setup, layered architecture (model/service/controller), in-memory User API with create/list/get-by-id