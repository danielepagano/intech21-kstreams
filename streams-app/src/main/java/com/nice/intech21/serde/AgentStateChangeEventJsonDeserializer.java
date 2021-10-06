package com.nice.intech21.serde;

import com.google.protobuf.util.JsonFormat;
import com.nice.intech.AgentStateOuterClass;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Deserializer;

@Log4j2
public class AgentStateChangeEventJsonDeserializer implements Deserializer<AgentStateOuterClass.AgentStateChangeEvent> {

    @Override
    @SneakyThrows
    public AgentStateOuterClass.AgentStateChangeEvent deserialize(String topic, byte[] data) {
        final AgentStateOuterClass.AgentStateChangeEvent.Builder builder = AgentStateOuterClass.AgentStateChangeEvent.newBuilder();
        final String json = new String(data);
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }
}
