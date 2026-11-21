package com.onlineshop.order.service;


import com.onlineshop.order.client.ProductClient;
import com.onlineshop.order.dto.CreateOrderLineDto;
import com.onlineshop.order.dto.OrderLineDto;
import com.onlineshop.order.dto.ProductResponse;
import com.onlineshop.order.entity.OrderEntity;
import com.onlineshop.order.entity.OrderLineEntity;
import com.onlineshop.order.enums.OrderStatus;
import com.onlineshop.order.exception.InvalidOrderException;
import com.onlineshop.order.exception.OrderLineNotFoundException;
import com.onlineshop.order.mapper.OrderMapper;
import com.onlineshop.order.repository.OrderLineRepository;
import com.onlineshop.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderLineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderLineService.class);

    private static final int MINIMUM_QUANTITY = 1;
    private static final int NO_QUANTITY_CHANGE = 0;
    private static final int MINIMUM_LINE_COUNT = 1;

    private final OrderLineRepository orderLineRepository;
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    public OrderLineService(OrderLineRepository orderLineRepository, OrderRepository orderRepository,ProductClient productClient, OrderMapper orderMapper) {
        this.orderLineRepository = orderLineRepository;
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.orderMapper = orderMapper;
    }

    public OrderLineDto createOrderLine(Long orderId, CreateOrderLineDto dto) {

        OrderEntity order = orderRepository.requireById(orderId);
        requireConfirmedOrder(order);

        OrderLineEntity line = prepareOrderLine(dto);

        productClient.decreaseStock( line.getProductId(), line.getQuantity() );

        try {
            order.addOrderLine(line);
            recalculateTotalAmount(order);

            orderRepository.save(order);
            orderRepository.flush();

            return orderMapper.toOrderLineDto(line);

        } catch (RuntimeException exception) {
            restoreStockAfterFailure(line);
            throw exception;
        }
    }

    public OrderLineDto updateOrderLine(Long orderId,Long lineId, int quantity) {

        validateQuantity(quantity);

        OrderEntity order = orderRepository.requireById(orderId);
        requireConfirmedOrder(order);

        OrderLineEntity line = findOrderLine(orderId, lineId);

        int quantityDifference = quantity - line.getQuantity();

        if (quantityDifference == NO_QUANTITY_CHANGE) {
            return orderMapper.toOrderLineDto(line);
        }

        // Adjust only the difference between old and new quantities.
        if (quantityDifference > NO_QUANTITY_CHANGE) {
            productClient.decreaseStock(line.getProductId(), quantityDifference);
        } else {
            productClient.increaseStock(line.getProductId(), -quantityDifference);
        }

        try {
            // Keep the product and its original purchase price.
            line.setQuantity(quantity);
            recalculateTotalAmount(order);

            orderRepository.save(order);
            orderRepository.flush();

            return orderMapper.toOrderLineDto(line);

        } catch (RuntimeException exception) {
            undoQuantityChange( line.getProductId(),quantityDifference );

            throw exception;
        }
    }

    public void deleteOrderLine(Long orderId, Long lineId) {
        OrderEntity order = orderRepository.requireById(orderId);
        requireConfirmedOrder(order);

        OrderLineEntity line = findOrderLine(orderId, lineId);

        if (order.getOrderLineList().size() <= MINIMUM_LINE_COUNT) {
            throw new InvalidOrderException( "Cancel the order instead of deleting its last line" );
        }

        productClient.increaseStock( line.getProductId(),line.getQuantity());

        try {
            // orphanRemoval deletes the line removed from the collection.
            order.getOrderLineList().remove(line);
            recalculateTotalAmount(order);

            orderRepository.save(order);
            orderRepository.flush();

        } catch (RuntimeException exception) {
            reserveStockAfterFailure(line);
            throw exception;
        }
    }

    public Optional<OrderLineDto> getOrderLineById(Long orderId,  Long lineId) {
        return orderLineRepository
                .findByIdAndOrderId(lineId, orderId)
                .map(orderMapper::toOrderLineDto);
    }

    public List<OrderLineDto> getLinesByOrderId(Long orderId) {
        orderRepository.requireById(orderId);

        return orderLineRepository.findByOrderId(orderId)
                .stream()
                .map(orderMapper::toOrderLineDto)
                .toList();
    }

    public List<OrderLineDto> getAllOrderLines() {
        return orderLineRepository.findAll()
                .stream()
                .map(orderMapper::toOrderLineDto)
                .toList();
    }


    /**
     * Prepare a line without saving data or changing stock.
     */
    public OrderLineEntity prepareOrderLine(CreateOrderLineDto dto) {
        if (dto == null || dto.productId() == null) {
            throw new InvalidOrderException( "Product ID is required" );
        }

        validateQuantity(dto.quantity());

        ProductResponse product = productClient.getProduct(dto.productId());

        if (product == null || product.price() == null) {
            throw new InvalidOrderException( "The catalogue did not return a product price" );
        }

        if (product.price().signum() < 0) {
            throw new InvalidOrderException( "The catalogue returned a negative price");
        }

        return new OrderLineEntity(
                dto.productId(),
                dto.quantity(),
                product.price()
        );
    }

    /**
     * Calculate the total using the prices stored in the order.
     */
    public void recalculateTotalAmount(OrderEntity order) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderLineEntity line : order.getOrderLineList()) {
            BigDecimal lineAmount = line.getUnitPrice()
                    .multiply(BigDecimal.valueOf(line.getQuantity()));

            totalAmount = totalAmount.add(lineAmount);
        }

        order.setTotalAmount(totalAmount);
    }

    /**
     * Try to undo a successful stock reservation.
     */
    public void restoreStockAfterFailure(OrderLineEntity line) {
        try {
            productClient.increaseStock(line.getProductId(),line.getQuantity());

        } catch (RuntimeException exception) {
            LOGGER.error("Could not restore stock for product {}. " + "Manual reconciliation is required.",
                    line.getProductId(),
                    exception
            );
        }
    }

    /**
     * Try to undo a successful stock return.
     */
    public void reserveStockAfterFailure(OrderLineEntity line) {
        try {
            productClient.decreaseStock(line.getProductId(), line.getQuantity() );

        } catch (RuntimeException exception) {
            LOGGER.error( "Could not reserve stock for product {}. " + "Manual reconciliation is required.",
                    line.getProductId(),
                    exception
            );
        }
    }


    private OrderLineEntity findOrderLine( Long orderId,Long lineId) {
        return orderLineRepository
                .findByIdAndOrderId(lineId, orderId)
                .orElseThrow(() -> new OrderLineNotFoundException(orderId, lineId) );
    }

    private void validateQuantity(int quantity) {
        if (quantity < MINIMUM_QUANTITY) {
            throw new InvalidOrderException( "Quantity must be at least one" );
        }
    }

    private void requireConfirmedOrder(OrderEntity order) {
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException( "Only confirmed orders can have their lines changed" );
        }
    }

    private void undoQuantityChange(Long productId, int quantityDifference) {
        try {
            if (quantityDifference > NO_QUANTITY_CHANGE) {
                productClient.increaseStock(productId, quantityDifference);
            } else {
                productClient.decreaseStock(productId, -quantityDifference);
            }

        } catch (RuntimeException exception) {
            LOGGER.error( "Could not undo stock change for product {}. " + "Manual reconciliation is required.",
                    productId,
                    exception
            );
        }
    }
}
