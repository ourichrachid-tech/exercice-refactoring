package com.nimbleways.springboilerplate.exposition;

import com.nimbleways.springboilerplate.application.OrderService;
import com.nimbleways.springboilerplate.exposition.dto.ProcessOrderResponse;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class OrderControllerUnitTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("processOrder: delegates to OrderService and returns response")
    public void processOrder_delegatesToOrderService() {
        Long orderId = 100L;
        ProcessOrderResponse expected = new ProcessOrderResponse(orderId);
        when(orderService.processOrder(orderId)).thenReturn(expected);

        ProcessOrderResponse response = orderController.processOrder(orderId);

        assertEquals(expected, response);
        verify(orderService, times(1)).processOrder(orderId);
    }
}
