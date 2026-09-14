package com.nimbleways.springboilerplate.application;

import com.nimbleways.springboilerplate.application.strategies.ProductProcessingService;
import com.nimbleways.springboilerplate.domain.Order;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.exposition.dto.ProcessOrderResponse;
import com.nimbleways.springboilerplate.infrastructure.persistence.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductProcessingService productProcessingService;

    public OrderServiceImpl(OrderRepository orderRepository, ProductProcessingService productProcessingService) {
        this.orderRepository = orderRepository;
        this.productProcessingService = productProcessingService;
    }

    @Override
    @Transactional
    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        log.info("Processing order: {}", order);

        Set<Product> products = order.getItems();
        if (products != null) {
            for (Product product : products) {
                productProcessingService.processProduct(product);
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
