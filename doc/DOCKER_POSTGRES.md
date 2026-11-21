# PostgreSQL with Docker

This setup runs PostgreSQL in Docker. The two Java APIs run on the host, for example in IntelliJ or two terminals. It does not containerize the APIs.

## Files

| File | Purpose |
| --- | --- |
| `docker-compose.yml` | PostgreSQL 17, persistent volume, local port and readiness check |
| `.env.example` | Public example configuration; copy it to private `.env` |
| `docker/postgres/init/01-create-databases.sh` | Create application and test databases on first initialization |

The previous compose filename had a trailing space and mounted a missing SQL file. The corrected file uses the standard filename and mounts an existing initialization directory.

## Requirements

Install Docker with Docker Compose and start its engine, such as Docker Desktop on macOS. Run the following commands from the repository root. No local PostgreSQL installation is required for this Docker option.

## 1. Configure the environment

For a new checkout that does not already have `.env`:

```bash
cp .env.example .env
```

If `.env` already exists, edit it instead of overwriting your local configuration. Set DB_USERNAME, DB_PASSWORD, CATALOGUE_DB_NAME and ORDER_DB_NAME. The two test names have defaults in Compose. Replace the example password with your own password. Keep values unquoted because Spring imports the same file as Java properties.

The default databases are:

| Environment key | Default example |
| --- | --- |
| CATALOGUE_DB_NAME | online_store_db |
| ORDER_DB_NAME | online_shop_order_db |
| CATALOGUE_TEST_DB_NAME | online_shop_catalogue_test |
| ORDER_TEST_DB_NAME | online_shop_order_test |

Use different names for application databases and test databases. The configured POSTGRES_USER owns these databases. For this local educational setup it is also the administrator created by the official image; use separate restricted accounts for a production deployment.

## 2. Check the host port

The default published address is `127.0.0.1:5432`. If Homebrew PostgreSQL already uses port 5432, choose which server you want the APIs to use. Do not run two database servers on the same host port.

You can set `POSTGRES_PORT=5433` in `.env` to publish Docker on a different port. The Java application URLs currently use port 5432 explicitly, so changing POSTGRES_PORT alone does not redirect the APIs. Either use port 5432 for this container, or override each application's `SPRING_DATASOURCE_URL`, for example:

```text
Catalogue: jdbc:postgresql://localhost:5433/online_store_db
Orders:    jdbc:postgresql://localhost:5433/online_shop_order_db
```

In IntelliJ, add the appropriate value as an environment variable in each application's run configuration. Keep DB_USERNAME and DB_PASSWORD consistent with the selected database server.

## 3. Validate and start PostgreSQL

```bash
docker compose config --quiet
docker compose up -d postgres
docker compose ps
docker compose logs postgres
```

Wait until the PostgreSQL service is healthy. The first startup runs the initialization script and creates all four databases. It creates databases only; Hibernate creates/updates application tables when the respective API starts.

The health check verifies that the server accepts connections to its maintenance database. It does not prove that Java application credentials, schemas or API behavior are correct. Inspect logs for initialization errors as well.

## 4. Inspect the databases

List databases without installing psql on the host:

```bash
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d postgres -c "\\l"'
```

Open an interactive psql session:

```bash
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$CATALOGUE_DB_NAME"'
```

For the order database:

```bash
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$ORDER_DB_NAME"'
```

Use `\dt` inside psql to list tables, and `\q` to exit. Empty databases before the APIs have started are expected.

## 5. Start the APIs

Use the two terminal commands in the root [README](../README.md#start-both-apis), or start both main application classes in IntelliJ. Ensure each process loads the root `.env` and uses the correct database URL.

Then follow the [Swagger scenario](API_GUIDE.md#complete-manual-scenario). PostgreSQL readiness alone does not demonstrate that an order and its stock changes work.

## 6. Run repository tests

The initialization script creates separate test databases, but creating them does not automatically make Java tests use them.

Catalogue test properties currently select `online_shop_catalogue_test` and include existing local credentials. If your Docker credentials differ, override them explicitly from the catalogue-service directory, replacing each example argument with your chosen values:

```bash
mvn test -Dspring.datasource.url=jdbc:postgresql://localhost:5432/online_shop_catalogue_test -Dspring.datasource.username=helb -Dspring.datasource.password=YOUR_LOCAL_PASSWORD
```

Use your actual host port and username. This example passes a password on the command line, so an IntelliJ test configuration with environment variables is preferable when avoiding shell history. Use SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD there.

For the existing order context test, use the equivalent overrides with `online_shop_order_test` from the order-service directory. More order tests remain to be written. Read [TESTING.md](TESTING.md) for the current test scope.

## Stop and restart

```bash
docker compose stop postgres
docker compose start postgres
```

`docker compose down` removes the container and network while retaining the named database volume by default. Starting again reuses the existing data.

Do not remove the volume to fix a routine startup problem: deleting the volume deletes its databases. Back up any data you need before deliberately reinitializing a disposable environment.

## Initialization and existing volumes

PostgreSQL image initialization scripts run only when the data volume is empty. Changing a database name or password in `.env` after initialization does not rename existing databases or change the stored PostgreSQL role password.

For an existing volume, update the role/database deliberately through psql as an administrator. To add missing databases with the currently configured names, the idempotent initialization script can be rerun inside the running container:

```bash
docker compose exec postgres bash /docker-entrypoint-initdb.d/01-create-databases.sh
```

This creates missing databases and leaves existing ones in place. It does not change existing database owners or role passwords.

## Validation performed for this change

Compose configuration parsing and Bash syntax were checked. Access to the local Docker daemon was denied in the editing environment, so container startup, image download and actual database initialization were not executed or claimed as passing. Run the startup and inspection steps above on a machine with access to its Docker engine.
