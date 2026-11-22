# Dependencies and Build Tools

The authoritative dependency declarations are [catalogue pom.xml](../catalogue-service/pom.xml) and [order pom.xml](../order-service/pom.xml). This document describes what is declared, not the latest available releases.

## Versions declared by this project

| Component | Declared value | Purpose |
| --- | --- | --- |
| Java | 21 | Language and compilation target |
| Spring Boot parent | 4.1.2 | Dependency management and Maven defaults |
| Both application artifacts | 0.0.1-SNAPSHOT | Development JAR version |
| springdoc OpenAPI starter | 3.2.0 | Generated OpenAPI description and Swagger UI |
| Other library and plugin versions | Managed by the parent unless overridden | Resolve exact versions through Maven |

The Spring Boot parent manages many transitive dependency versions. A starter groups libraries; it is not a single implementation class. The PostgreSQL JDBC driver is a Java library and does not install or start the PostgreSQL server.

## Application dependencies

| Maven artifact | Service | Role |
| --- | --- | --- |
| `spring-boot-starter-webmvc` | Both | Servlet-based REST controllers, HTTP request handling and JSON integration |
| `spring-boot-starter-data-jpa` | Both | Spring Data repositories, JPA and Hibernate persistence |
| `spring-boot-starter-validation` | Both | Jakarta validation for annotated DTO/entity fields |
| `springdoc-openapi-starter-webmvc-ui` | Both | `/v3/api-docs` and Swagger UI |
| `postgresql` | Both, runtime | JDBC communication with PostgreSQL |
| `lombok` | Both, optional | Compile-time getters, setters and no-argument constructors for annotated entities |
| `spring-boot-starter-actuator` | Both | Application monitoring infrastructure; exact exposed endpoints depend on configuration |
| `spring-boot-devtools` | Both, runtime and optional | Development conveniences such as restart support |
| `spring-boot-starter-restclient` | Orders | Spring Boot integration for synchronous RestClient and its builder |
| `httpclient5` | Orders | Apache HTTP client used through HttpComponentsClientHttpRequestFactory |
| `spring-boot-starter-hateoas` | Both | Hypermedia support; current DTO responses do not implement a linked-resource model |
| `spring-boot-starter-webservices` | Both | SOAP web-services support; current business controllers implement REST, not SOAP endpoints |

HATEOAS and SOAP starters are present in the POMs, but their presence does not mean the application exposes HATEOAS or SOAP functionality. They are candidates for a later dependency review, not changes made by this documentation update.

## Test dependencies

Both POMs declare these test-scoped starters:

- `spring-boot-starter-actuator-test`
- `spring-boot-starter-data-jpa-test`
- `spring-boot-starter-hateoas-test`
- `spring-boot-starter-validation-test`
- `spring-boot-starter-webmvc-test`
- `spring-boot-starter-webservices-test`

The test code uses JUnit Jupiter, Mockito and Spring testing facilities supplied through the resolved test dependencies. JUnit runs test methods and assertions. Mockito creates simulated collaborators for isolated tests. MockMvc invokes MVC endpoints without starting an external HTTP server. JPA repository tests use Spring's JPA test support and a real PostgreSQL database.

Do not infer that all those test starters are exercised simply because they are declared. The order service has not yet received its detailed test suite.

## Build plugins

| Plugin | Role |
| --- | --- |
| `maven-compiler-plugin` | Compile application and test code; configure Lombok annotation processing |
| `spring-boot-maven-plugin` | Build executable Spring Boot JARs |
| Maven lifecycle plugins inherited from parent/default lifecycle | Clean, copy resources, run tests and package artifacts |

Each service has its own Maven wrapper files. There is no root aggregator `pom.xml`; run Maven from the selected service directory.

## Inspect resolved dependencies

From either service directory:

```bash
mvn dependency:tree
mvn help:effective-pom
mvn -version
```

Use these commands when checking the exact version of a transitive library or diagnosing a missing class. No new dependency resolution or version audit was performed for this documentation update.

## Common build failures

`release version 21 not supported` means the JDK Maven is using cannot compile for Java 21. Compare `mvn -version` with IntelliJ's configured project and Maven JDKs.

`Unable to access jarfile` can be a consequence of an earlier failed package step: check the first Maven error before trying to launch a JAR that was not produced.

A test database connection failure can also stop `mvn package`, because packaging runs tests before building the final artifact.
