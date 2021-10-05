package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class TableRowAgentSessionSerde implements Serde<AgentStateOuterClass.TableRowAgentSession> {
    @Override
    public Serializer<AgentStateOuterClass.TableRowAgentSession> serializer() {
        return new TableRowAgentSessionSerializer();
    }

    @Override
    public Deserializer<AgentStateOuterClass.TableRowAgentSession> deserializer() {
        return new TableRowAgentSessionDeserializer();
    }
}
