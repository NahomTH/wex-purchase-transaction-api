# Wex Purchase Transaction Service API 

## Synopsis

Wex Purchase Transaction Service API Stores USD purchase transactions and retrieves them converted to a Treasury supported 
foreign currency uses the most recent exchange within the <= 6 months.

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
- JDK 21

### Run with Docker (recommended)

Create your local env file.

```bash
cp .env.example .env
docker compose up --build
```

### Without Docker

You need a PostgreSQL instance. So you just need to create one probably using docker.

```bash
docker run --name wex-purchases-db -p 5432:5432 \
  -e POSTGRES_DB=wexPurchases -e POSTGRES_USER=wexPurchasesUser -e POSTGRES_PASSWORD=CHANGE_THIS \
  -d postgres:16-alpine
```

The datasource username and password have no defaults, so pass them when starting the app. The URL defaults to `jdbc:postgresql://localhost:5432/wexPurchases`:

```bash
SPRING_DATASOURCE_USERNAME=wexPurchasesUser \
SPRING_DATASOURCE_PASSWORD=CHANGE_THIS \
./gradlew bootRun
```

* * *

## How to test

```bash
./gradlew test
```

* * *

## How to get the Currency format

The `currency` parameter is the Treasury **`country_currency_desc`** value (for example, `Canada-Dollar`), not an ISO code. The full list of accepted values is available from:

```
GET /api/v1/all/currencies
```