package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks 
    private ProductService productService;

    @Test
    @DisplayName("notifyDelay: updates lead time, saves product, and sends delay notification")
    public void notifyDelay_shouldSaveProductAndSendDelayNotification() {
        Product product = new Product(1L, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.notifyDelay(product.getLeadTime(), product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(product.getLeadTime(), product.getName());
    }

    @Test
    @DisplayName("Seasonal: when restock lead time exceeds season end date, notify out of stock and set available to 0")
    public void handleSeasonalProduct_whenRestockExceedsSeasonEndDate_shouldNotifyOutOfStockAndSetAvailableToZero() {
        LocalDate now = LocalDate.now();
        Product product = new Product(2L, 10, 5, "SEASONAL", "Watermelon", null, now.minusDays(10), now.plusDays(5));
        when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendOutOfStockNotification(product.getName());
    }

    @Test
    @DisplayName("Seasonal: when season has not started yet, notify out of stock")
    public void handleSeasonalProduct_whenSeasonHasNotStartedYet_shouldNotifyOutOfStock() {
        LocalDate now = LocalDate.now();
        Product product = new Product(3L, 2, 5, "SEASONAL", "Grapes", null, now.plusDays(10), now.plusDays(30));
        when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);

        assertEquals(5, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendOutOfStockNotification(product.getName());
    }

    @Test
    @DisplayName("Seasonal: when in season and restock fits before season end date, notify delay")
    public void handleSeasonalProduct_whenInSeasonAndRestockWithinSeason_shouldNotifyDelay() {
        LocalDate now = LocalDate.now();
        Product product = new Product(4L, 5, 0, "SEASONAL", "Strawberries", null, now.minusDays(10), now.plusDays(20));
        when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);

        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(5, product.getName());
    }

    @Test
    @DisplayName("Expirable: when available and not expired, decrement available by 1 and save")
    public void handleExpiredProduct_whenAvailableAndNotExpired_shouldDecrementAvailable() {
        Product product = new Product(5L, 5, 3, "EXPIRABLE", "Butter", LocalDate.now().plusDays(10), null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertEquals(2, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Expirable: when expired, notify expiration, set available to 0 and save")
    public void handleExpiredProduct_whenExpired_shouldNotifyExpirationAndSetAvailableToZero() {
        LocalDate expiryDate = LocalDate.now().minusDays(2);
        Product product = new Product(6L, 5, 4, "EXPIRABLE", "Milk", expiryDate, null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendExpirationNotification(product.getName(), expiryDate);
    }

    @Test
    @DisplayName("Expirable: when stock is 0, notify expiration, set available to 0 and save")
    public void handleExpiredProduct_whenAvailableIsZero_shouldNotifyExpirationAndSetAvailableToZero() {
        LocalDate expiryDate = LocalDate.now().plusDays(5);
        Product product = new Product(7L, 5, 0, "EXPIRABLE", "Yogurt", expiryDate, null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendExpirationNotification(product.getName(), expiryDate);
    }
}