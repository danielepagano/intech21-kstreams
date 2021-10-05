package com.nice.intech21.serde;

import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Serializer;

public class TableRowAgentSessionSerializer implements Serializer<AgentStateOuterClass.TableRowAgentSession> {
    @Override
    public byte[] serialize(String topic, AgentStateOuterClass.TableRowAgentSession data) {
        return data.toByteArray();
    }
}
