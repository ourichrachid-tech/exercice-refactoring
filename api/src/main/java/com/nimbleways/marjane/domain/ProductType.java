package com.nimbleways.springboilerplate.domain;

import java.util.Arrays;

public enum ProductType {
    NORMAL,
    SEASONAL,
    EXPIRABLE;

    public static ProductType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product type: " + value));
    }
}
