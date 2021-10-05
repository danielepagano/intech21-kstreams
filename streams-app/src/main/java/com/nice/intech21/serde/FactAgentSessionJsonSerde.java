package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class FactAgentSessionJsonSerde implements Serde<AgentStateOuterClass.AgentSession> {
    @Override
    public Serializer<AgentStateOuterClass.AgentSession> serializer() {
        return new FactAgentSessionJsonSerializer();
    }

    @Override
    public Deserializer<AgentStateOuterClass.AgentSession> deserializer() {
        return new FactAgentSessionJsonDeserializer();
    }
}
