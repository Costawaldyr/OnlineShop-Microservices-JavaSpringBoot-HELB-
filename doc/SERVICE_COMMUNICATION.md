# Communication Between Services

## Ownership and connection

The order API is the caller. The catalogue API owns the product information and stock that the caller needs. Catalogue code does not call the order API, and order code does not access catalogue tables directly.

`RestClientConfig` creates a `RestClient` bean using the Spring-provided builder, an Apache `HttpComponentsClientHttpRequestFactory`, and the base URL from configuration:

```properties
product-service.api.base-url=http://localhost:8081
```

`ProductClient` receives that bean through constructor injection. It is responsible for HTTP requests and translating selected remote failures. `OrderService` and `OrderLineService` are responsible for deciding when those calls are needed.

## Calls made by ProductClient

| Java method | HTTP request | Successful response | Use |
| --- | --- | --- | --- |
| `getProduct(productId)` | `GET /products/{id}` | 200, mapped to ProductResponse | Prepare a line and capture its price |
| `decreaseStock(productId, quantity)` | `POST /products/{id}/stock/decrease?quantity=...` | 204 | Reserve additional units |
| `increaseStock(productId, quantity)` | `POST /products/{id}/stock/increase?quantity=...` | 204 | Return units or compensate a reservation |

`ProductResponse` contains ID, name, price and stock. Unknown JSON properties are ignored, allowing the catalogue DTO to contain additional fields such as description and category ID.

No API-version header is configured in the current RestClient bean. Importing `ApiVersionInserter` alone does not enable versioning. Swagger's documented version label does not change request routing.

## Creation algorithm

1. Validate that an order has a client ID and at least one line.
2. Load the client from the order database.
3. For every requested line, validate the quantity and read the product from the catalogue. Reject missing or negative prices.
4. Create order-line objects with a snapshot of the product price.
5. Calculate total amount using `unitPrice × quantity` for each line.
6. Decrease catalogue stock for each line; remember calls that returned successfully.
7. Set the order status to confirmed, save it and flush the order repository.
8. Map the stored entity to the response DTO.

A catalogue stock value read during preparation is only information from that moment. The authoritative stock availability check happens in the catalogue's conditional update query when decreasing stock.

The catalogue performs a single update with the condition `stock >= quantity`. A result of one updated row means success. Zero rows causes the service to distinguish a missing product from insufficient stock. This prevents that individual successful decrement from making stock negative; it does not make an entire multi-product order atomic.

## Order-line modifications

| Operation | Stock change | Price and total |
| --- | --- | --- |
| Add line | Decrease by new line quantity | Read current catalogue price for new line; recalculate total |
| Increase quantity | Decrease by new quantity minus old quantity | Keep existing unit price; recalculate total |
| Decrease quantity | Increase by old quantity minus new quantity | Keep existing unit price; recalculate total |
| Unchanged quantity | No remote stock call | Return existing line |
| Delete a line | Increase by removed quantity | Recalculate total; reject deletion of the last line |
| Cancel order | Increase by each line quantity | Preserve historical line prices and amount; mark cancelled |
| Delete cancelled order | No remote stock call | Remove order and its lines locally |

Only confirmed orders allow line changes. Updating an order's client does not adjust stock.

## Error translation

| Remote result | Translation in ProductClient |
| --- | --- |
| 404 for a product call | ProductNotFoundException; order API advice maps it to 404 |
| 409 during stock decrease | InsufficientStockException; maps to 409 |
| Catalogue 5xx response | CatalogueUnavailableException; maps to 503 |
| ResourceAccessException, such as a connection failure | CatalogueUnavailableException; maps to 503 |
| Other errors | Not explicitly translated by these handlers; inspect logs and actual response |

The order configuration declares connect/read timeout properties of 2s and 5s. Because the code manually constructs its request factory, verify the effective timeouts with an integration test instead of assuming those settings reach that factory.

## Compensation and its limits

Creation remembers each successfully completed decrease call. On a later failure, it tries to increase stock for those lines. Cancellation does the reverse: if a later step fails after returning stock, it tries to decrease the stock already returned. Line changes similarly try to reverse the quantity difference.

Compensation helpers log failures and retain the original exception. They do not durably schedule a retry. A lost response is particularly important: the catalogue may commit a stock change even though the caller receives a timeout. The caller then cannot safely infer that nothing changed.

The current order services also lack a service-level transaction. Repository `save()` and `flush()` are separate method calls, so stock compensation alone cannot guarantee rollback of already committed order data. There is no distributed transaction, idempotency key, outbox or durable saga coordinator. Concurrency, retry and reconciliation behavior remain future work.

See [sequence diagrams](DIAGRAMS.md) for the intended successful exchanges and the explicitly limited failure paths.
