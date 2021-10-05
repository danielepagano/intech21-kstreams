package com.nice.intech21.serde;

import com.google.protobuf.util.JsonFormat;
import com.nice.intech.AgentStateOuterClass;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Serializer;

public class FactAgentActivityJsonSerializer implements Serializer<AgentStateOuterClass.AgentActivity> {
    @SneakyThrows
    @Override
    public byte[] serialize(String topic, AgentStateOuterClass.AgentActivity data) {
        return JsonFormat.printer().print(data).getBytes();
    }
}
