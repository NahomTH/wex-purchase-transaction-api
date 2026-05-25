# Wex Purchase Transaction Service API 

## Synopsis

Wex Purchase Transaction Service API Stores USD purchase transactions and retrieves them converted to a Treasury supported 
foreign currency uses the most recent exchange within the preceding 6 months.

* * *

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- springdoc-openapi (Swagger UI / OpenAPI)

* * *

## How to run it locally

### Prerequisites

- Docker (Engine + Compose v2) for the recommended path
- Internet access for Treasury Exchange API
- JDK 21 only needed if you run without Docker

### Run with Docker (recommended)

```bash
docker compose up --build
```

This builds the application image and starts two containers:

- **app** the service, available at http://localhost:8080

### Without Docker

The app expects a PostgreSQL instance on `localhost:5432` with database, username, and password all set to `purchases` (the defaults in `application.yml`). The quickest way to get one:

```bash
docker run --name purchases-db -p 5432:5432 \
  -e POSTGRES_DB=purchases -e POSTGRES_USER=purchases -e POSTGRES_PASSWORD=purchases \
  -d postgres:16-alpine
```

Then start the app:

```bash
./gradlew bootRun
```

If your database differs, override the connection with standard Spring environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/purchases \
SPRING_DATASOURCE_USERNAME=purchases \
SPRING_DATASOURCE_PASSWORD=purchases \
./gradlew bootRun
```

* * *

## How to test

```bash
./gradlew test
```

* * *

## Currency format

The `currency` parameter is the Treasury **`country_currency_desc`** value (for example, `Canada-Dollar`), not an ISO code. The full list of accepted values is available from:

```
GET /api/v1/all/currencies
```