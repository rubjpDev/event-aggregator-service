# Event Aggregator Service

A backend microservice that aggregates event data from an external XML provider, caches it in Redis, and exposes a RESTful search API. Built as a portfolio project to demonstrate production-ready patterns in Java 25 and Spring Boot.

---

## Why This Project

I designed this service to solve a common backend challenge: consuming unreliable external APIs while guaranteeing fast, available reads for end users. The focus is on resilient data synchronization, a manual cache-aside pattern, and a clean layered architecture — without reaching for heavyweight frameworks until they are warranted.

---

## Architecture & Design

The service implements a **dual-layer persistence strategy** combining Redis for speed and PostgreSQL for reliability.

### Scenario 1: Provider Available

When the external provider is operational:

1. A **background sync task** fetches data from the provider with retry logic
2. Data is persisted to **PostgreSQL** for long-term storage
3. Data is written to **Redis** with a configurable TTL
4. API requests to `/api/v1/search` are served directly from cache

![Provider Available](./docs/CASE1.png)

The synchronization runs on a fixed schedule, keeping data continuously fresh.

---

### Scenario 2: Provider Unavailable

When the external provider experiences downtime:

1. The sync task detects the failure (HTTP 5xx, timeout) after 5 retries
2. The sync aborts cleanly — the read path is unaffected
3. The service continues serving responses from Redis or rebuilds cache from PostgreSQL
4. **Zero downtime** is maintained throughout the outage

![Provider Unavailable](./docs/CASE2.png)

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL 16 |
| Cache | Redis 7.4 |
| Build | Maven |
| Containerization | Docker & Docker Compose |
| Observability | Micrometer Tracing + Logstash |

---

## Getting Started

### Prerequisites

- **Docker** & **Docker Compose**
- **Make**
- **Java 25+** (for local development)
- **Maven 3.9+** (or use the included `mvnw` wrapper)

### Run with Docker (recommended)

```bash
make build
make run
```

The API will be available at `http://localhost:8080`.

### Local development

```bash
# Start only infrastructure
make infra

# Run the application with the local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Provider URL configuration

The provider URL is externalized and can be overridden at runtime:

```bash
# Docker
PROVIDER_URL=https://your-provider.com/api/events make run

# Local
PROVIDER_URL=https://your-provider.com/api/events ./mvnw spring-boot:run
```

---

## API Usage

**Search Events:**

```
GET /api/v1/search?starts_at=2024-01-01T00:00:00&ends_at=2024-12-31T23:59:59
```

| Parameter | Required | Description |
|---|---|---|
| `starts_at` | Yes | ISO 8601 datetime — range start |
| `ends_at` | Yes | ISO 8601 datetime — range end |

**Response:** JSON array of events within the specified time range.

---

## Project Structure

```
event-aggregator-service/
├── src/main/java/com/rubdev/eventsync/
│   ├── cache/            # Cache management (RedisTemplate-based cache-aside)
│   ├── config/           # Spring configuration & @ConfigurationProperties
│   ├── controller/       # REST API endpoints
│   ├── model/            # Domain entities & provider XML DTOs
│   ├── repository/       # Data access layer (Spring Data JPA)
│   ├── service/          # Business logic & orchestration
│   ├── task/             # Scheduled background sync
│   └── utils/            # Application constants
├── docs/
│   ├── adr/              # Architecture Decision Records
│   ├── CASE1.png         # Architecture diagram — provider available
│   └── CASE2.png         # Architecture diagram — provider unavailable
├── docker-compose.yml
├── Dockerfile
├── Makefile
└── pom.xml
```

---

## Testing

```bash
mvn test
```

The test suite covers:

- **Unit tests** — service layer logic and cache manager behaviour
- **Integration tests** — repository operations
- **API tests** — endpoint validation and error handling

---

## Future Improvements

- Bundled mock provider for fully self-contained demo runs
- Circuit breaker if retry complexity grows beyond current scope (Resilience4j)
- Exponential backoff with jitter to reduce thundering-herd against recovering providers
- Indexed pagination on the search endpoint for large datasets
- Redis Cluster configuration for multi-region deployments

---

## Author

**Rubén Juárez Pérez**

Software Engineer — 2026.

---

## License

MIT — see [LICENSE](./LICENSE)
