package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Serializer;

public class AgentStateChangeEventSerializer implements Serializer<AgentStateOuterClass.AgentStateChangeEvent> {
    @Override
    public byte[] serialize(String topic, AgentStateOuterClass.AgentStateChangeEvent data) {
        return data.toByteArray();
    }
}
