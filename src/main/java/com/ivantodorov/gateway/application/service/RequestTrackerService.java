package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.application.mapper.TrackingMapper;
import com.ivantodorov.gateway.domain.exception.DuplicateRequestException;
import com.ivantodorov.gateway.infrastructure.db.RequestTrackerRepository;
import com.ivantodorov.gateway.infrastructure.redis.Redis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestTrackerService {

    private final Redis redis;
    private final RequestTrackerRepository requestTrackerRepository;
    private final TrackingMapper trackingMapper;

    public void validateNoDuplicateRequest(String serviceId, String requestId) {

        if (isDuplicate(serviceId, requestId)) {
            throw new DuplicateRequestException(
                    String.format("Duplicate request with requestId: %s and serviceId: %s.", requestId, serviceId));
        }
    }

    public boolean isDuplicate(String serviceId, String requestId) {
        if (redis.isDuplicate(serviceId, requestId)) {
            return true;
        }

        return requestTrackerRepository.findByRequestIdAndSourceService(requestId, serviceId).isPresent();
    }

    public void markRequestAsProcessed(String serviceId, String requestId) {

        redis.markAsProcessed(serviceId, requestId);
        requestTrackerRepository.save(trackingMapper.toRequestTracker(serviceId, requestId));
    }
}
