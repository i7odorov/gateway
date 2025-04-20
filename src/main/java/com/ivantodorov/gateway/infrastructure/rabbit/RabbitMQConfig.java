package com.ivantodorov.gateway.infrastructure.rabbit;

import com.ivantodorov.gateway.domain.model.properties.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    private static final String ROUTING_KEY = "request.log.created";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Declarables rabbitDeclarables(RabbitMQProperties properties) {

        TopicExchange exchange = new TopicExchange(properties.getExchange(), true, false);
        Queue queue = new Queue(ROUTING_KEY, true);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);

        return new Declarables(exchange, queue, binding);
    }
}