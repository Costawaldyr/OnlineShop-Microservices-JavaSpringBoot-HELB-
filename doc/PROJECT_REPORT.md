# Project Report

## Project purpose

Online Shop is an academic application that applies Java, Spring Boot, Spring MVC and Spring Data JPA to a small shopping domain. Its main objective is to split catalogue management and order management into two applications that communicate through REST.

The system must support products and categories, stock changes, orders containing multiple lines, and access to order details. Client management provides an existing client to associate with an order. Swagger UI makes the APIs usable without building a separate frontend.

## Functional scope

| Area | Implemented operations |
| --- | --- |
| Categories | Create, list, find by ID, update and delete an empty category |
| Products | Create, list, find by ID, filter by category, update and delete |
| Stock | Increase stock; decrease stock only when the requested quantity is available |
| Clients | Create, list and find by ID |
| Orders | Create with lines, list, find by ID, filter by client, change client, cancel and delete after cancellation |
| Order lines | Add to an existing confirmed order, list by order, find within an order, change quantity and delete while keeping at least one line |

The current client controller does not expose update or delete endpoints. Order update changes the associated client; it does not accept a free-form status, date or total. Order-line update changes quantity while retaining the product and purchase price.

## Architecture and responsibility separation

Each application is an independent Maven project and executable Spring Boot JAR. The catalogue service owns product and category data. The order service owns client, order and order-line data. Both use PostgreSQL, with separate database names.

Controllers handle routes, request bodies, path variables and HTTP responses. Services perform business checks and coordinate repositories and HTTP calls. Repositories extend `JpaRepository` to access stored entities. Mappers create DTO records from entities, so the REST layer does not expose JPA associations directly. Exception handlers translate selected domain exceptions into HTTP responses.

Dependencies are supplied through constructors. `@Service`, `@Component` and REST controller annotations register application components. `RestClientConfig` uses `@Configuration` and `@Bean` because it constructs a configured library object used by `ProductClient`.

`Optional<Dto>` represents a single lookup that may find nothing; controllers convert absence into HTTP 404. Lists represent multiple results and can legitimately be empty.

## Persistence design

A category contains products. A product stores its category reference, price and available stock. A client can have several orders. An order contains several order lines. Each line stores a product ID, quantity and purchase unit price.

Only relationships within one application are JPA associations. `OrderLineEntity.productId` refers logically to a catalogue product, but it is a scalar ID rather than a JPA relationship to `ProductEntity`. There is no cross-database foreign key.

Money is represented with `BigDecimal`. Each line stores the catalogue price read while preparing the order. Later changes to the product price do not change an existing order's unit price. The total is calculated from saved line prices and quantities rather than accepted from an API caller.

## Order creation

The order service validates the request and loads the client. It prepares all lines by fetching products from the catalogue, then calculates the total. It decreases stock for each line, changes the order from the initial in-memory `PENDING` state to `CONFIRMED`, saves the order and flushes the repository.

If an exception occurs during reservation, saving, flushing or response mapping, the service tries to restore stock for reservations that returned successfully. A failure while preparing product information occurs before stock reservation.

The initial `PENDING` order is not saved as a separate creation step. A failed request therefore does not intentionally create a persisted pending order. See the communication guide for what failures can still leave behind.

## Changes and cancellation

A confirmed order can receive a new line. Changing an existing line adjusts only the difference in quantity, then recalculates the order total. Removing a line returns its quantity to stock, but removing the last line is rejected: the user must cancel the order instead.

Cancellation returns all line quantities to the catalogue and sets the order status to `CANCELLED`. A second cancellation is rejected. Deletion is allowed only after cancellation, and deletes the related lines through JPA cascade. Deletion does not return stock a second time.

## Validation and errors

DTO constraints validate catalogue requests and order creation requests. Selected business errors map to 400, 404, 409 or 503. A custom error DTO contains status, message and timestamp. Optional lookup endpoints can return 404 without an error body.

Category creation checks names without case sensitivity and trims the name. The database also enforces a unique category name, but its ordinary unique constraint is not equivalent to case-insensitive application validation. The current category update method does not repeat the creation duplicate check. Client creation similarly checks email without case sensitivity before saving.

## Tests and evidence

The catalogue contains tests for entities, mappers, repositories, services and controllers. Repository tests use a dedicated PostgreSQL test database. Mock-based service and controller tests isolate dependencies. The order project currently contains only the generated context-loading test.

No test run was performed for this documentation update. Therefore this report records implementation and test scope, not a new passing result, coverage percentage or production certification.

## Current limitations and next work

1. `OrderService` and `OrderLineService` have no service-level `@Transactional` boundary. Repository methods have their own transaction behavior, but this does not make an entire multi-step operation atomic. A save may commit before a later failure triggers stock compensation. Review transaction boundaries and add database-backed tests.
2. Compensation is best effort. A timeout can hide a stock change that succeeded remotely, compensation can fail, and no durable reconciliation queue or idempotency key is implemented.
3. Read-modify-save operations on the same order have no explicit optimistic version field or locking strategy. Concurrent changes require additional review.
4. `OrderLineNotFoundException` has no dedicated handler in the current order exception advice. A missing line during update or deletion can produce HTTP 500 instead of a domain-specific 404.
5. `ClientDto` imports validation annotations but does not apply them to its fields. `@Valid` alone does not enforce nonblank name, email or address. Null values can reach service calls such as `trim()`.
6. No application authentication, authorization or rate limiting is implemented. Stock operations are exposed directly.
7. Hibernate schema update is used for development; controlled migrations and explicit monetary column precision are not yet supplied.
8. The environment example and Docker initialization now describe separate application and test databases. Compose parsing and initialization-script syntax were checked; actual container startup remains unverified because Docker daemon access was denied.
9. Order unit, repository, controller and interservice integration tests remain to be written. Timeout behavior should also be verified because a manually constructed Apache request factory is used.

These items describe the current code; this documentation task does not change the implementation.

## Next verification milestone

Run both applications against separate PostgreSQL databases. Complete the Swagger scenario, verify stock before and after each operation, and implement order tests for both successful operations and failures. Record the commands, actual results and remaining issues before the final academic delivery.
