package com.nimbleways.springboilerplate.infrastructure.clock;

import com.nimbleways.springboilerplate.domain.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }
}
