package com.nimbleways.springboilerplate.domain;

import java.time.LocalDate;

@FunctionalInterface
public interface TimeProvider {
    LocalDate getCurrentDate();
}
