package com.nice.intech21;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.util.function.Supplier;

@Log4j2
@SpringBootApplication
public class SampleStreamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleStreamsApplication.class, args);
    }

    @Bean
    Supplier<Clock> clockSupplier() {
        return Clock::systemUTC;
    }
}
