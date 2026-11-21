# PostgreSQL with Docker

Docker is an optional way to run PostgreSQL without installing PostgreSQL directly on the host. The two Java APIs still run in IntelliJ or separate terminals.

## Configure and start

Install and start Docker with Docker Compose. From the repository root, create `.env` from `.env.example` if it does not already exist. Otherwise edit the existing file without overwriting your settings. Set DB_USERNAME and DB_PASSWORD, plus the two database names required by the Java applications. Keep values unquoted because Spring also reads the file as properties.

```bash
docker compose config --quiet
docker compose up -d postgres
docker compose logs postgres
```

Wait until the logs say PostgreSQL is ready to accept connections. The container publishes port 5432 and stores its data in the named `pgdata` volume. If another PostgreSQL server already uses port 5432, choose which server to run on that port before starting the container.

## Create the databases manually

Open psql inside the container:

```bash
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d postgres'
```

For the example names from `.env.example`, run these commands once, outside a transaction:

```sql
CREATE DATABASE online_store_db;
CREATE DATABASE online_shop_order_db;
```

The connected user owns the databases it creates. If you configured different names, use those names instead. The PostgreSQL image has already created the account from DB_USERNAME; do not create that role a second time.

If you want to run database tests, create dedicated databases too:

```sql
CREATE DATABASE online_shop_catalogue_test;
CREATE DATABASE online_shop_order_test;
```

Creating a test database does not automatically configure the tests to use it. See [TESTING.md](TESTING.md), and ensure test credentials match your PostgreSQL account. The existing order context test still needs an isolated datasource configuration before it can be considered a database-isolated test.

Use `\l` to list databases and `\q` to exit psql. Start both APIs using the [README](../README.md#start-both-apis). Hibernate creates or updates their tables. No Dockerfile or initialization script is required.

## Stop and reuse

```bash
docker compose stop postgres
docker compose start postgres
```

`docker compose down` removes the container and network but retains the named volume by default. Do not remove the volume unless you intend to delete its stored databases.

Changing DB_USERNAME or DB_PASSWORD in `.env` after the volume has been initialized does not change existing PostgreSQL accounts or passwords. Update existing accounts through psql as an administrator when needed.

## Validation

Compose configuration parsing was checked. Docker daemon access was denied in the editing environment, so actual container startup and database creation were not tested.
