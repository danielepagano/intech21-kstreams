package com.nice.intech21;

import com.google.protobuf.Timestamp;
import com.nice.intech.AgentStateOuterClass;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.nice.intech21.AgentStateStreamProcessor.SERDE_EVT_AGENT_STATE_CHANGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TopologyTestDriverSmokeTest {
    private static final long BASE_SEC = 1234567890L; // About 2019-02-13
    private static final long FIRST_SEQ = 101L;
    private static final String SESSION_FACT_UUID = UUID.randomUUID().toString();
    private static final Serde<Integer> SERDE_INT = new Serdes.IntegerSerde();
    private static final Serde<String> SERDE_STRING = new Serdes.StringSerde();
    private static final int AGENT_ID = 123;

    private TopologyTestDriver testDriver;
    private TestInputTopic<Integer, AgentStateOuterClass.AgentStateChangeEvent> inputTopic;
    private TestOutputTopic<String, AgentStateOuterClass.AgentSession> outputTopicFactAgentSession;
    private TestOutputTopic<String, AgentStateOuterClass.AgentActivity> outputTopicFactAgentActivity;
    private TestOutputTopic<String, AgentStateOuterClass.TableRowAgentSession> tableAgentSession;

    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "intech21-streams-smoke-test");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SERDE_EVT_AGENT_STATE_CHANGE.getClass().getName());
        return new KafkaStreamsConfiguration(props);
    }

    @BeforeEach
    public void setup() {
        final AgentStateConfig config = new AgentStateConfig();
        final StreamProcessingContext context = new StreamProcessingContext(Clock::systemUTC, config);
        final AgentStateStreamProcessor processor = new AgentStateStreamProcessor(context, new AgentSessionTopology(context));

        final StreamsBuilder builder = new StreamsBuilder();
        processor.processKStream(builder);
        testDriver = new TopologyTestDriver(builder.build(), kStreamsConfigs().asProperties());

        // setup test topics (input, tables, and output)
        inputTopic = testDriver.createInputTopic(config.getInputTopic(),
                SERDE_INT.serializer(), SERDE_EVT_AGENT_STATE_CHANGE.serializer());

        tableAgentSession = testDriver.createOutputTopic(config.getTableAgentSession(),
                SERDE_STRING.deserializer(), AgentSessionTopology.SERDE_TABLE_ROW_AGENT_SESSION.deserializer());

        outputTopicFactAgentSession = testDriver.createOutputTopic(config.getOutputTopicsFactAgentSession(),
                SERDE_STRING.deserializer(), AgentSessionTopology.SERDE_FACT_AGENT_SESSION_JSON.deserializer());
        outputTopicFactAgentActivity = testDriver.createOutputTopic(config.getOutputTopicsFactAgentActivity(),
                SERDE_STRING.deserializer(), AgentSessionTopology.SERDE_FACT_AGENT_ACTIVITY_JSON.deserializer());
    }

    @AfterEach
    public void tearDown() {
        testDriver.close();
    }

    @Test
    void topologySmokeTest() {
        int mainSeq = 0;

        // -> Agent logs in
        final AgentStateOuterClass.AgentStateChangeEvent loginEvent = getEventBuilder(++mainSeq,
                AgentStateOuterClass.AgentStateEventIndicator.SESSION_STARTED, AgentStateOuterClass.AgentState.LOGGED_IN)
                .build();
        inputTopic.pipeInput(loginEvent);

        // <- We should have a started session fact and a started activity fact
        final KeyValue<String, AgentStateOuterClass.AgentSession> sessionStarted = outputTopicFactAgentSession.readKeyValue();
        assertThat(sessionStarted.key, equalTo(SESSION_FACT_UUID));
        assertThat(sessionStarted.value.getAgentSessionUUID(), equalTo(SESSION_FACT_UUID));
        assertThat(sessionStarted.value.getAgentId(), equalTo(loginEvent.getAgentId()));
        assertThat(sessionStarted.value.getStartTimestamp(), equalTo(loginEvent.getEventTimestamp()));

        final KeyValue<String, AgentStateOuterClass.AgentActivity> activityLoginStarted = outputTopicFactAgentActivity.readKeyValue();
        assertThat(activityLoginStarted.key, equalTo(SESSION_FACT_UUID));
        assertThat(activityLoginStarted.value.getAgentSessionUUID(), equalTo(SESSION_FACT_UUID));
        assertThat(activityLoginStarted.value.getAgentId(), equalTo(loginEvent.getAgentId()));
        assertThat(activityLoginStarted.value.getStartTimestamp(), equalTo(loginEvent.getEventTimestamp()));
        assertThat(activityLoginStarted.value.getAgentState(), equalTo(loginEvent.getAgentState()));

        // -> Agent goes available
        final AgentStateOuterClass.AgentStateChangeEvent availableEvent = getEventBuilder(++mainSeq, AgentStateOuterClass.AgentStateEventIndicator.STATE_CHANGE,
                AgentStateOuterClass.AgentState.AVAILABLE).build();
        inputTopic.pipeInput(availableEvent);

        // <- We should have a completed logged in activity and a started available activity
        final AgentStateOuterClass.AgentActivity activityLoginCompleted = outputTopicFactAgentActivity.readValue();
        assertThat(activityLoginCompleted.getStartTimestamp(), equalTo(loginEvent.getEventTimestamp()));
        assertThat(activityLoginCompleted.getAgentState(), equalTo(loginEvent.getAgentState()));
        assertThat(activityLoginCompleted.getEndTimestamp(), equalTo(availableEvent.getEventTimestamp()));

        final AgentStateOuterClass.AgentActivity activityAvailableStarted = outputTopicFactAgentActivity.readValue();
        assertThat(activityAvailableStarted.getStartTimestamp(), equalTo(availableEvent.getEventTimestamp()));
        assertThat(activityAvailableStarted.getAgentState(), equalTo(availableEvent.getAgentState()));

        // -> Agent goes unavailable
        final AgentStateOuterClass.AgentStateChangeEvent unavailableEvent = getEventBuilder(++mainSeq, AgentStateOuterClass.AgentStateEventIndicator.STATE_CHANGE,
                AgentStateOuterClass.AgentState.UNAVAILABLE).build();
        inputTopic.pipeInput(unavailableEvent);

        final AgentStateOuterClass.AgentActivity activityAvailableCompleted = outputTopicFactAgentActivity.readValue();
        assertThat(activityAvailableCompleted.getStartTimestamp(), equalTo(availableEvent.getEventTimestamp()));
        assertThat(activityAvailableCompleted.getAgentState(), equalTo(availableEvent.getAgentState()));
        assertThat(activityAvailableCompleted.getEndTimestamp(), equalTo(unavailableEvent.getEventTimestamp()));

        final AgentStateOuterClass.AgentActivity activityUnavailableStarted = outputTopicFactAgentActivity.readValue();
        assertThat(activityUnavailableStarted.getStartTimestamp(), equalTo(unavailableEvent.getEventTimestamp()));
        assertThat(activityUnavailableStarted.getAgentState(), equalTo(unavailableEvent.getAgentState()));

        // -> Agent logs out
        final AgentStateOuterClass.AgentStateChangeEvent logoutEvent = getEventBuilder(++mainSeq, AgentStateOuterClass.AgentStateEventIndicator.SESSION_ENDED,
                AgentStateOuterClass.AgentState.LOGGED_OUT).build();
        inputTopic.pipeInput(logoutEvent);

        // <- We should have a completed session and completed last activity
        final KeyValue<String, AgentStateOuterClass.AgentSession> sessionEnded = outputTopicFactAgentSession.readKeyValue();
        assertThat(sessionEnded.key, equalTo(SESSION_FACT_UUID));
        assertThat(sessionEnded.value.getAgentSessionUUID(), equalTo(SESSION_FACT_UUID));
        assertThat(sessionEnded.value.getAgentId(), equalTo(loginEvent.getAgentId()));
        assertThat(sessionEnded.value.getStartTimestamp(), equalTo(loginEvent.getEventTimestamp()));
        assertThat(sessionEnded.value.getEndTimestamp(), equalTo(logoutEvent.getEventTimestamp()));
        assertThat(sessionEnded.value.getAgentSessionDurationSeconds(), equalTo(3L)); // all events
        assertThat(sessionEnded.value.getAvailableSeconds(), equalTo(1L));
        assertThat(sessionEnded.value.getUnavailableSeconds(), equalTo(1L));
        assertThat(sessionEnded.value.getWorkingContactsSeconds(), equalTo(0L));
        assertThat(sessionEnded.value.getSystemSeconds(), equalTo(1L)); // logged in, before available

        final AgentStateOuterClass.AgentActivity activityUnavailableCompleted = outputTopicFactAgentActivity.readValue();
        assertThat(activityUnavailableCompleted.getStartTimestamp(), equalTo(unavailableEvent.getEventTimestamp()));
        assertThat(activityUnavailableCompleted.getAgentState(), equalTo(unavailableEvent.getAgentState()));
        assertThat(activityUnavailableCompleted.getEndTimestamp(), equalTo(logoutEvent.getEventTimestamp()));
    }

    @NotNull
    static AgentStateOuterClass.AgentStateChangeEvent.Builder getEventBuilder(int eventSeq,
                                                                              AgentStateOuterClass.AgentStateEventIndicator indicator,
                                                                              AgentStateOuterClass.AgentState agentState) {
        return AgentStateOuterClass.AgentStateChangeEvent.newBuilder()
                .setEventTimestamp(Timestamp.newBuilder().setSeconds(BASE_SEC + eventSeq))
                .setEventIndicator(indicator)
                .setAgentId(AGENT_ID)
                .setAgentSessionUUID(TopologyTestDriverSmokeTest.SESSION_FACT_UUID)
                .setEventSequence(FIRST_SEQ + eventSeq)
                .setAgentState(agentState);
    }
}