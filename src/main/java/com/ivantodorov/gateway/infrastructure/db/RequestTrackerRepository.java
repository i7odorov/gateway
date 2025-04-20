package com.ivantodorov.gateway.infrastructure.db;

import com.ivantodorov.gateway.infrastructure.entity.RequestTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestTrackerRepository extends JpaRepository<RequestTracker, Long> {
    Optional<RequestTracker> findByRequestIdAndSourceService(String requestId, String sourceService);
}