# Architecture, Flow and Sequence Diagrams

These diagrams describe the current source and intended successful operations. Database actions are shown separately from HTTP stock changes because there is no atomic transaction spanning both services. Order services currently have no service-level transaction boundary.

## System architecture

```mermaid
flowchart TD
    User["Swagger UI or HTTP client"]
    Catalogue["Catalogue API · port 8081"]
    Orders["Order API · port 8082"]
    ProductClient["ProductClient and RestClient"]
    CatalogueDB[("Catalogue PostgreSQL database")]
    OrderDB[("Order PostgreSQL database")]
    User --> Catalogue
    User --> Orders
    Orders --> ProductClient
    ProductClient -->|"HTTP product and stock requests"| Catalogue
    Catalogue --> CatalogueDB
    Orders --> OrderDB
```

The order service reads product information through HTTP. It never joins its tables to the catalogue database. Each service contains its own controllers, services, repositories, DTOs and mappers.

## Order lifecycle

```mermaid
stateDiagram-v2
    [*] --> PENDING: Construct order in memory
    PENDING --> CONFIRMED: Reserve all stock and save
    CONFIRMED --> CONFIRMED: Change client or lines
    CONFIRMED --> CANCELLED: Return stock and save
    CANCELLED --> [*]: Delete order and lines
```

`PENDING` is the initial in-memory status during creation, not a separately saved draft exposed by a draft endpoint. Failure paths are omitted from this state diagram: compensation and persistence can fail, as described below. Cancelled orders cannot be updated through the order update method or have their lines changed.

## Create an order: decision flow

```mermaid
flowchart TD
    Start["POST /orders"] --> Validate{"Valid request and existing client?"}
    Validate -->|No| Reject["Return validation or not-found error"]
    Validate -->|Yes| Prepare["Read all products and capture prices"]
    Prepare --> ValidProducts{"All lines prepared?"}
    ValidProducts -->|No| Reject
    ValidProducts -->|Yes| Total["Calculate total amount"]
    Total --> Reserve["Decrease stock for each line"]
    Reserve --> Reserved{"All reservations completed?"}
    Reserved -->|No| Undo["Try restoring acknowledged reservations"]
    Reserved -->|Yes| Save["Set CONFIRMED, save and flush"]
    Save --> Saved{"Save, flush and mapping completed?"}
    Saved -->|No| Undo
    Saved -->|Yes| Response["Return 201 with OrderDto"]
    Undo --> Error["Rethrow original error; reconciliation may be needed"]
```

Only reservation calls that returned successfully are remembered for compensation. A timeout can still conceal a remote stock change. The flow does not imply that a later error rolls back an already committed local save.

## Create an order: successful sequence

```mermaid
sequenceDiagram
    participant User as API caller
    participant Orders as Order service
    participant Client as ProductClient
    participant Catalogue as Catalogue API
    participant DB as Order database
    User->>Orders: POST /orders with clientId and lines
    Orders->>DB: Load existing client
    DB-->>Orders: Client
    loop Every requested line
        Orders->>Client: getProduct(productId)
        Client->>Catalogue: GET /products/{id}
        Catalogue-->>Client: Product JSON with price
        Client-->>Orders: ProductResponse
        Note over Orders: Capture unit price in line
    end
    Note over Orders: Calculate total from unit prices and quantities
    loop Every prepared line
        Orders->>Client: decreaseStock(productId, quantity)
        Client->>Catalogue: POST /products/{id}/stock/decrease
        Catalogue-->>Client: 204 if stock is available
        Client-->>Orders: Reservation completed
    end
    Orders->>DB: Save CONFIRMED order and cascading lines
    Orders->>DB: Flush repository
    Orders-->>User: 201 with OrderDto
```

The catalogue persists each stock operation in its own database, which is not shown as a sixth participant. Controller validation and DTO mapping are included within the order-service participant for readability.

## Partial creation failure: compensation

```mermaid
sequenceDiagram
    participant User as API caller
    participant Orders as Order service
    participant Client as ProductClient
    participant Catalogue as Catalogue API
    User->>Orders: Create order with two prepared lines
    Orders->>Client: Reserve product A
    Client->>Catalogue: Decrease stock A
    Catalogue-->>Client: 204
    Client-->>Orders: Success
    Orders->>Client: Reserve product B
    Client->>Catalogue: Decrease stock B
    Catalogue-->>Client: 409 insufficient stock
    Client-->>Orders: InsufficientStockException
    Orders->>Client: Restore product A
    Client->>Catalogue: Increase stock A
    alt Restoration succeeds
        Catalogue-->>Client: 204
    else Restoration fails
        Catalogue-->>Client: Error or connection failure
        Note over Orders: Log reconciliation requirement
    end
    Orders-->>User: Original stock conflict, 409
```

