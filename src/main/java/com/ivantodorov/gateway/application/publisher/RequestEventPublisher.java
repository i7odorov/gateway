package com.ivantodorov.gateway.application.publisher;

import com.ivantodorov.gateway.domain.exception.PublishRequestEventException;
import com.ivantodorov.gateway.domain.model.properties.RabbitMQProperties;
import com.ivantodorov.gateway.domain.model.rabbitmq.RequestEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestEventPublisher {

    private static final String ROUTING_KEY = "request.log.created";

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    @Retryable(
            value = { AmqpException.class },
            maxAttemptsExpression = "${spring.rabbitmq.retry.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${spring.rabbitmq.retry.interval}",
                    multiplierExpression = "${spring.rabbitmq.retry.multiplier}",
                    maxDelayExpression = "${spring.rabbitmq.retry.max-interval}"
            )
    )
    public void publish(RequestEventMessage message) {

        try {

            rabbitTemplate.convertAndSend(properties.getExchange(), "request.log.created", message);
            log.info("Published RabbitMQ event: {}", message);
        } catch (AmqpException e) {

            log.error("RabbitMQ error occurred while publishing message: {}", message, e);
            throw new PublishRequestEventException("Failed to publish event to RabbitMQ");
        }
    }
}
