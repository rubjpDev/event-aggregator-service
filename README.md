# Event Aggregator Service

A high-performance microservice built with Java 25 and Spring Boot 3.3 that aggregates event data from external providers with robust caching, fault tolerance, and scalability features.

## Project Overview

This service demonstrates patterns for integrating with external APIs while maintaining high availability and performance. It implements a resilient data synchronization strategy that ensures zero downtime even when external providers are unavailable.

Key Features:
- Sub-second response times with Redis caching
- Automatic background synchronization with retry logic
- Fault-tolerant design with database fallback
- RESTful API for event searching with time-range filtering
- Fully containerized with Docker Compose
- Production-ready architecture

---

## Architecture & Design

### System Design Overview

The service implements a **dual-layer persistence strategy** combining Redis for speed and PostgreSQL for reliability:

#### Scenario 1: Provider Available

When the external provider is operational:
1. **Background Sync Task** fetches data from the provider with exponential backoff retry logic
2. Data is persisted to **PostgreSQL** for long-term storage
3. Data is cached in **Redis** for ultra-fast retrieval
4. API requests to `/api/v1/search` are served directly from cache

The synchronization runs continuously at configured intervals, keeping data fresh and up-to-date.

---

#### Scenario 2: Provider Unavailable

When the external provider experiences downtime:
1. The sync task detects the failure (e.g., HTTP 503, timeout)
2. The system automatically **rebuilds the cache from PostgreSQL**
3. Users continue receiving responses without interruption
4. **Zero downtime** is maintained throughout the outage

This design ensures resilience and continuous service availability.

---

## Quick Start

### Prerequisites
- **Docker** & **Docker Compose**
- **Make**
- **Java 25+**
- **Maven 3.9+**

### Running the Application

From the project root directory:

```bash
# Build the Java application
make build

# Start all services (app, PostgreSQL, Redis)
make run
```

The API will be available at `http://localhost:8080`

### API Endpoint

**Search Events:**
```bash
GET /api/v1/search?starts_at=2024-01-01T00:00:00&ends_at=2024-12-31T23:59:59
```

**Parameters:**
- `starts_at` (required): ISO 8601 datetime for range start
- `ends_at` (required): ISO 8601 datetime for range end

**Response:** JSON array of events within the specified time range

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 25 |
| **Framework** | Spring Boot 3.3 |
| **Database** | PostgreSQL |
| **Cache** | Redis |
| **Build Tool** | Maven |
| **Containerization** | Docker & Docker Compose |
| **API Design** | RESTful with OpenAPI spec |

---

## Design Decisions & Trade-offs

### Caching Strategy
- **Redis** provides millisecond-level response times for read-heavy workloads
- **TTL-based invalidation** ensures data freshness while minimizing provider API calls
- **Cache-aside pattern** with database fallback guarantees availability

### Fault Tolerance
- **Retry logic with exponential backoff** handles transient provider failures
- **Database-backed cache rebuilding** ensures service continuity during extended outages
- **Graceful degradation** maintains functionality even with stale data

### Scalability Considerations
- **Stateless application design** enables horizontal scaling behind a load balancer
- **Connection pooling** (HikariCP) optimizes database resource usage
- **Async I/O** for external API calls prevents thread blocking

---

## Performance & Scalability

### Handling High Traffic (5k-10k RPS)

Strategies implemented:
- **Redis caching** reduces database load by 95%+
- **Stateless architecture** allows deploying multiple instances
- **Load balancing** distributes traffic across application replicas
- **Database connection pooling** prevents connection exhaustion

### Large Dataset Optimization

For thousands of events with complex zone data:
- **Batch inserts** reduce database round trips
- **Pagination support** limits memory footprint
- **Indexed queries** on timestamp fields for fast range searches
- **Lazy loading** of related entities

### Future Enhancements

- **Event-driven architecture** with webhooks (if provider supports notifications)
- **AWS Lambda integration** for serverless scaling
- **GraphQL API** for flexible client queries
- **Distributed caching** with Redis Cluster for multi-region deployments

---

## Project Structure

```
event-aggregator-service/
├── src/main/java/com/rubdev/eventsync/
│   ├── controller/       # REST API endpoints
│   ├── service/          # Business logic & orchestration
│   ├── repository/       # Data access layer
│   ├── model/            # Domain entities
│   ├── config/           # Spring configuration
│   └── scheduler/        # Background sync tasks
├── docker-compose.yml    # Multi-container orchestration
├── Dockerfile            # Application container image
├── Makefile              # Build & run automation
└── pom.xml               # Maven dependencies
```

---

## Testing

Run the test suite:
```bash
mvn test
```

The project includes:
- **Unit tests** for service layer logic
- **Integration tests** for repository operations
- **API tests** for endpoint validation

---

## License

This project is part of my personal portfolio and is available for review and demonstration purposes.

---

## Author

Rubén Juárez Pérez

Java Engineer specializing in microservices architecture, distributed systems, and high-performance backend development.

---

## Additional Resources

- [API Documentation](https://app.swaggerhub.com/apis-docs/luis-pintado-feverup/backend-test/1.0.0)
- [System Design Diagrams](./docs/)


