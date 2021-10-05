package com.nice.intech21;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Primary
@EnableConfigurationProperties(AgentStateConfig.class)
@ConfigurationProperties(prefix = "app.agent-state")
@Getter
@Setter
public class AgentStateConfig {
    private String topicPrefix = "";
    private String inputTopic = "agent_reporting";

    private String tableAgentSession = "table_agent_session";

    private String outputTopicsFactAgentSession = "agent_session";
    private String outputTopicsFactAgentActivity = "agent_activity";
}
