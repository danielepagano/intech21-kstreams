package com.nice.intech21.streams;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import com.nice.intech.AgentStateOuterClass;
import com.nice.intech21.StreamProcessingContext;
import com.nice.intech21.serde.FactAgentActivityJsonSerde;
import com.nice.intech21.serde.FactAgentSessionJsonSerde;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Log4j2
@AllArgsConstructor
@Component
public class AgentSessionTopology {
    public static final Serde<AgentStateOuterClass.AgentSession> SERDE_FACT_AGENT_SESSION_JSON = new FactAgentSessionJsonSerde();
    public static final Serde<AgentStateOuterClass.AgentActivity> SERDE_FACT_AGENT_ACTIVITY_JSON = new FactAgentActivityJsonSerde();

    private final StreamProcessingContext context;

    void processStream(KStream<String, AgentStateOuterClass.AgentStateChangeEvent> inputStream) {
        // 1. Group events by session key, as that is our table row
        final KGroupedStream<String, AgentStateOuterClass.AgentStateChangeEvent> inputBySessionKey = inputStream
                .groupBy((key, value) -> value.getAgentSessionUUID(),
                        Grouped.with(Serdes.String(), AgentStateStreamProcessor.SERDE_EVT_AGENT_STATE_CHANGE));

        // 2. Update the table with the incoming events
        final KTable<String, AgentStateOuterClass.TableRowAgentSession> agentSessionTable = inputBySessionKey
                .aggregate(AgentStateOuterClass.TableRowAgentSession::getDefaultInstance, this::accumulateTableRow,
                        TopologyHelper.getSessionTableStore(context.getConfig()));

        // 3. Produce any session fact
        agentSessionTable.toStream()
                .flatMapValues(AgentSessionTopology::emitAgentSessionFacts)
                .to(context.getConfig().getOutputTopicsFactAgentSession(), Produced.with(Serdes.String(), SERDE_FACT_AGENT_SESSION_JSON));

        // 4. Produce any activity facts
        agentSessionTable.toStream()
                .flatMap(AgentSessionTopology::emitAgentActivityFacts)
                .to(context.getConfig().getOutputTopicsFactAgentActivity(), Produced.with(Serdes.String(), SERDE_FACT_AGENT_ACTIVITY_JSON));

        // 6. Delete completed table rows
        agentSessionTable.filter((k, v) -> v.getSession().hasEndTimestamp()).mapValues(v -> null);
    }

    AgentStateOuterClass.TableRowAgentSession accumulateTableRow(String key, AgentStateOuterClass.AgentStateChangeEvent event,
                                                                 AgentStateOuterClass.TableRowAgentSession row) {
        final AgentStateOuterClass.TableRowAgentSession.Builder rowBuilder = AgentStateOuterClass.TableRowAgentSession.newBuilder(row);

        // Ingest the event in the table
        rowBuilder.addOrderedEvents(event);

        updateAgentActivitiesFromEvent(event, rowBuilder);
        updateAgentSessionFromEvent(event, rowBuilder);

        return rowBuilder.build();
    }

    static void updateAgentSessionFromEvent(AgentStateOuterClass.AgentStateChangeEvent event, AgentStateOuterClass.TableRowAgentSession.Builder rowBuilder) {
        final AgentStateOuterClass.AgentSession.Builder builder = rowBuilder.getSessionBuilder();

        switch (event.getEventIndicator()) {
            case SESSION_STARTED:
                // If started, init keys
                builder.setAgentSessionUUID(event.getAgentSessionUUID())
                        .setAgentId(event.getAgentId())
                        .setStartTimestamp(event.getEventTimestamp());
                break;
            case SESSION_ENDED:
                // If ended, calculate statistics (assumes activities are completed!)
                final List<AgentStateOuterClass.AgentActivity> activities = rowBuilder.getActivitiesList();
                final Duration duration = Timestamps.between(builder.getStartTimestamp(), event.getEventTimestamp());
                builder.setEndTimestamp(event.getEventTimestamp())
                        .setAgentSessionDurationSeconds(Math.round(Durations.toSecondsAsDouble(duration)))
                        .setAvailableSeconds(TopologyHelper.sumActivityTimeByType(activities,
                                ActivityTimeClass.AVAILABLE))
                        .setWorkingContactsSeconds(TopologyHelper.sumActivityTimeByType(activities,
                                ActivityTimeClass.WORKING_CONTACTS))
                        .setUnavailableSeconds(TopologyHelper.sumActivityTimeByType(activities,
                                ActivityTimeClass.UNAVAILABLE))
                        .setSystemSeconds(TopologyHelper.sumActivityTimeByType(activities,
                                ActivityTimeClass.SYSTEM));
                break;
            default:
                // No-op; handled by activities
                break;
        }
    }

