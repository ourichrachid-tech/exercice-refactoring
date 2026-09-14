package com.nimbleways.springboilerplate.exposition;

import com.nimbleways.springboilerplate.application.ProductService;
import com.nimbleways.springboilerplate.domain.Order;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.ProductType;
import com.nimbleways.springboilerplate.domain.TimeProvider;
import com.nimbleways.springboilerplate.domain.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.exposition.dto.ProcessOrderResponse;
import com.nimbleways.springboilerplate.infrastructure.persistence.OrderRepository;
import com.nimbleways.springboilerplate.infrastructure.persistence.ProductRepository;

import java.time.LocalDate;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private static final int ZERO_STOCK = 0;
    private static final int ORDER_UNIT_QUANTITY = 1;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private TimeProvider timeProvider = () -> LocalDate.now();

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        log.info("Processing order: {}", order);
        Set<Product> products = order.getItems();
        LocalDate now = timeProvider.getCurrentDate();

        for (Product product : products) {
            ProductType productType = ProductType.from(product.getType());
            switch (productType) {
                case NORMAL -> {
                    if (product.getAvailable() > ZERO_STOCK) {
                        product.setAvailable(product.getAvailable() - ORDER_UNIT_QUANTITY);
                        productRepository.save(product);
                    } else {
                        int leadTime = product.getLeadTime();
                        if (leadTime > ZERO_STOCK) {
                            productService.notifyDelay(leadTime, product);
                        }
                    }
                }
                case SEASONAL -> {
                    if (now.isAfter(product.getSeasonStartDate())
                            && now.isBefore(product.getSeasonEndDate())
                            && product.getAvailable() > ZERO_STOCK) {
                        product.setAvailable(product.getAvailable() - ORDER_UNIT_QUANTITY);
                        productRepository.save(product);
                    } else {
                        productService.handleSeasonalProduct(product);
                    }
                }
                case EXPIRABLE -> {
                    if (product.getAvailable() > ZERO_STOCK && product.getExpiryDate().isAfter(now)) {
                        product.setAvailable(product.getAvailable() - ORDER_UNIT_QUANTITY);
                        productRepository.save(product);
                    } else {
                        productService.handleExpiredProduct(product);
                    }
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
