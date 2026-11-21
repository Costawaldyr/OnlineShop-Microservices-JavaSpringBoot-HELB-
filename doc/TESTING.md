# Testing Guide

## Existing test scope

| Area | Catalogue | Orders |
| --- | --- | --- |
| Application context | Present | Present |
| Entities | Present | To write |
| Mappers | Present | To write |
| Repositories with PostgreSQL | Present | To write |
| Services with Mockito | Present | To write |
| Controllers with MockMvc | Present | To write |
| Real HTTP communication between services | No dedicated test identified | To write |

This is an inventory of test sources, not a statement that all tests currently pass. No Maven tests were executed for the documentation update.

## Run the catalogue tests

Start PostgreSQL and create `online_shop_catalogue_test`. The catalogue's `src/test/resources/application.properties` selects that database. It currently contains local test credentials; these must match the PostgreSQL account on the machine running the tests. Do not copy private credentials into public documentation.

From the repository root:

```bash
cd catalogue-service
mvn clean test
```

Or run `bash test.sh` from that service directory. Tests using `@DataJpaTest` and database replacement NONE require the real PostgreSQL test database, not an embedded database. Their test transactions normally roll back after each test. Rollback does not reset database sequences and does not clean up rows left by manual requests or other non-rolled-back operations.

## Run the order tests

From the repository root:

```bash
cd order-service
mvn clean test
```

At present this runs only `OrderServiceApplicationTests.contextLoads()`. There is no separate order test properties file in the inspected source. The context test therefore relies on application configuration and can connect to the normal order database. Configure a dedicated test database before adding database tests; do not claim this existing context test is fully isolated.

## Course-style unit test structure

Use public test methods with names that state the expected behavior, such as `shouldReturnOrderWhenIdExists`. Use `@BeforeEach` to create fresh test data. Keep comments in English and separate Arrange, Act and Assert.

For service tests, mock repositories, the mapper and ProductClient as appropriate, then instantiate the real service using its constructor. Stub only the collaborators used in that test. Check returned DTOs or exceptions and verify important side effects such as stock calls and saved entity changes.

An Optional-returning method should be tested for both present and empty results. A list-returning method should be tested with results and with an empty list. Do not mock the method under test.

## OrderService cases to implement

- Creation with a valid client, products and available stock.
- Rejection of missing client ID, missing/empty lines and unknown clients.
- Product preparation failure before stock reservation.
- First reservation failure and later reservation failure.
- Restoration of earlier reservations after save or flush failure.
- Find by ID, empty Optional, list all and list by existing client.
- Client change and rejection when an order is cancelled or the new client is absent.
- Cancellation, repeated cancellation, and partial stock return failure.
- Deletion after cancellation and rejection of deleting a confirmed order.

## OrderLineService cases to implement

- Product and price lookup; invalid product IDs, quantities and prices.
- Total calculation from several saved unit prices and quantities.
- Adding a line to a confirmed order and compensation after persistence failure.
- Quantity increase, decrease and unchanged quantity.
- Preservation of original product and unit price on update.
- Scoped lookup that prevents access to a line through another order ID.
- Deleting a line, recalculating total and refusing removal of the last line.
- Compensation failures and operations on cancelled/pending orders.

## Controller and integration checks

Controller tests should cover route mapping, query parameters, JSON validation, HTTP codes and serialized DTOs. Repository tests should check stored relationships and constraints with PostgreSQL.

Real integration tests should check the HTTP contract between ProductClient and catalogue, stock before/after creation and cancellation, unavailable catalogue behavior, timeout behavior, and persistence failures. Mockito cannot demonstrate that JPA transactions, lazy relationships or the actual HTTP client behave correctly.

A useful manual integration scenario is in the [Swagger guide](API_GUIDE.md#complete-manual-scenario). Manual checks complement automated tests; they do not replace them.

## Read a failing build

Read the first meaningful cause in the Maven output, then inspect `target/surefire-reports/`. Separate compilation problems from assertion failures and application-context/database startup errors. A final `BUILD FAILURE` line alone does not identify the cause.
