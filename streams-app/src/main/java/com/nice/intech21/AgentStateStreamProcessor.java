package com.nice.intech21;

import com.nice.intech.AgentStateOuterClass;
import com.nice.intech21.serde.AgentStateChangeEventJsonSerde;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@EnableKafkaStreams
@AllArgsConstructor
public class AgentStateStreamProcessor {
    public static final Serde<AgentStateOuterClass.AgentStateChangeEvent> SERDE_EVT_AGENT_STATE_CHANGE = new AgentStateChangeEventJsonSerde();
    private static final Logger logger = LoggerFactory.getLogger(AgentStateStreamProcessor.class);
    @Getter
    private final StreamProcessingContext processingContext;
    private final AgentSessionTopology agentSessionTopology;

    @Bean
    public KStream<String, AgentStateOuterClass.AgentStateChangeEvent> processKStream(StreamsBuilder kStreamBuilder) {
        final KStream<String, AgentStateOuterClass.AgentStateChangeEvent> eventStream = kStreamBuilder.stream(processingContext.getConfig().getInputTopic());
        logger.info("Input Topic {}", processingContext.getConfig().getInputTopic());

        agentSessionTopology.processStream(eventStream);
        return eventStream;
    }
}
