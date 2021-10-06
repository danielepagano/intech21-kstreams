package com.nice.intech21.streams;

import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import com.nice.intech.AgentStateOuterClass;
import com.nice.intech21.serde.TableRowAgentSessionSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;

public final class TopologyHelper {
    public static final Serde<AgentStateOuterClass.TableRowAgentSession> SERDE_TABLE_ROW_AGENT_SESSION = new TableRowAgentSessionSerde();

    static Materialized<String, AgentStateOuterClass.TableRowAgentSession, KeyValueStore<Bytes, byte[]>> getSessionTableStore(AgentStateConfig config) {
        final Materialized<String, AgentStateOuterClass.TableRowAgentSession, KeyValueStore<Bytes, byte[]>> sessionTableStore =
                Materialized.as(config.getTableAgentSession());
        sessionTableStore.withKeySerde(Serdes.String());
        sessionTableStore.withValueSerde(SERDE_TABLE_ROW_AGENT_SESSION);
        return sessionTableStore;
    }

    static long sumActivityTimeByType(List<AgentStateOuterClass.AgentActivity> activities, ActivityTimeClass timeClass) {
        final Duration duration = activities.stream()
                .filter(a -> ActivityTimeClass.classify(a.getAgentState()) == timeClass)
                .map(a -> Timestamps.between(a.getStartTimestamp(), a.getEndTimestamp()))
                .reduce(Durations.ZERO, Durations::add);
        return Math.round(Durations.toSecondsAsDouble(duration));
    }
}
