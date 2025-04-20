package com.ivantodorov.gateway.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_tracker", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requestId", "sourceService"})
})
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "request_id")
    private String requestId;
    @Column(name = "source_service")
    private String sourceService;   // EXT_SERVICE_1, EXT_SERVICE_2
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
}