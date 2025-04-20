package com.ivantodorov.gateway.application.service;


import com.ivantodorov.gateway.application.mapper.TrackingMapper;
import com.ivantodorov.gateway.application.publisher.RequestEventPublisher;
import com.ivantodorov.gateway.domain.exception.PublishRequestEventException;
import com.ivantodorov.gateway.domain.exception.SaveTrackingLogException;
import com.ivantodorov.gateway.domain.model.rabbitmq.RequestEventMessage;
import com.ivantodorov.gateway.infrastructure.db.RequestLogRepository;
import com.ivantodorov.gateway.infrastructure.entity.RequestLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestLogServiceTest {

    private final String SERVICE_ID = "EXT_SERVICE_1";
    private final String REQUEST_ID = "req-001";
    private final String CLIENT = "client-001";
    private final LocalDateTime TIMESTAMP = LocalDateTime.now();

    @Mock
    private RequestLogRepository requestLogRepository;
    @Mock
    private RequestEventPublisher requestEventPublisher;
    @Mock
    private TrackingMapper trackingMapper;
    @InjectMocks
    private RequestLogService requestLogService;

    @Test
    void saveTrackingLogSaveAndPublishSuccessfully() {

        RequestLog requestLog = RequestLog.builder()
                .requestId(REQUEST_ID)
                .client(CLIENT)
                .serviceId(SERVICE_ID)
                .build();

        RequestEventMessage requestEventMessage = RequestEventMessage.builder()
                .requestId(REQUEST_ID)
                .client(CLIENT)
                .service(SERVICE_ID)
                .timestamp(TIMESTAMP)
                .build();

        when(trackingMapper.toRequestLog(anyString(), anyString(), anyString(), any()))
                .thenReturn(requestLog);
        when(trackingMapper.toRequestEventMessage(anyString(), anyString(), anyString(), any()))
                .thenReturn(requestEventMessage);
        assertDoesNotThrow(() -> requestLogService.saveTrackingLog(SERVICE_ID, REQUEST_ID, CLIENT, TIMESTAMP));

        ArgumentCaptor<RequestLog> logCaptor = ArgumentCaptor.forClass(RequestLog.class);
        ArgumentCaptor<RequestEventMessage> eventCaptor = ArgumentCaptor.forClass(RequestEventMessage.class);

        verify(requestLogRepository, times(1)).save(logCaptor.capture());
        verify(requestEventPublisher, times(1)).publish(eventCaptor.capture());

        RequestLog capturedLog = logCaptor.getValue();
        RequestEventMessage capturedEvent = eventCaptor.getValue();

        assertEquals(REQUEST_ID, capturedLog.getRequestId());
        assertEquals(CLIENT, capturedLog.getClient());
        assertEquals(SERVICE_ID, capturedLog.getServiceId());

        assertEquals(REQUEST_ID, capturedEvent.getRequestId());
        assertEquals(CLIENT, capturedEvent.getClient());
        assertEquals(SERVICE_ID, capturedEvent.getService());
    }

    @Test
    void saveTrackingLogThrowSaveTrackingLogException() {

        when(trackingMapper.toRequestLog(anyString(), anyString(), anyString(), any()))
                .thenReturn(RequestLog.builder().build());
        doThrow(new RuntimeException("DB error"))
                .when(requestLogRepository).save(any(RequestLog.class));

        SaveTrackingLogException ex = assertThrows(SaveTrackingLogException.class,
                () -> requestLogService.saveTrackingLog(SERVICE_ID, REQUEST_ID, CLIENT, TIMESTAMP));

        assertEquals("Saving tracking log in repository failed.", ex.getMessage());

        verify(requestEventPublisher, never()).publish(any());
    }

    @Test
    void saveTrackingLogThrowPublishRequestEventException() {

        when(trackingMapper.toRequestLog(anyString(), anyString(), anyString(), any()))
                .thenReturn(RequestLog.builder().build());

        doThrow(new RuntimeException("RabbitMQ down"))
                .when(requestEventPublisher).publish(any(RequestEventMessage.class));

        PublishRequestEventException ex = assertThrows(PublishRequestEventException.class,
                () -> requestLogService.saveTrackingLog(SERVICE_ID, REQUEST_ID, CLIENT, TIMESTAMP));

        assertEquals("Publishing request event failed.", ex.getMessage());

        verify(requestLogRepository, times(1)).save(any(RequestLog.class));
    }
}