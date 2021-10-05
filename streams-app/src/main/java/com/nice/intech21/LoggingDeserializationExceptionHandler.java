package com.nice.intech21;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Map;

@Log4j2
public class LoggingDeserializationExceptionHandler implements DeserializationExceptionHandler {
    @Override
    public DeserializationHandlerResponse handle(final ProcessorContext context,
                                                 final ConsumerRecord<byte[], byte[]> record,
                                                 final Exception exception) {

        log.error("Exception caught during Deserialization, " +
                        "taskId: {}, topic: {}, partition: {}, offset: {}",
                context.taskId(), record.topic(), record.partition(), record.offset(),
                exception);

        // Once implemented, we can also alert here

        return DeserializationHandlerResponse.CONTINUE;
    }

    @Override
    public void configure(final Map<String, ?> configs) {
        // ignore for now
    }
}
