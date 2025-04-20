package com.ivantodorov.gateway.domain.model.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class RabbitMQProperties {

    private String exchange;
    private String host;
    private int port;
    private String username;
    private String password;
}