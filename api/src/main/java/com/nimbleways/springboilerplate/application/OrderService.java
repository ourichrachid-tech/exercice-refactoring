package com.nimbleways.springboilerplate.application;

import com.nimbleways.springboilerplate.exposition.dto.ProcessOrderResponse;

public interface OrderService {
    ProcessOrderResponse processOrder(Long orderId);
}
