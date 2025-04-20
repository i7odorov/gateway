package com.ivantodorov.gateway.infrastructure.db;


import com.ivantodorov.gateway.infrastructure.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
}