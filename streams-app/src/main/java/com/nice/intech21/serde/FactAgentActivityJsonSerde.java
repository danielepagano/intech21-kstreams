package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class FactAgentActivityJsonSerde implements Serde<AgentStateOuterClass.AgentActivity> {
    @Override
    public Serializer<AgentStateOuterClass.AgentActivity> serializer() {
        return new FactAgentActivityJsonSerializer();
    }

    @Override
    public Deserializer<AgentStateOuterClass.AgentActivity> deserializer() {
        return new FactAgentActivityJsonDeserializer();
    }
}
