package com.nice.intech21.serde;

import com.google.protobuf.util.JsonFormat;
import com.nice.intech.AgentStateOuterClass;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;

public class FactAgentActivityJsonDeserializer implements Deserializer<AgentStateOuterClass.AgentActivity> {
    @SneakyThrows
    @Override
    public AgentStateOuterClass.AgentActivity deserialize(String topic, byte[] data) {
        final AgentStateOuterClass.AgentActivity.Builder builder = AgentStateOuterClass.AgentActivity.newBuilder();
        final String json = new String(data);
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }
}
