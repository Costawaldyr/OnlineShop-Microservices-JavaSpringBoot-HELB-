package com.onlineshop.order.repository;

import com.onlineshop.order.entity.ClientEntity;
import com.onlineshop.order.exception.ClientNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    public default ClientEntity requireById(Long id) {
        return findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }
}