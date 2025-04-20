package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.application.mapper.XmlMapper;
import com.ivantodorov.gateway.domain.exception.DuplicateRequestException;
import com.ivantodorov.gateway.domain.exception.InvalidXmlRequestException;
import com.ivantodorov.gateway.domain.model.xml.XmlGet;
import com.ivantodorov.gateway.domain.model.xml.XmlHistory;
import com.ivantodorov.gateway.domain.model.xml.XmlRate;
import com.ivantodorov.gateway.domain.model.xml.XmlRequest;
import com.ivantodorov.gateway.domain.model.xml.XmlResponse;
import com.ivantodorov.gateway.infrastructure.db.CurrencyRateRepository;
import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XmlApiServiceTest {

    private static final String REQUEST_ID = "req-001";
    private static final String CLIENT = "EXT_SERVICE_2";
    private static final String CURRENCY = "USD";
    private static final long TIMESTAMP_MILLIS = System.currentTimeMillis();
    private static final LocalDateTime NOW = LocalDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_MILLIS), ZoneOffset.UTC);

    private final CurrencyRate currencyRate = CurrencyRate.builder()
            .currency(CURRENCY)
            .baseCurrency("EUR")
            .rate(new BigDecimal("1.23"))
            .timestamp(NOW.minusMinutes(5))
            .build();

    @Mock
    private CurrencyRateRepository currencyRateRepository;
    @Mock
    private RequestTrackerService requestTrackerService;
    @Mock
    private RequestLogService requestLogService;
    @Mock
    private XmlMapper xmlMapper;
    @InjectMocks
    private XmlApiService xmlApiService;

    @Test
    void processReturnGetResponseWhenValidGetRequest() {

        XmlRequest request = XmlRequest.builder()
                .requestId(REQUEST_ID)
                .client(CLIENT)
                .timestamp(TIMESTAMP_MILLIS)
                .get(XmlGet.builder().currency(CURRENCY).build())
                .build();

        XmlRate xmlRate = XmlRate.builder().build();
        XmlResponse xmlResponse = XmlResponse.builder()
                .baseCurrency("EUR")
                .currency("USD")
                .rates(Collections.singletonList(xmlRate))
                .build();

        when(xmlMapper.toXmlResponse(anyString(), anyList())).thenReturn(xmlResponse);
        when(currencyRateRepository.findTopByCurrencyOrderByTimestampDesc(CURRENCY)).thenReturn(currencyRate);

        XmlResponse response = xmlApiService.process(request);

        assertNotNull(response);
        assertEquals(CURRENCY, response.getCurrency());
        assertEquals("EUR", response.getBaseCurrency());
        assertEquals(1, response.getRates().size());

        verify(requestTrackerService).validateNoDuplicateRequest("EXT_SERVICE_2", REQUEST_ID);
        verify(requestLogService).saveTrackingLog("EXT_SERVICE_2", REQUEST_ID, CLIENT, NOW);
    }

    @Test
    void processReturnHistoryResponseWhenValidHistoryRequest() {

        int period = 6;
        XmlRequest request = XmlRequest.builder()
                .requestId(REQUEST_ID)
                .client(CLIENT)
                .timestamp(TIMESTAMP_MILLIS)
                .history(XmlHistory.builder().currency(CURRENCY).period(period).build())
                .build();

        XmlRate xmlRate = XmlRate.builder().build();
        XmlResponse xmlResponse = XmlResponse.builder()
                .baseCurrency("EUR")
                .currency("USD")
                .rates(Collections.singletonList(xmlRate))
                .build();

        when(xmlMapper.toXmlResponse(anyString(), anyList())).thenReturn(xmlResponse);
        when(currencyRateRepository.findByCurrencyAndTimestampAfter(eq(CURRENCY), any()))
                .thenReturn(List.of(currencyRate));

        XmlResponse response = xmlApiService.process(request);

        assertNotNull(response);
        assertEquals(CURRENCY, response.getCurrency());
        assertEquals("EUR", response.getBaseCurrency());
        assertEquals(1, response.getRates().size());

        verify(requestTrackerService).markRequestAsProcessed("EXT_SERVICE_2", REQUEST_ID);
        verify(requestLogService).saveTrackingLog("EXT_SERVICE_2", REQUEST_ID, CLIENT, NOW);
    }

    @Test
    void processThrowExceptionWhenRequestHasNoGetOrHistory() {

        XmlRequest request = XmlRequest.builder()
                .requestId(REQUEST_ID)
                .client(CLIENT)
                .timestamp(TIMESTAMP_MILLIS)
                .build();

        InvalidXmlRequestException ex = assertThrows(
                InvalidXmlRequestException.class,
                () -> xmlApiService.process(request)
        );

        assertEquals("XML request must contain <get> or <history>", ex.getMessage());
        verifyNoInteractions(currencyRateRepository);
        verifyNoInteractions(requestLogService);
    }

    @Test
    void processThrowExceptionWhenDuplicateRequest() {

        XmlRequest request = XmlRequest.builder()
                .requestId(REQUEST_ID)
                .client(CLIENT)
                .timestamp(TIMESTAMP_MILLIS)
                .get(XmlGet.builder().currency(CURRENCY).build())
                .build();

        doThrow(new DuplicateRequestException("Duplicate!"))
                .when(requestTrackerService).validateNoDuplicateRequest("EXT_SERVICE_2", REQUEST_ID);

        DuplicateRequestException ex = assertThrows(
                DuplicateRequestException.class,
                () -> xmlApiService.process(request)
        );

        assertEquals("Duplicate!", ex.getMessage());
        verify(requestLogService, never()).saveTrackingLog(any(), any(), any(), any());
    }

    @Test
    void processHandleMissingCurrencyData() {

        XmlRequest request = XmlRequest.builder()
                .requestId(REQUEST_ID)
                .client(CLIENT)
                .timestamp(TIMESTAMP_MILLIS)
                .get(XmlGet.builder().currency(CURRENCY).build())
                .build();

        when(xmlMapper.toXmlResponse(anyString(), anyList()))
                .thenReturn(XmlResponse.builder().rates(Collections.emptyList()).build());
        when(currencyRateRepository.findTopByCurrencyOrderByTimestampDesc(CURRENCY)).thenReturn(null);

        XmlResponse response = xmlApiService.process(request);

        assertNotNull(response);
        assertEquals(0, response.getRates().size());
    }
}