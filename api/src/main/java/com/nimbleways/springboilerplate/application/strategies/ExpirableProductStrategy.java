package com.nimbleways.springboilerplate.application.strategies;

import com.nimbleways.springboilerplate.application.NotificationService;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.ProductType;
import com.nimbleways.springboilerplate.domain.TimeProvider;
import com.nimbleways.springboilerplate.infrastructure.persistence.ProductRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ExpirableProductStrategy implements ProductStrategy {

    private static final int ZERO_STOCK = 0;
    private static final int ORDER_UNIT_QUANTITY = 1;

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final TimeProvider timeProvider;

    public ExpirableProductStrategy(ProductRepository productRepository,
                                    NotificationService notificationService,
                                    TimeProvider timeProvider) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.timeProvider = timeProvider;
    }

    @Override
    public ProductType getSupportedType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public void process(Product product) {
        LocalDate now = timeProvider.getCurrentDate();
        int available = product.getAvailable() != null ? product.getAvailable() : ZERO_STOCK;

        if (available > ZERO_STOCK && product.getExpiryDate() != null && product.getExpiryDate().isAfter(now)) {
            product.setAvailable(available - ORDER_UNIT_QUANTITY);
            productRepository.save(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(ZERO_STOCK);
            productRepository.save(product);
        }
    }
}
