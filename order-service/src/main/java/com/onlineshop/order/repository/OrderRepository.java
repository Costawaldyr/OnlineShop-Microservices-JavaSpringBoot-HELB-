package com.onlineshop.order.repository;


import com.onlineshop.order.entity.OrderEntity;
import com.onlineshop.order.exception.OrderNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByClientId(Long clientId);

    public default OrderEntity requireById(Long id) {
        return findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