This specific sequence fails before saving an order. A save, flush or response-mapping failure can happen later and has different local persistence consequences. The compensation implementation is best effort, not a durable saga.

## Change an order-line quantity

```mermaid
sequenceDiagram
    participant User as API caller
    participant Lines as OrderLineService
    participant Catalogue as Catalogue API
    participant DB as Order database
    User->>Lines: PUT line with query quantity
    Lines->>DB: Load confirmed order and scoped line
    DB-->>Lines: Order and line
    Note over Lines: Difference = new quantity - old quantity
    alt Difference is positive
        Lines->>Catalogue: Decrease stock by difference
        Catalogue-->>Lines: 204
    else Difference is negative
        Lines->>Catalogue: Increase stock by absolute difference
        Catalogue-->>Lines: 204
    else Difference is zero
        Note over Lines: Return existing line without stock changes
    end
    opt Difference is not zero
        Note over Lines: Keep purchase price and recalculate total
        Lines->>DB: Save order and flush
    end
    Lines-->>User: 200 with OrderLineDto
```

ProductClient carries the HTTP requests but is collapsed into the catalogue interaction here. If saving or flushing fails after a stock adjustment, the service attempts the inverse adjustment and rethrows the error.

## Add or delete an order line

```mermaid
flowchart TD
    Request["Line mutation request"] --> Load["Load order and require CONFIRMED"]
    Load --> Action{"Operation?"}
    Action -->|Add| Prepare["Read product price and validate quantity"]
    Prepare --> Reserve["Decrease stock and attach new line"]
    Action -->|Delete| Count{"More than one line?"}
    Count -->|No| Reject["Reject; cancel order instead"]
    Count -->|Yes| Return["Increase stock and remove selected line"]
    Reserve --> Save["Recalculate total, save and flush"]
    Return --> Save
    Save --> Result{"Completed?"}
    Result -->|Yes| Success["Return DTO for add, or 204 for delete"]
    Result -->|No| Undo["Try inverse stock operation and rethrow"]
```

The delete lookup checks both line ID and order ID. Cascading persistence saves new lines; orphan removal removes deleted lines from the order database.

## Cancel and delete an order

```mermaid
sequenceDiagram
    participant User as API caller
    participant Orders as Order service
    participant Catalogue as Catalogue API
    participant DB as Order database
    User->>Orders: POST /orders/{id}/cancel
    Orders->>DB: Load confirmed order with lines
    DB-->>Orders: Order
    loop Every saved line
        Orders->>Catalogue: Increase stock by line quantity
        Catalogue-->>Orders: 204
    end
    Orders->>DB: Save CANCELLED status and flush
    Orders-->>User: 200 with cancelled OrderDto
    User->>Orders: DELETE /orders/{id}
    Orders->>DB: Delete cancelled order and cascade lines
    Note over Orders,Catalogue: Deletion makes no additional stock call
    Orders-->>User: 204 with no body
```

If a stock return or local cancellation step fails, the service tries to reserve the units already returned. Repeated cancellation is rejected instead of returning stock twice. This successful sequence is not an idempotency guarantee for concurrent or retried requests.

## Database relationships

### Catalogue database

```mermaid
erDiagram
    CATEGORY o|--o{ PRODUCT : groups
    CATEGORY {
        bigint id PK
        string name UK
        string description
    }
    PRODUCT {
        bigint id PK
        bigint category_id FK
        string name
        string description
        decimal price
        integer quantity_stock
    }
```

The entity mapping permits a null product category; the API requires categoryId and the service loads an existing category. The diagram shows the mapping's optionality rather than strengthening the SQL constraint beyond the code.

### Order database

```mermaid
erDiagram
    CLIENTS ||--o{ ORDERS : places
    ORDERS ||--o{ ORDER_LINE : contains
    CLIENTS {
        bigint id PK
        string name
        string email UK
        string address
    }
    ORDERS {
        bigint id PK
        bigint client_id FK
        datetime order_date
        string status
        decimal total_amount
    }
    ORDER_LINE {
        bigint id PK
        bigint order_id FK
        bigint product_id
        integer quantity
        decimal unit_price
    }
```

`product_id` is deliberately not marked FK: its target is owned by the catalogue database. The API requires at least one line when creating an order, but the JPA relationship alone does not enforce that minimum as a database constraint.
