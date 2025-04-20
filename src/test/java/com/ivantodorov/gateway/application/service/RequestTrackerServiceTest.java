package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.application.mapper.TrackingMapper;
import com.ivantodorov.gateway.domain.exception.DuplicateRequestException;
import com.ivantodorov.gateway.infrastructure.db.RequestTrackerRepository;
import com.ivantodorov.gateway.infrastructure.entity.RequestTracker;
import com.ivantodorov.gateway.infrastructure.redis.Redis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTrackerServiceTest {

    private static final String SERVICE_ID = "EXT_SERVICE_1";
    private static final String REQUEST_ID = "req-001";

    @Mock
    private Redis redis;
    @Mock
    private RequestTrackerRepository requestTrackerRepository;
    @Mock
    private TrackingMapper trackingMapper;
    @InjectMocks
    private RequestTrackerService requestTrackerService;

    @Captor
    private ArgumentCaptor<RequestTracker> trackerCaptor;

    @Test
    void validateNoDuplicateRequestSuccessful() {
        when(redis.isDuplicate(SERVICE_ID, REQUEST_ID)).thenReturn(false);
        when(requestTrackerRepository.findByRequestIdAndSourceService(REQUEST_ID, SERVICE_ID))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> requestTrackerService.validateNoDuplicateRequest(SERVICE_ID, REQUEST_ID));
    }

    @Test
    void validateNoDuplicateRequestThrowWhenDuplicateInRedis() {
        when(redis.isDuplicate(SERVICE_ID, REQUEST_ID)).thenReturn(true);

        DuplicateRequestException ex = assertThrows(
                DuplicateRequestException.class,
                () -> requestTrackerService.validateNoDuplicateRequest(SERVICE_ID, REQUEST_ID)
        );

        assertTrue(ex.getMessage().contains(REQUEST_ID));
    }

    @Test
    void validateNoDuplicateRequestTrowWhenDuplicateInDatabase() {
        when(redis.isDuplicate(SERVICE_ID, REQUEST_ID)).thenReturn(false);
        when(requestTrackerRepository.findByRequestIdAndSourceService(REQUEST_ID, SERVICE_ID))
                .thenReturn(Optional.of(new RequestTracker()));

        DuplicateRequestException ex = assertThrows(
                DuplicateRequestException.class,
                () -> requestTrackerService.validateNoDuplicateRequest(SERVICE_ID, REQUEST_ID)
        );

        assertTrue(ex.getMessage().contains(SERVICE_ID));
    }

    @Test
    void markRequestAsProcessedCallRedisAndSaveToRepository() {

        RequestTracker requestTracker = RequestTracker.builder()
                .requestId(REQUEST_ID)
                .sourceService(SERVICE_ID)
                .receivedAt(LocalDateTime.now())
                .build();

        when(trackingMapper.toRequestTracker(anyString(), anyString()))
                .thenReturn(requestTracker);
        assertDoesNotThrow(() -> requestTrackerService.markRequestAsProcessed(SERVICE_ID, REQUEST_ID));

        verify(redis).markAsProcessed(SERVICE_ID, REQUEST_ID);
        verify(requestTrackerRepository).save(trackerCaptor.capture());

        RequestTracker captured = trackerCaptor.getValue();
        assertEquals(SERVICE_ID, captured.getSourceService());
        assertEquals(REQUEST_ID, captured.getRequestId());
        assertNotNull(captured.getReceivedAt());
    }

    @Test
    void isDuplicateReturnTrueWhenFoundInRedis() {
        when(redis.isDuplicate(SERVICE_ID, REQUEST_ID)).thenReturn(true);

        assertTrue(requestTrackerService.isDuplicate(SERVICE_ID, REQUEST_ID));
    }

    @Test
    void isDuplicateReturnTrueWhenFoundInDatabase() {
        when(redis.isDuplicate(SERVICE_ID, REQUEST_ID)).thenReturn(false);
        when(requestTrackerRepository.findByRequestIdAndSourceService(REQUEST_ID, SERVICE_ID))
                .thenReturn(Optional.of(new RequestTracker()));

        assertTrue(requestTrackerService.isDuplicate(SERVICE_ID, REQUEST_ID));
    }

    @Test
    void isDuplicateReturnFalseWhenNowhereFound() {
        when(redis.isDuplicate(SERVICE_ID, REQUEST_ID)).thenReturn(false);
        when(requestTrackerRepository.findByRequestIdAndSourceService(REQUEST_ID, SERVICE_ID))
                .thenReturn(Optional.empty());

        assertFalse(requestTrackerService.isDuplicate(SERVICE_ID, REQUEST_ID));
    }
}