    static void updateAgentActivitiesFromEvent(AgentStateOuterClass.AgentStateChangeEvent event, AgentStateOuterClass.TableRowAgentSession.Builder rowBuilder) {
        // Find any previous activity
        final AgentStateOuterClass.AgentActivity.Builder lastActivity = rowBuilder.getActivitiesCount() < 1 ? null :
                rowBuilder.getActivitiesBuilder(rowBuilder.getActivitiesCount() - 1);

        // We only want to touch activities if:
        //   1. This is the first event, so we create the first activity
        //   2. The agent state is different from the previous activity; i.e. we skip no-op updates
        //   3. It's the end of the session, so we just want to close the last activity
        if (lastActivity == null || lastActivity.getAgentState() != event.getAgentState() ||
                event.getEventIndicator() == AgentStateOuterClass.AgentStateEventIndicator.SESSION_ENDED) {

            // End any previous activity
            if (lastActivity != null) {
                final Duration duration = Timestamps.between(lastActivity.getStartTimestamp(), event.getEventTimestamp());
                lastActivity.setEndTimestamp(event.getEventTimestamp())
                        .setAgentActivityDurationSeconds(Math.round(Durations.toSecondsAsDouble(duration)));
            }

            // Create new activity unless session has ended
            if (event.getEventIndicator() != AgentStateOuterClass.AgentStateEventIndicator.SESSION_ENDED) {
                final AgentStateOuterClass.AgentActivity.Builder newActivity = AgentStateOuterClass.AgentActivity.newBuilder()
                        .setAgentActivityUUID(UUID.randomUUID().toString())
                        .setAgentSessionUUID(event.getAgentSessionUUID())
                        .setAgentActivitySequence(rowBuilder.getActivitiesCount() + 1L)
                        .setAgentId(event.getAgentId())
                        .setAgentState(event.getAgentState())
                        .setStartTimestamp(event.getEventTimestamp());

                rowBuilder.addActivities(newActivity);
            }
        }
    }

    static Iterable<AgentStateOuterClass.AgentSession> emitAgentSessionFacts(String sessionKey, AgentStateOuterClass.TableRowAgentSession row) {
        // Emit session on start and end
        final AgentStateOuterClass.AgentStateChangeEvent lastEvent = row.getOrderedEventsList().get(row.getOrderedEventsCount() - 1);
        final boolean isBoundaryEvent = lastEvent.getEventIndicator() == AgentStateOuterClass.AgentStateEventIndicator.SESSION_STARTED ||
                lastEvent.getEventIndicator() == AgentStateOuterClass.AgentStateEventIndicator.SESSION_ENDED;
        return isBoundaryEvent ? Collections.singletonList(row.getSession()) : Collections.emptyList();
    }

    static Iterable<KeyValue<String, AgentStateOuterClass.AgentActivity>> emitAgentActivityFacts(String sessionKey, AgentStateOuterClass.TableRowAgentSession row) {
        final AgentStateOuterClass.AgentStateChangeEvent lastEvent = row.getOrderedEventsList().get(row.getOrderedEventsCount() - 1);

        // On login and logout, send the latest activity (first one, or finished last one)
        final int activitiesCount = row.getActivitiesCount();
        if (activitiesCount < 1) return Collections.emptyList();
        if (activitiesCount == 1) {
            final AgentStateOuterClass.AgentActivity activity = row.getActivitiesList().get(0);
            return Collections.singletonList(KeyValue.pair(activity.getAgentActivityUUID(), activity));
        }
        if (lastEvent.getEventIndicator() == AgentStateOuterClass.AgentStateEventIndicator.SESSION_ENDED) {
            final AgentStateOuterClass.AgentActivity last = row.getActivitiesList().get(row.getActivitiesCount() - 1);
            return Collections.singletonList(KeyValue.pair(last.getAgentActivityUUID(), last));
        }

        // Return previous completed one and current started one
        final AgentStateOuterClass.AgentActivity last = row.getActivitiesList().get(row.getActivitiesCount() - 1);
        final AgentStateOuterClass.AgentActivity penultimate = row.getActivitiesList().get(row.getActivitiesCount() - 2);
        return ImmutableList.of(
                KeyValue.pair(penultimate.getAgentActivityUUID(), penultimate),
                KeyValue.pair(last.getAgentActivityUUID(), last));
    }
}
