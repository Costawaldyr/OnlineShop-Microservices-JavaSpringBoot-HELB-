package com.onlineshop.order.controller;

import com.onlineshop.order.dto.CreateOrderDto;
import com.onlineshop.order.dto.OrderDto;
import com.onlineshop.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create a new order")
    @ApiResponse(responseCode = "201", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@Valid @RequestBody CreateOrderDto dto) {
        return orderService.createOrder(dto);
    }

    @Operation(summary = "Get all orders")
    @GetMapping
    public List<OrderDto> getAll() {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Get a specific order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById( @PathVariable("id") Long id ) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all orders by client ID")
    @ApiResponse(responseCode = "200", description = "Orders found")
    @ApiResponse(responseCode = "404", description = "Client not found")
    @GetMapping("/by-client/{clientId}")
    public List<OrderDto> getByClient( @PathVariable("clientId") Long clientId) {
        return orderService.getOrdersByClient(clientId);
    }

    // Change the client associated with the order.
    @Operation(summary = "Update an order")
    @ApiResponse(responseCode = "200", description = "Order updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @PutMapping("/{id}")
    public OrderDto update( @PathVariable("id") Long id,@RequestParam("clientId") Long clientId ) {
        return orderService.updateOrder(id, clientId);
    }


    @Operation(summary = "Cancel an order")
    @ApiResponse(responseCode = "200", description = "Order canceled successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @PostMapping("/{id}/cancel")
    public OrderDto cancel(@PathVariable("id") Long id ) {
        return orderService.cancelOrder(id);
    }

    // The service requires cancellation before deletion.
    @Operation(summary = "Delete an order")
    @ApiResponse(responseCode = "204", description = "Order deleted successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        orderService.deleteOrder(id);
    }
}