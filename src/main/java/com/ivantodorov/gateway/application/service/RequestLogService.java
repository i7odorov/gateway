package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.application.mapper.TrackingMapper;
import com.ivantodorov.gateway.application.publisher.RequestEventPublisher;
import com.ivantodorov.gateway.domain.exception.PublishRequestEventException;
import com.ivantodorov.gateway.domain.exception.SaveTrackingLogException;
import com.ivantodorov.gateway.infrastructure.db.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestLogService {

    private static final String REPOSITORY_SUCCESS_MESSAGE = "Saved tracking log in repository.";
    private static final String REPOSITORY_EXCEPTION_MESSAGE = "Saving tracking log in repository failed.";
    private static final String PUBLISHER_SUCCESS_MESSAGE = "Published request event.";
    private static final String PUBLISHER_EXCEPTION_MESSAGE = "Publishing request event failed.";

    private final RequestLogRepository requestLogRepository;
    private final RequestEventPublisher requestEventPublisher;
    private final TrackingMapper trackingMapper;

    public void saveTrackingLog(String serviceId, String requestId, String client, LocalDateTime timestamp) {

        saveTackingInRepository(serviceId, requestId, client, timestamp);
        publishRequestEvent(serviceId, requestId, client, timestamp);
    }

    private void saveTackingInRepository(String serviceId, String requestId, String client, LocalDateTime timestamp) {

        try {
            requestLogRepository.save(trackingMapper.toRequestLog(serviceId, requestId, client, timestamp));
            log.info(REPOSITORY_SUCCESS_MESSAGE);
        } catch (RuntimeException ex) {
            log.error(REPOSITORY_EXCEPTION_MESSAGE);
            throw new SaveTrackingLogException(REPOSITORY_EXCEPTION_MESSAGE);
        }
    }

    private void publishRequestEvent(String serviceId, String requestId, String client, LocalDateTime timestamp) {

        try {
            requestEventPublisher.publish(
                    trackingMapper.toRequestEventMessage(serviceId, requestId, client, timestamp));
            log.info(PUBLISHER_SUCCESS_MESSAGE);
        } catch (RuntimeException ex) {
            log.error(PUBLISHER_EXCEPTION_MESSAGE);
            throw new PublishRequestEventException(PUBLISHER_EXCEPTION_MESSAGE);
        }
    }
}
