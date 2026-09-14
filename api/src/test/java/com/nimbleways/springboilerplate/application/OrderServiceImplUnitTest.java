package com.nimbleways.springboilerplate.application;

import com.nimbleways.springboilerplate.application.strategies.ProductProcessingService;
import com.nimbleways.springboilerplate.domain.Order;
import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.exposition.dto.ProcessOrderResponse;
import com.nimbleways.springboilerplate.infrastructure.persistence.OrderRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class OrderServiceImplUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductProcessingService productProcessingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("processOrder: processes all items and returns response when order found")
    public void processOrder_success() {
        Product p1 = new Product(1L, 10, 5, "NORMAL", "Item 1", null, null, null);
        Product p2 = new Product(2L, 5, 2, "EXPIRABLE", "Item 2", null, null, null);
        Order order = new Order(100L, Set.of(p1, p2));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        ProcessOrderResponse response = orderService.processOrder(100L);

        assertEquals(100L, response.id());
        verify(productProcessingService, times(1)).processProduct(p1);
        verify(productProcessingService, times(1)).processProduct(p2);
    }

    @Test
    @DisplayName("processOrder: throws OrderNotFoundException when order does not exist")
    public void processOrder_notFound_throwsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.processOrder(999L));
        verifyNoInteractions(productProcessingService);
    }
}
