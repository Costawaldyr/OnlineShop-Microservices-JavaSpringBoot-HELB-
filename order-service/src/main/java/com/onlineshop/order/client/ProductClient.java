package com.onlineshop.order.client;

import com.onlineshop.order.dto.ProductResponse;
import com.onlineshop.order.exception.CatalogueUnavailableException;
import com.onlineshop.order.exception.InsufficientStockException;
import com.onlineshop.order.exception.ProductNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private static final int NOT_FOUND = 404;
    private static final int CONFLICT = 409;
    private static final String CATALOGUE_ERROR_MESSAGE = "Catalogue service returned an error";

    private final RestClient productRestClient;

    public ProductClient(RestClient productRestClient) {
        this.productRestClient = productRestClient;
    }

    public ProductResponse getProduct(Long productId) {
        try {
            return productRestClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .onStatus(status -> status.value() == NOT_FOUND,
                            (request, response) -> { throw new ProductNotFoundException(productId); })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> { throw new CatalogueUnavailableException(CATALOGUE_ERROR_MESSAGE); })
                    .body(ProductResponse.class);
        } catch (ResourceAccessException e) {
            throw new CatalogueUnavailableException(e);
        }
    }

    public void decreaseStock(Long productId, int quantity) {
        try {
            productRestClient.post()
                    .uri("/products/{id}/stock/decrease?quantity={quantity}", productId, quantity)
                    .retrieve()
                    .onStatus(status -> status.value() == NOT_FOUND,
                            (request, response) -> { throw new ProductNotFoundException(productId); })
                    .onStatus(status -> status.value() == CONFLICT,
                            (request, response) -> { throw new InsufficientStockException(productId); })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> { throw new CatalogueUnavailableException(CATALOGUE_ERROR_MESSAGE); })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new CatalogueUnavailableException(e);
        }
    }

    public void increaseStock(Long productId, int quantity) {
        try {
            productRestClient.post()
                    .uri("/products/{id}/stock/increase?quantity={quantity}", productId, quantity)
                    .retrieve()
                    .onStatus(status -> status.value() == NOT_FOUND,
                            (request, response) -> { throw new ProductNotFoundException(productId); })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> { throw new CatalogueUnavailableException(CATALOGUE_ERROR_MESSAGE); })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new CatalogueUnavailableException(e);
        }
    }
}