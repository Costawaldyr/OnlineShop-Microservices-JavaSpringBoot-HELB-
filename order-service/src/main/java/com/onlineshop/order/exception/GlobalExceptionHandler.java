package com.onlineshop.order.exception;

import com.onlineshop.order.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleClientNotFound(ClientNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderNotFound(OrderNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProductNotFound(ProductNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    // 409
    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleClientAlreadyExists(ClientAlreadyExistsException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientStock(InsufficientStockException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage());
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    // 409 (avec les autres)
    @ExceptionHandler(OrderAlreadyCancelledException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderAlreadyCancelled(OrderAlreadyCancelledException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage());
    }

    // 400 (avec les autres)
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidOrder(InvalidOrderException exception) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    // 503
    @ExceptionHandler(CatalogueUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleCatalogueUnavailable(CatalogueUnavailableException exception) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }



    private ResponseEntity<ErrorResponseDto> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponseDto(status.value(), message, LocalDateTime.now()));
    }
}