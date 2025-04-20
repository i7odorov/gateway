package com.ivantodorov.gateway.domain.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.rabbitmq.retry")
@Data
public class RabbitRetryProperties {

    private long initialInterval;
    private double multiplier;
    private long maxInterval;
    private int maxAttempts;
}
