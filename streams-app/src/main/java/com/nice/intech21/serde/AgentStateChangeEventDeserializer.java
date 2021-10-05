package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Deserializer;

@Log4j2
public class AgentStateChangeEventDeserializer implements Deserializer<AgentStateOuterClass.AgentStateChangeEvent> {

    @Override
    @SneakyThrows
    public AgentStateOuterClass.AgentStateChangeEvent deserialize(String topic, byte[] data) {
        return AgentStateOuterClass.AgentStateChangeEvent.parseFrom(data);
    }
}
