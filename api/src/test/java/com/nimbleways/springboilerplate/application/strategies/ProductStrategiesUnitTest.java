package com.nimbleways.springboilerplate.application.strategies;

import com.nimbleways.springboilerplate.application.NotificationService;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.ProductType;
import com.nimbleways.springboilerplate.domain.TimeProvider;
import com.nimbleways.springboilerplate.infrastructure.persistence.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductStrategiesUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TimeProvider timeProvider;

    @Test
    @DisplayName("NORMAL: available > 0 decrements stock and saves")
    public void normal_available_decrementsStock() {
        NormalProductStrategy strategy = new NormalProductStrategy(productRepository, notificationService);
        Product product = new Product(1L, 10, 5, "NORMAL", "USB Cable", null, null, null);

        strategy.process(product);

        assertEquals(4, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("NORMAL: available <= 0 with leadTime > 0 notifies delay")
    public void normal_outOfStock_notifiesDelay() {
        NormalProductStrategy strategy = new NormalProductStrategy(productRepository, notificationService);
        Product product = new Product(1L, 10, 0, "NORMAL", "USB Cable", null, null, null);

        strategy.process(product);

        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(10, "USB Cable");
    }

    @Test
    @DisplayName("SEASONAL: in season with stock decrements stock and saves")
    public void seasonal_inSeason_decrementsStock() {
        LocalDate now = LocalDate.of(2026, 6, 1);
        when(timeProvider.getCurrentDate()).thenReturn(now);
        SeasonalProductStrategy strategy = new SeasonalProductStrategy(productRepository, notificationService, timeProvider);
        Product product = new Product(2L, 5, 8, "SEASONAL", "Watermelon", null, now.minusDays(5), now.plusDays(10));

        strategy.process(product);

        assertEquals(7, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("SEASONAL: restock exceeds season end date notifies out of stock")
    public void seasonal_restockExceedsSeason_notifiesOutOfStock() {
        LocalDate now = LocalDate.of(2026, 6, 1);
        when(timeProvider.getCurrentDate()).thenReturn(now);
        SeasonalProductStrategy strategy = new SeasonalProductStrategy(productRepository, notificationService, timeProvider);
        Product product = new Product(2L, 15, 0, "SEASONAL", "Watermelon", null, now.minusDays(5), now.plusDays(10));

        strategy.process(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendOutOfStockNotification("Watermelon");
    }

    @Test
    @DisplayName("EXPIRABLE: unexpired with stock decrements stock")
    public void expirable_valid_decrementsStock() {
        LocalDate now = LocalDate.of(2026, 6, 1);
        when(timeProvider.getCurrentDate()).thenReturn(now);
        ExpirableProductStrategy strategy = new ExpirableProductStrategy(productRepository, notificationService, timeProvider);
        Product product = new Product(3L, 5, 4, "EXPIRABLE", "Butter", now.plusDays(10), null, null);

        strategy.process(product);

        assertEquals(3, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("EXPIRABLE: expired notifies expiration and sets stock to 0")
    public void expirable_expired_notifiesExpiration() {
        LocalDate now = LocalDate.of(2026, 6, 1);
        LocalDate expiryDate = now.minusDays(1);
        when(timeProvider.getCurrentDate()).thenReturn(now);
        ExpirableProductStrategy strategy = new ExpirableProductStrategy(productRepository, notificationService, timeProvider);
        Product product = new Product(3L, 5, 2, "EXPIRABLE", "Butter", expiryDate, null, null);

        strategy.process(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendExpirationNotification("Butter", expiryDate);
    }

    @Test
    @DisplayName("ProductProcessingService: routes product to correct strategy")
    public void processingService_routesToStrategy() {
        NormalProductStrategy normalStrategy = mock(NormalProductStrategy.class);
        when(normalStrategy.getSupportedType()).thenReturn(ProductType.NORMAL);

        ProductProcessingService service = new ProductProcessingService(List.of(normalStrategy));
        Product product = new Product(1L, 10, 5, "NORMAL", "USB Cable", null, null, null);

        service.processProduct(product);

        verify(normalStrategy, times(1)).process(product);
    }
}
