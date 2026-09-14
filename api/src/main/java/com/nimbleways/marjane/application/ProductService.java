package com.nimbleways.springboilerplate.application;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.TimeProvider;
import com.nimbleways.springboilerplate.infrastructure.persistence.ProductRepository;

@Service
public class ProductService {

    private static final int ZERO_STOCK = 0;
    private static final int DECREMENT_UNIT = 1;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired(required = false)
    private TimeProvider timeProvider = () -> LocalDate.now();

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void handleSeasonalProduct(Product product) {
        LocalDate now = timeProvider.getCurrentDate();
        if (now.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(ZERO_STOCK);
            productRepository.save(product);
        } else if (product.getSeasonStartDate().isAfter(now)) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    public void handleExpiredProduct(Product product) {
        LocalDate now = timeProvider.getCurrentDate();
        if (product.getAvailable() > ZERO_STOCK && product.getExpiryDate().isAfter(now)) {
            product.setAvailable(product.getAvailable() - DECREMENT_UNIT);
            productRepository.save(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(ZERO_STOCK);
            productRepository.save(product);
        }
    }
}
