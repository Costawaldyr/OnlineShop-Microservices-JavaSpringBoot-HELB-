package com.onlineshop.order.repository;

import com.onlineshop.order.entity.OrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderLineRepository  extends JpaRepository<OrderLineEntity, Long> {

    List<OrderLineEntity> findByOrderId(Long orderId);
    Optional<OrderLineEntity> findByIdAndOrderId(Long id, Long orderId);
}