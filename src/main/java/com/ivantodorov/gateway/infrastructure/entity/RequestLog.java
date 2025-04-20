package com.ivantodorov.gateway.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_logs")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "service_id")
    private String serviceId;   // EXT_SERVICE_1, EXT_SERVICE_2
    @Column(name = "request_id")
    private String requestId;
    @Column(name = "client")
    private String client;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}