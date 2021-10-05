package com.nice.intech21.serde;

import com.google.protobuf.util.JsonFormat;
import com.nice.intech.AgentStateOuterClass;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;

public class FactAgentSessionJsonDeserializer implements Deserializer<AgentStateOuterClass.AgentSession> {
    @SneakyThrows
    @Override
    public AgentStateOuterClass.AgentSession deserialize(String topic, byte[] data) {
        final AgentStateOuterClass.AgentSession.Builder builder = AgentStateOuterClass.AgentSession.newBuilder();
        final String json = new String(data);
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }
}
