package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.domain.exception.CustomFixerException;
import com.ivantodorov.gateway.domain.model.ErrorInfo;
import com.ivantodorov.gateway.domain.model.FixerApiResponse;
import com.ivantodorov.gateway.domain.model.properties.FixerApiProperties;
import com.ivantodorov.gateway.infrastructure.db.CurrencyRateRepository;
import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FixerSyncServiceTest {

    @Mock
    private FixerApiProperties fixerApiProperties;
    @Mock
    private CurrencyRateRepository rateRepository;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private FixerSyncService fixerSyncService;

    @Test
    public void fixerApiReturnsSuccess() {

        Map<String, BigDecimal> rates = Map.of("USD", new BigDecimal("1.10"), "BGN", new BigDecimal("1.95"));
        FixerApiResponse response = FixerApiResponse.builder()
                .success(true)
                .base("EUR")
                .rates(rates)
                .build();

        when(fixerApiProperties.getUrl()).thenReturn("https://api.fixer.io/latest");
        when(fixerApiProperties.getKey()).thenReturn("testkey");
        when(restTemplate.getForObject(anyString(), eq(FixerApiResponse.class))).thenReturn(response);

        fixerSyncService.syncRates();

        verify(rateRepository, times(2)).save(any(CurrencyRate.class));
    }

    @Test
    public void fixerApiThrowsException() {

        when(restTemplate.getForObject(anyString(), eq(FixerApiResponse.class))).thenThrow(HttpClientErrorException.class);
        Assertions.assertThrows(CustomFixerException.class, () -> fixerSyncService.syncRates());
    }

    @Test
    public void fixerApiReturnsError() {

        ErrorInfo errorInfo = ErrorInfo.builder()
                .code(101)
                .type("invalid_key")
                .info("The key is invalid.")
                .build();

        FixerApiResponse response = FixerApiResponse.builder()
                .success(false)
                .errorInfo(errorInfo)
                .build();

        when(restTemplate.getForObject(anyString(), eq(FixerApiResponse.class))).thenReturn(response);

        fixerSyncService.syncRates();

        verify(rateRepository, never()).save(any());
    }
}
