package com.nimbleways.springboilerplate.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }
}
