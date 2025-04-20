package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.domain.exception.DuplicateRequestException;
import com.ivantodorov.gateway.domain.model.json.JsonCurrencyResponse;
import com.ivantodorov.gateway.domain.model.json.JsonCurrentRequest;
import com.ivantodorov.gateway.domain.model.json.JsonHistoryRequest;
import com.ivantodorov.gateway.infrastructure.db.CurrencyRateRepository;
import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JsonApiServiceTest {

    private static final String CURRENCY = "USD";
    private static final String DUPLICATE_ID = "dup-001";
    private static final CurrencyRate CURRENCY_RATE = CurrencyRate.builder()
            .currency(CURRENCY)
            .baseCurrency("EUR")
            .rate(new BigDecimal("1.10"))
            .timestamp(LocalDateTime.now())
            .build();

    @Mock
    private CurrencyRateRepository currencyRateRepository;
    @Mock
    private RequestTrackerService requestTrackerService;
    @Mock
    private RequestLogService requestLogService;
    @InjectMocks
    private JsonApiService jsonApiService;

    @Test
    void currentRequestReturnLatestRate() {

        JsonCurrentRequest request = JsonCurrentRequest.builder()
                .requestId("req-001")
                .currency(CURRENCY)
                .client("client-01")
                .timestamp(System.currentTimeMillis())
                .build();

        when(currencyRateRepository.findTopByCurrencyOrderByTimestampDesc(CURRENCY)).thenReturn(CURRENCY_RATE);

        JsonCurrencyResponse response = jsonApiService.handleCurrentRequest(request);

        verify(requestTrackerService).validateNoDuplicateRequest("EXT_SERVICE_1", "req-001");
        verify(requestTrackerService).markRequestAsProcessed("EXT_SERVICE_1", "req-001");
        verify(requestLogService).saveTrackingLog(eq("EXT_SERVICE_1"), eq("req-001"), eq("client-01"), any());

        assertNotNull(response);
        assertEquals(CURRENCY, response.getCurrency());
        assertEquals("EUR", response.getBaseCurrency());
        assertEquals(1, response.getRates().size());
    }

    @Test
    void historyRequestReturnMultipleRates() {

        JsonHistoryRequest request = JsonHistoryRequest.builder()
                .requestId("req-002")
                .currency(CURRENCY)
                .client("client-01")
                .timestamp(System.currentTimeMillis())
                .period(24)
                .build();

        List<CurrencyRate> rates = List.of(CURRENCY_RATE,
                CurrencyRate.builder()
                        .currency(CURRENCY)
                        .baseCurrency("EUR")
                        .rate(new BigDecimal("1.05"))
                        .timestamp(LocalDateTime.now().minusHours(5))
                        .build());

        when(currencyRateRepository.findByCurrencyAndTimestampAfter(eq(CURRENCY), any()))
                .thenReturn(rates);

        JsonCurrencyResponse response = jsonApiService.handleHistoryRequest(request);

        verify(requestTrackerService).validateNoDuplicateRequest("EXT_SERVICE_1", "req-002");
        verify(requestTrackerService).markRequestAsProcessed("EXT_SERVICE_1", "req-002");
        verify(requestLogService).saveTrackingLog(eq("EXT_SERVICE_1"), eq("req-002"), eq("client-01"), any());

        assertNotNull(response);
        assertEquals(CURRENCY, response.getCurrency());
        assertEquals("EUR", response.getBaseCurrency());
        assertEquals(2, response.getRates().size());
    }

    @Test
    void currentRequestThrowDuplicateRequestException() {

        JsonCurrentRequest request = JsonCurrentRequest.builder()
                .requestId(DUPLICATE_ID)
                .currency(CURRENCY)
                .client("client-01")
                .timestamp(System.currentTimeMillis())
                .build();

        doThrow(new DuplicateRequestException("Duplicate request ID"))
                .when(requestTrackerService)
                .validateNoDuplicateRequest("EXT_SERVICE_1", DUPLICATE_ID);

        DuplicateRequestException ex = assertThrows(
                DuplicateRequestException.class,
                () -> jsonApiService.handleCurrentRequest(request)
        );

        assertEquals("Duplicate request ID", ex.getMessage());
        verify(currencyRateRepository, never()).findTopByCurrencyOrderByTimestampDesc(any());
        verify(requestLogService, never()).saveTrackingLog(any(), any(), any(), any());
    }

    @Test
    void historyRequestThrowDuplicateRequestException() {

        JsonHistoryRequest request = JsonHistoryRequest.builder()
                .requestId(DUPLICATE_ID)
                .currency(CURRENCY)
                .client("client-02")
                .timestamp(System.currentTimeMillis())
                .period(12)
                .build();

        doThrow(new DuplicateRequestException("Duplicate request ID"))
                .when(requestTrackerService)
                .validateNoDuplicateRequest("EXT_SERVICE_1", DUPLICATE_ID);

        DuplicateRequestException ex = assertThrows(
                DuplicateRequestException.class,
                () -> jsonApiService.handleHistoryRequest(request)
        );

        assertEquals("Duplicate request ID", ex.getMessage());
        verify(currencyRateRepository, never()).findByCurrencyAndTimestampAfter(any(), any());
        verify(requestLogService, never()).saveTrackingLog(any(), any(), any(), any());
    }
}
