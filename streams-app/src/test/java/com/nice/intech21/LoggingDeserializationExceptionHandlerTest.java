package com.nice.intech21;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler.DeserializationHandlerResponse;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingDeserializationExceptionHandlerTest {

    @Test
    void handle() {
        final ConsumerRecord<byte[], byte[]> record = new ConsumerRecord<>("", 0, 0L, Bytes.EMPTY, Bytes.EMPTY);
        final DeserializationHandlerResponse handlerResponse = new LoggingDeserializationExceptionHandler()
                .handle(Mockito.mock(ProcessorContext.class), record, new RuntimeException());
        assertEquals(DeserializationHandlerResponse.CONTINUE, handlerResponse);
    }
}