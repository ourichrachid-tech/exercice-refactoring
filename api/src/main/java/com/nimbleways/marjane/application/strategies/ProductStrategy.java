package com.nimbleways.springboilerplate.application.strategies;

import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.ProductType;

public interface ProductStrategy {
    ProductType getSupportedType();
    void process(Product product);
}
