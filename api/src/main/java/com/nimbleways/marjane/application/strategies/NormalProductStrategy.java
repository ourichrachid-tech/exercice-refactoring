package com.nimbleways.springboilerplate.application.strategies;

import com.nimbleways.springboilerplate.application.NotificationService;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.ProductType;
import com.nimbleways.springboilerplate.infrastructure.persistence.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class NormalProductStrategy implements ProductStrategy {

    private static final int ZERO_STOCK = 0;
    private static final int ORDER_UNIT_QUANTITY = 1;

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public NormalProductStrategy(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ProductType getSupportedType() {
        return ProductType.NORMAL;
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > ZERO_STOCK) {
            product.setAvailable(product.getAvailable() - ORDER_UNIT_QUANTITY);
            productRepository.save(product);
        } else {
            int leadTime = product.getLeadTime() != null ? product.getLeadTime() : ZERO_STOCK;
            if (leadTime > ZERO_STOCK) {
                product.setLeadTime(leadTime);
                productRepository.save(product);
                notificationService.sendDelayNotification(leadTime, product.getName());
            }
        }
    }
}
