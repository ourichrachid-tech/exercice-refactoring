package com.nimbleways.springboilerplate.exposition;

import com.nimbleways.springboilerplate.domain.Order;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.infrastructure.persistence.OrderRepository;
import com.nimbleways.springboilerplate.infrastructure.persistence.ProductRepository;
import com.nimbleways.springboilerplate.application.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class OrderControllerUnitTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("processOrder: decrements NORMAL product when available > 0")
    public void processOrder_normalProductAvailable_decrementsStock() {
        Product product = new Product(1L, 10, 5, "NORMAL", "USB Cable", null, null, null);
        Order order = new Order(100L, Set.of(product));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderController.processOrder(100L);

        assertEquals(4, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("processOrder: delegates NORMAL product to notifyDelay when available <= 0 and leadTime > 0")
    public void processOrder_normalProductOutOfStock_notifiesDelay() {
        Product product = new Product(1L, 10, 0, "NORMAL", "USB Cable", null, null, null);
        Order order = new Order(100L, Set.of(product));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderController.processOrder(100L);

        verify(productService, times(1)).notifyDelay(10, product);
    }

    @Test
    @DisplayName("processOrder: decrements SEASONAL product when in season and available > 0")
    public void processOrder_seasonalProductInSeasonAvailable_decrementsStock() {
        LocalDate now = LocalDate.now();
        Product product = new Product(2L, 5, 8, "SEASONAL", "Watermelon", null, now.minusDays(5), now.plusDays(10));
        Order order = new Order(101L, Set.of(product));
        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));

        orderController.processOrder(101L);

        assertEquals(7, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("processOrder: delegates SEASONAL product to handleSeasonalProduct when out of season or stock 0")
    public void processOrder_seasonalProductOutOfSeason_delegatesToService() {
        LocalDate now = LocalDate.now();
        Product product = new Product(2L, 5, 0, "SEASONAL", "Watermelon", null, now.minusDays(5), now.plusDays(10));
        Order order = new Order(101L, Set.of(product));
        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));

        orderController.processOrder(101L);

        verify(productService, times(1)).handleSeasonalProduct(product);
    }

    @Test
    @DisplayName("processOrder: decrements EXPIRABLE product when unexpired and available > 0")
    public void processOrder_expirableProductValid_decrementsStock() {
        Product product = new Product(3L, 5, 4, "EXPIRABLE", "Butter", LocalDate.now().plusDays(10), null, null);
        Order order = new Order(102L, Set.of(product));
        when(orderRepository.findById(102L)).thenReturn(Optional.of(order));

        orderController.processOrder(102L);

        assertEquals(3, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("processOrder: delegates EXPIRABLE product to handleExpiredProduct when expired or stock 0")
    public void processOrder_expirableProductExpired_delegatesToService() {
        Product product = new Product(3L, 5, 0, "EXPIRABLE", "Butter", LocalDate.now().minusDays(1), null, null);
        Order order = new Order(102L, Set.of(product));
        when(orderRepository.findById(102L)).thenReturn(Optional.of(order));

        orderController.processOrder(102L);

        verify(productService, times(1)).handleExpiredProduct(product);
    }
}
