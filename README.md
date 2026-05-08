# ecommerce-spring-cloud-order-service

Order orchestration service for the Spring Cloud microservices demo.

## What This Service Does

The order service handles the full lifecycle of an order: creation, retrieval, update, and deletion. On each write operation it coordinates with two external services — `product-service` to fetch product data and pricing, and `inventory-service` to check and adjust stock levels. Communication happens via **OpenFeign** (declarative HTTP client), with service discovery delegated to Eureka so no URLs are hardcoded. Each Feign call is protected by a **Resilience4j** circuit breaker and retry policy, wrapped in a dedicated `ClientWrapper` bean to work around the single-annotation-per-method constraint of Spring AOP.

## API

| Method | Path | Status codes |
|---|---|---|
| GET | `/api/orders` | 200 |
| GET | `/api/orders/{id}` | 200 / 404 |
| POST | `/api/orders` | 201 / 400 / 503 |
| PATCH | `/api/orders/{id}` | 200 / 400 / 404 / 503 |
| DELETE | `/api/orders/{id}` | 204 / 404 / 503 |

Request body for POST and PATCH:

```json
{
  "productId": 1,
  "quantity": 2
}
```

`totalPrice` is calculated server-side as `product.price × quantity`.

## Key Configuration

Properties injected by the config server at bootstrap.

| Property | Value | Why |
|---|---|---|
| `server.port` | `8083` | Fixed port for this service |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5434/order_db` | Dedicated PostgreSQL instance |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Schema managed by Flyway |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka` | Registers with the discovery server |
| `resilience4j.circuitbreaker.instances.*` | per service | CB thresholds for product and inventory clients |
| `resilience4j.retry.instances.*` | per service | Retry attempts and wait duration |

## How to Run

Prerequisites: config-server (:8888), discovery-server (:8761), product-service (:8081), inventory-service (:8082), and PostgreSQL must already be running.

```bash
docker compose up -d
./mvnw spring-boot:run
```

## Stack

| Component | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Cloud | 2025.1.1 |
| OpenFeign | via Spring Cloud BOM |
| Resilience4j | via Spring Cloud BOM |
| MapStruct | 1.6.3 |
| PostgreSQL | 14.22 (Docker) |
| Flyway | via Spring Boot BOM |

## Startup Order

```
1. config-server        :8888
2. discovery-server     :8761
3. product-service      :8081
4. inventory-service    :8082
5. order-service        :8083   <- this service
6. api-gateway          :8080
```

---

# Project Overview — Spring Cloud Microservices Demo

## Goal

A working distributed system that covers the core Spring Cloud primitives: centralized configuration, service discovery, inter-service communication, API gateway routing, and fault tolerance. Each concept is implemented in isolation so it can be studied independently.

## Architecture

```
[Client HTTP]
      |
[API Gateway :8080]
      |
      +---> [Product Service   :8081]
      +---> [Inventory Service :8082]
      +---> [Order Service     :8083]
                  |
                  +---> [Product Service]   (via OpenFeign)
                  +---> [Inventory Service] (via OpenFeign)

All services register on  --> [Eureka Discovery Server :8761]
All services read config from --> [Config Server :8888]
Config Server reads from      --> [config-repo on GitHub]
```

## Repository Structure (Polyrepo)

| # | Repository | Purpose |
|---|---|---|
| 1 | `spring-cloud-config-repo` | YAML configuration files, read by Config Server via Git |
| 2 | `spring-cloud-config-server` | Reads config-repo and exposes properties to all services |
| 3 | `spring-cloud-discovery-server` | Eureka — service registry |
| 4 | `spring-cloud-api-gateway` | Single entry point, routes requests to microservices |
| 5 | `spring-cloud-product-service` | Product CRUD |
| 6 | `spring-cloud-inventory-service` | Inventory CRUD |
| 7 | `spring-cloud-order-service` | Order orchestration, calls product and inventory |

## Spring Cloud Concepts Covered

| Concept | Component | Repository |
|---|---|---|
| Centralized configuration | Spring Cloud Config | config-server + config-repo |
| Service discovery | Eureka | discovery-server |
| Client-side load balancing | Spring Cloud LoadBalancer | built into Feign and Gateway |
| Inter-service communication | OpenFeign | order-service |
| API Gateway / routing | Spring Cloud Gateway | api-gateway |
| Circuit Breaker / fault tolerance | Resilience4j | order-service |

## Common Stack

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2025.1.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<properties>
    <java.version>21</java.version>
</properties>
```