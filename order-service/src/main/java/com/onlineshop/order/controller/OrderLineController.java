package com.onlineshop.order.controller;

import com.onlineshop.order.dto.CreateOrderLineDto;
import com.onlineshop.order.dto.OrderLineDto;
import com.onlineshop.order.service.OrderLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Order Lines", description = "Endpoints for managing order lines")
@RestController
@RequestMapping("/orders/{orderId}/lines")
public class OrderLineController {

    private final OrderLineService orderLineService;

    public OrderLineController(OrderLineService orderLineService) {
        this.orderLineService = orderLineService;
    }

    @Operation(summary = "Create a new order line")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderLineDto create( @PathVariable("orderId") Long orderId, @Valid @RequestBody CreateOrderLineDto dto ) {
        return orderLineService.createOrderLine(orderId, dto);
    }

    @Operation(summary = "Get all order lines for a specific order")
    @GetMapping
    public List<OrderLineDto> getAll( @PathVariable("orderId") Long orderId) {
        return orderLineService.getLinesByOrderId(orderId);
    }

    @Operation(summary = "Get a specific order line by ID")
    @GetMapping("/{lineId}")
    public ResponseEntity<OrderLineDto> getById( @PathVariable("orderId") Long orderId, @PathVariable("lineId") Long lineId ) {
        return orderLineService.getOrderLineById(orderId, lineId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Change the quantity while keeping the purchase price.
    @Operation(summary = "Update the quantity of an order line")
    @PutMapping("/{lineId}")
    public OrderLineDto update( @PathVariable("orderId") Long orderId, @PathVariable("lineId") Long lineId,@RequestParam("quantity") int quantity) {
        return orderLineService.updateOrderLine( orderId, lineId, quantity );
    }

    @Operation(summary = "Delete an order line")
    @DeleteMapping("/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete( @PathVariable("orderId") Long orderId, @PathVariable("lineId") Long lineId ) {
        orderLineService.deleteOrderLine(orderId, lineId);
    }
}