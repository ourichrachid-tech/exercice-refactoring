package com.nimbleways.springboilerplate.exposition;

import com.nimbleways.springboilerplate.application.ProductService;
import com.nimbleways.springboilerplate.exposition.dto.ProcessOrderResponse;
import com.nimbleways.springboilerplate.domain.Order;
import com.nimbleways.springboilerplate.domain.Product;
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

    private static final String TYPE_NORMAL = "NORMAL";
    private static final String TYPE_SEASONAL = "SEASONAL";
    private static final String TYPE_EXPIRABLE = "EXPIRABLE";

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        log.info("Processing order: {}", order);
        Set<Product> products = order.getItems();

        for (Product product : products) {
            if (TYPE_NORMAL.equals(product.getType())) {
                if (product.getAvailable() > ZERO_STOCK) {
                    product.setAvailable(product.getAvailable() - ORDER_UNIT_QUANTITY);
                    productRepository.save(product);
                } else {
                    int leadTime = product.getLeadTime();
                    if (leadTime > ZERO_STOCK) {
                        productService.notifyDelay(leadTime, product);
                    }
                }
            } else if (TYPE_SEASONAL.equals(product.getType())) {
                if (LocalDate.now().isAfter(product.getSeasonStartDate())
                        && LocalDate.now().isBefore(product.getSeasonEndDate())
                        && product.getAvailable() > ZERO_STOCK) {
                    product.setAvailable(product.getAvailable() - ORDER_UNIT_QUANTITY);
                    productRepository.save(product);
                } else {
                    productService.handleSeasonalProduct(product);
                }
            } else if (TYPE_EXPIRABLE.equals(product.getType())) {
                if (product.getAvailable() > ZERO_STOCK && product.getExpiryDate().isAfter(LocalDate.now())) {
                    product.setAvailable(product.getAvailable() - ORDER_UNIT_QUANTITY);
                    productRepository.save(product);
                } else {
                    productService.handleExpiredProduct(product);
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
