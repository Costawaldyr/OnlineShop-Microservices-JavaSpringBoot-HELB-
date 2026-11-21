# Online Shop — Spring Boot Microservices

An academic Java project for HELB, built around two REST APIs. The catalogue service manages categories, products and available stock. The order service manages clients, orders and order lines, and calls the catalogue service to read product prices and adjust stock.

The applications run independently and use separate PostgreSQL databases. There is no frontend: Swagger UI provides an interactive interface for exploring and trying the endpoints.

## Services

| Application | Port | Responsibilities | Swagger UI |
| --- | --- | --- | --- |
| Catalogue Service | 8081 | Categories, products, stock | [Open catalogue Swagger](http://localhost:8081/swagger-ui.html) |
| Order Service | 8082 | Clients, orders, order lines | [Open order Swagger](http://localhost:8082/swagger-ui.html) |

Each API exposes its OpenAPI JSON at `/v3/api-docs` on its own port. Catalogue documentation declares version `1.0`; order documentation declares `1.2`. These are documentation labels, not enforced request versions.

## Documentation

Start with the [documentation index](doc/README.md).

- [Project report](doc/PROJECT_REPORT.md): scope, architecture, design choices and current progress.
- [API and Swagger guide](doc/API_GUIDE.md): every exposed business endpoint and a complete manual scenario.
- [Dependencies](doc/DEPENDENCIES.md): Maven libraries, build plugins and their roles.
- [Service communication](doc/SERVICE_COMMUNICATION.md): HTTP calls, stock changes and failure handling.
- [Flow and sequence diagrams](doc/DIAGRAMS.md): architecture, lifecycle and request sequences.
- [Database guide](doc/DATABASE.md): database setup, tables, relationships and constraints.
- [PostgreSQL with Docker](doc/DOCKER_POSTGRES.md): start PostgreSQL and create the databases manually.
- [Testing guide](doc/TESTING.md): existing tests, commands and remaining work.

## Requirements

- JDK 21; both Maven projects compile for Java 21.
- Maven, or the Maven wrapper included in each service.
- A running PostgreSQL server reachable at `localhost:5432`.
- Two separate application databases and a PostgreSQL account allowed to access them.

Check the Java version used by Maven before building:

```bash
java -version
mvn -version
```

On macOS, if Maven uses an older installed JDK:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

## Database and environment setup

Choose either [PostgreSQL with Docker](doc/DOCKER_POSTGRES.md) or the manual setup in the [database guide](doc/DATABASE.md). Create a `.env` file at the repository root with these keys, replacing the example values with your local settings:

```properties
CATALOGUE_DB_NAME=online_store_db
ORDER_DB_NAME=online_shop_order_db
DB_USERNAME=helb
DB_PASSWORD=replace_with_your_local_password
```

Do not add quotes around values: both services import this file as Java properties through `spring.config.import=optional:file:../.env[.properties]`. Keep the file private; it is ignored by Git. The updated `.env.example` supplies these keys.

Start each application from its own service directory so that `../.env` points to the repository root. Database names are examples and must match databases that actually exist.

## Start both APIs

Use two terminal tabs. Start the catalogue first.

Terminal 1, from the repository root:

```bash
cd catalogue-service
mvn clean package
java -jar target/catalogue-service-0.0.1-SNAPSHOT.jar
```

Terminal 2, from the repository root:

```bash
cd order-service
mvn clean package
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

Wait for the application startup message, then open the Swagger links above. `mvn package` also runs tests; the required test databases must be available. Use `./mvnw` instead of `mvn` if Maven is not installed.

Each directory also contains `run.sh` and `test.sh`. The current `run.sh` launches its JAR in the background with `&` and does not stop automatically after a failed Maven command. The foreground commands above make startup failures and shutdown easier to follow. Stop a foreground application with `Ctrl+C`.

## First manual scenario

1. Create a category in catalogue Swagger and copy its returned ID.
2. Create a product linked to that category, with a positive price and available stock.
3. Create a client in order Swagger and copy its returned ID.
4. Create an order using the client ID, product ID and quantity.
5. Read the order and check its saved unit price, total and `CONFIRMED` status.
6. Read the product again to check that stock decreased.
7. Cancel the order and check that stock increased again.

Use IDs returned by your own requests; do not assume an ID starts at `1`. Request bodies and expected HTTP codes are in the [API guide](doc/API_GUIDE.md).

## Project structure

| Location | Contents |
| --- | --- |
| `catalogue-service/` | Independent Maven project for the catalogue API and its tests |
| `order-service/` | Independent Maven project for the order API and test preparation |
| `doc/` | English technical documentation |
| `0 Docs/` | Original assignment and design materials |

Both services use controllers, services, repositories, entities, DTOs, mappers, configuration classes and exception handlers. Constructor injection supplies dependencies. JPA entities represent stored data; DTO records represent API payloads.

## Current status

Catalogue tests cover services, controllers, repositories, entities and mapping. The order service currently has only the generated application-context test; detailed tests remain to be written. This documentation update does not claim that builds or tests have passed.

The current order and order-line services do not define service-level transactions. Stock compensation is best effort and does not provide atomic transactions across the two applications. Read the [project report](doc/PROJECT_REPORT.md#current-limitations-and-next-work) before treating the application as production-ready.

## Git workflow

`main` contains both services. The service-specific branches currently contain only their respective service: `feature/catalogue-api`, `testcatalogue`, `feature/order-service` and `test/order-service`.

Because those branches omit the other service, their deletion commits must not be merged blindly into `main`. Transfer selected changes on a branch created from `main`, or restore the other service before proposing a merge. Keeping a complete checkout of `main` is the simplest way to see both applications in IntelliJ.
