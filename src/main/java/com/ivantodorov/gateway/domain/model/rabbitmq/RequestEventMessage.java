package com.ivantodorov.gateway.domain.model.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestEventMessage {

    private String service;
    private String requestId;
    private String client;
    private LocalDateTime timestamp;
}
