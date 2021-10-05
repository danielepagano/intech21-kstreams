package com.nice.intech21;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StreamsAppConfig.class)
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class StreamsAppConfig {
    // Add any global config here
}
