package com.nice.intech21;

import com.google.protobuf.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Supplier;

@Log4j2
@Service
@EnableKafkaStreams
@AllArgsConstructor
@Component
public class StreamProcessingContext {
    @Getter
    private final Supplier<Clock> clockSupplier;

    @Getter
    private final AgentStateConfig config;

    public Timestamp getCurrentTimestamp() {
        final Instant now = clockSupplier.get().instant();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }
}
