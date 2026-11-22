# Database Guide

## Database ownership

| Application | Example database | Tables |
| --- | --- | --- |
| Catalogue | `online_store_db` | `category`, `product` |
| Orders | `online_shop_order_db` | `clients`, `orders`, `order_line` |
| Catalogue repository tests | `online_shop_catalogue_test` | Test copies of catalogue tables |

Use separate application database names even when both services connect to the same local PostgreSQL server and account. Do not use the application database for repository tests.

## Create the local databases

Connect as a PostgreSQL administrator. If the `helb` role does not already exist, create it once:

```sql
CREATE ROLE helb LOGIN PASSWORD 'replace_with_your_local_password';
```

If it already exists, skip role creation. An existing role does not imply that its password is the value in an old terminal command. Set the intended password as an administrator if needed:

```sql
ALTER ROLE helb WITH PASSWORD 'replace_with_your_local_password';
```

Create each required database once, outside a transaction block:

```sql
CREATE DATABASE online_store_db OWNER helb;
CREATE DATABASE online_shop_order_db OWNER helb;
CREATE DATABASE online_shop_catalogue_test OWNER helb;
```

These are setup commands for a new local environment, not instructions to drop an existing database. Use the actual application names from your `.env` if they differ.

Set the root environment file as shown in the [README](../README.md#database-and-environment-setup). Hibernate currently uses `spring.jpa.hibernate.ddl-auto=update` to update tables when each application starts. No controlled migration history is currently supplied.

## Catalogue model

### category

| Java field | Column | Mapping and constraints |
| --- | --- | --- |
| id | id | Long; generated identity primary key |
| name | name | String; not null; unique |
| description | description | String; nullable in entity mapping |
| productList | No column | One-to-many inverse association, mapped by product.category |

### product

| Java field | Column | Mapping and constraints |
| --- | --- | --- |
| id | id | Long; generated identity primary key |
| name | name | String; not null |
| description | description | String; not null |
| price | price | BigDecimal; not null |
| stock | quantity_stock | Integer; mapping does not explicitly set not-null |
| category | category_id | Many-to-one association; mapping does not explicitly require a non-null category |

The product DTO requires stock and categoryId for API creation/update, but DTO validation is different from database nullability. A product name is not declared unique.

Category-to-product mapping uses cascade ALL and orphan removal. The service's delete method explicitly refuses a nonempty category, preventing that cascade from being used to delete its products through the normal endpoint.

## Order model

### clients

| Java field | Column | Mapping and constraints |
| --- | --- | --- |
| id | id | Long; generated identity primary key |
| name | name | String; not null |
| email | email | String; not null; unique; entity has email validation |
| address | address | String; not null |

### orders

| Java field | Column | Mapping and constraints |
| --- | --- | --- |
| id | id | Long; generated identity primary key |
| client | client_id | Required many-to-one association; lazy fetch |
| orderDate | order_date | LocalDateTime; not null; initialized when constructed |
| status | status | Enum stored as a string; not null |
| totalAmount | total_amount | BigDecimal; not null; initially zero |
| orderLineList | No column | One-to-many inverse association, mapped by order_line.order |

### order_line

| Java field | Column | Mapping and constraints |
| --- | --- | --- |
| id | id | Long; generated identity primary key |
| productId | product_id | Long; not null; logical reference to catalogue |
| order | order_id | Required many-to-one association; lazy fetch |
| quantity | quantity | int; not null |
| unitPrice | unit_price | BigDecimal; not null; captured purchase price |

An order cascades persistence and deletion to its lines, with orphan removal. Removing a line from the collection can remove its database row. The service blocks deletion of the last line and requires cancelling the order before deleting it.

Status values are `PENDING`, `CONFIRMED` and `CANCELLED`. They are application enum values stored as text; the entity does not declare a database enum type.

## Relationships and cross-service references

- `product.category_id` references `category.id` within the catalogue database.
- `orders.client_id` references `clients.id` within the order database.
- `order_line.order_id` references `orders.id` within the order database.
- `order_line.product_id` is a logical reference only. The product is checked through HTTP, not a foreign key or a JPA join across databases.

The ER diagrams in [DIAGRAMS.md](DIAGRAMS.md#database-relationships) deliberately separate the two physical databases. Deleting a catalogue product does not automatically delete historical order lines in the other database.

## Prices and totals

```text
line amount = unit_price × quantity
total amount = sum of line amounts
```

A line keeps its unit price when its quantity changes. A catalogue price update affects future line preparation, not historical line prices. Prices and totals are BigDecimal values; explicit database precision and scale are not declared on the entity columns. Inspect generated DDL before relying on a particular numeric definition.

No currency field, tax model, payment record or shipping charge is implemented. The examples should not be interpreted as an accounting model.

## Inspect the schema

From a terminal, connect using the database name you configured:

```bash
psql -h localhost -U helb -d online_store_db
```

Inside psql:

```sql
\dt
\d category
\d product
SELECT id, name, price, quantity_stock, category_id FROM product;
```

For the order database:

```bash
psql -h localhost -U helb -d online_shop_order_db
```

```sql
\dt
\d clients
\d orders
\d order_line
SELECT id, client_id, status, total_amount FROM orders;
SELECT id, order_id, product_id, quantity, unit_price FROM order_line;
```

Generated identity values can have gaps and do not necessarily restart at 1 after rollback or deletion. Always use returned IDs in API calls and tests.

## Docker alternative

Use [DOCKER_POSTGRES.md](DOCKER_POSTGRES.md) to start PostgreSQL 17 with a simple Compose file. Create the databases manually through psql using the commands above. There is no initialization script or Dockerfile; Hibernate creates/updates application tables when each API starts.
