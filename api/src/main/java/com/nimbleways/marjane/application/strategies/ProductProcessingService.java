package com.nimbleways.springboilerplate.application.strategies;

import com.nimbleways.springboilerplate.domain.Product;
import com.nimbleways.springboilerplate.domain.ProductType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductProcessingService {

    private final Map<ProductType, ProductStrategy> strategies;

    public ProductProcessingService(List<ProductStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ProductStrategy::getSupportedType, Function.identity()));
    }

    public void processProduct(Product product) {
        ProductType type = ProductType.from(product.getType());
        ProductStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for product type: " + type);
        }
        strategy.process(product);
    }
}
