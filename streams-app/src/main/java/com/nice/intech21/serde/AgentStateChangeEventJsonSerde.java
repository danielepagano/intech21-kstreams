package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AgentStateChangeEventJsonSerde implements Serde<AgentStateOuterClass.AgentStateChangeEvent> {
    @Override
    public Serializer<AgentStateOuterClass.AgentStateChangeEvent> serializer() {
        return new AgentStateChangeEventJsonSerializer();
    }

    @Override
    public Deserializer<AgentStateOuterClass.AgentStateChangeEvent> deserializer() {
        return new AgentStateChangeEventJsonDeserializer();
    }
}
