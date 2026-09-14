package com.nimbleways.springboilerplate.application.strategies;

import com.nimbleways.springboilerplate.application.NotificationService;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.ProductType;
import com.nimbleways.springboilerplate.domain.TimeProvider;
import com.nimbleways.springboilerplate.infrastructure.persistence.ProductRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SeasonalProductStrategy implements ProductStrategy {

    private static final int ZERO_STOCK = 0;
    private static final int ORDER_UNIT_QUANTITY = 1;

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final TimeProvider timeProvider;

    public SeasonalProductStrategy(ProductRepository productRepository,
                                   NotificationService notificationService,
                                   TimeProvider timeProvider) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.timeProvider = timeProvider;
    }

    @Override
    public ProductType getSupportedType() {
        return ProductType.SEASONAL;
    }

    @Override
    public void process(Product product) {
        LocalDate now = timeProvider.getCurrentDate();
        int available = product.getAvailable() != null ? product.getAvailable() : ZERO_STOCK;
        int leadTime = product.getLeadTime() != null ? product.getLeadTime() : ZERO_STOCK;

        if (isInSeason(product, now) && available > ZERO_STOCK) {
            product.setAvailable(available - ORDER_UNIT_QUANTITY);
            productRepository.save(product);
        } else if (now.plusDays(leadTime).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(ZERO_STOCK);
            productRepository.save(product);
        } else if (product.getSeasonStartDate() != null && product.getSeasonStartDate().isAfter(now)) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            product.setLeadTime(leadTime);
            productRepository.save(product);
            notificationService.sendDelayNotification(leadTime, product.getName());
        }
    }

    private boolean isInSeason(Product product, LocalDate date) {
        return product.getSeasonStartDate() != null
                && product.getSeasonEndDate() != null
                && date.isAfter(product.getSeasonStartDate())
                && date.isBefore(product.getSeasonEndDate());
    }
}
