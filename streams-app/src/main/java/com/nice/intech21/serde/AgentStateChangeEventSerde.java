package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AgentStateChangeEventSerde implements Serde<AgentStateOuterClass.AgentStateChangeEvent> {
    @Override
    public Serializer<AgentStateOuterClass.AgentStateChangeEvent> serializer() {
        return new AgentStateChangeEventSerializer();
    }

    @Override
    public Deserializer<AgentStateOuterClass.AgentStateChangeEvent> deserializer() {
        return new AgentStateChangeEventDeserializer();
    }
}
