package com.onlineshop.order.service;


import com.onlineshop.order.client.ProductClient;
import com.onlineshop.order.dto.*;
import com.onlineshop.order.entity.ClientEntity;
import com.onlineshop.order.entity.OrderEntity;
import com.onlineshop.order.entity.OrderLineEntity;
import com.onlineshop.order.enums.OrderStatus;
import com.onlineshop.order.exception.InvalidOrderException;
import com.onlineshop.order.exception.OrderAlreadyCancelledException;
import com.onlineshop.order.mapper.OrderMapper;
import com.onlineshop.order.repository.ClientRepository;
import com.onlineshop.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, ClientRepository clientRepo, ProductClient productClient, OrderLineService orderLineService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.clientRepository = clientRepo;
        this.productClient = productClient;
        this.orderLineService = orderLineService;
    }

    public OrderDto createOrder(CreateOrderDto dto) {

        validateOrder(dto);
        ClientEntity client = clientRepository.requireById(dto.clientId());
        OrderEntity order = new OrderEntity( client,OrderStatus.PENDING);

        // Prepare every line before reserving stock.
        for (CreateOrderLineDto lineDto : dto.lines()) {
            OrderLineEntity line = orderLineService.prepareOrderLine(lineDto);
            order.addOrderLine(line);
        }

        orderLineService.recalculateTotalAmount(order);
        List<OrderLineEntity> reservedLines = new ArrayList<>();

        try {
            for (OrderLineEntity line : order.getOrderLineList()) {
                productClient.decreaseStock(line.getProductId(),line.getQuantity() );
                reservedLines.add(line);
            }

            order.setStatus(OrderStatus.CONFIRMED);

            OrderEntity savedOrder = orderRepository.save(order);
            orderRepository.flush();

            return orderMapper.toOrderDto(savedOrder);

        } catch (RuntimeException exception) {
            for (OrderLineEntity line : reservedLines) {
                orderLineService.restoreStockAfterFailure(line);
            }

            throw exception;
        }
    }

    public OrderDto updateOrder(Long id, Long clientId) {
        if (clientId == null) {
            throw new InvalidOrderException("Client ID is required");
        }

        OrderEntity order = orderRepository.requireById(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("A cancelled order cannot be updated");
        }

        ClientEntity client = clientRepository.requireById(clientId);
        order.setClient(client);
        OrderEntity savedOrder = orderRepository.save(order);

        return orderMapper.toOrderDto(savedOrder);
    }

    public void deleteOrder(Long id) {
        OrderEntity order = orderRepository.requireById(id);

        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Cancel the order before deleting it");
        }

        // Related lines are removed through cascade.
        orderRepository.delete(order);
    }

    public OrderDto cancelOrder(Long id) {
        OrderEntity order = orderRepository.requireById(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException(id);
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException("Only a confirmed order can be cancelled");
        }

        List<OrderLineEntity> restoredLines = new ArrayList<>();

        try {
            for (OrderLineEntity line : order.getOrderLineList()) {
                productClient.increaseStock(  line.getProductId(), line.getQuantity() );

                restoredLines.add(line);
            }

            order.setStatus(OrderStatus.CANCELLED);

            OrderEntity savedOrder = orderRepository.save(order);
            orderRepository.flush();

            return orderMapper.toOrderDto(savedOrder);

        } catch (RuntimeException exception) {
            for (OrderLineEntity line : restoredLines) {
                orderLineService.reserveStockAfterFailure(line);
            }

            throw exception;
        }
    }



    public Optional<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toOrderDto);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderDto)
                .toList();
    }

    public List<OrderDto> getOrdersByClient(Long clientId) {
        clientRepository.requireById(clientId);

        return orderRepository.findByClientId(clientId)
                .stream()
                .map(orderMapper::toOrderDto)
                .toList();
    }

    private void validateOrder(CreateOrderDto dto) {
        if (dto == null || dto.clientId() == null) {
            throw new InvalidOrderException("Client ID is required");
        }

        if (dto.lines() == null || dto.lines().isEmpty()) {
            throw new InvalidOrderException("An order must contain at least one line");
        }
    }

}
