package com.nimbleways.springboilerplate.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nimbleways.springboilerplate.domain.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findById(Long orderId);
}
