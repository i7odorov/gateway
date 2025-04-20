package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.domain.exception.CustomFixerException;
import com.ivantodorov.gateway.domain.model.ErrorInfo;
import com.ivantodorov.gateway.domain.model.FixerApiResponse;
import com.ivantodorov.gateway.domain.model.properties.FixerApiProperties;
import com.ivantodorov.gateway.infrastructure.db.CurrencyRateRepository;
import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixerSyncService {

    private static final String ACCESS_KEY = "?access_key=";

    private final FixerApiProperties fixerApiProperties;
    private final CurrencyRateRepository rateRepository;
    private final RestTemplate restTemplate;

    // It is called once when started
    @PostConstruct
    public void init() {
        syncRates();
    }

    @Scheduled(fixedDelayString = "${fixer.scheduler.interval}")
    public void syncRates() {

        FixerApiResponse response;

        try{

            response = restTemplate.getForObject(buildFixerApiUrl(), FixerApiResponse.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {

            log.error("Error fetching currency rates from Fixer API: {}", e.getMessage(), e);
            throw new CustomFixerException("Error fetching currency rates from Fixer API");
        } catch (Exception e) {

            log.error("Unexpected error occurred while fetching currency rates: {}", e.getMessage(), e);
            throw new CustomFixerException("Error fetching currency rates from Fixer API");
        }

        if (response.isSuccess()) {

            response.getRates().forEach((currency, rate) ->
                    rateRepository.save(buildCurrencyRate(currency, rate, response.getBase())));
            log.info("Currency rate sync successfully.");
        } else {

            ErrorInfo error = response.getErrorInfo();
            log.error("Fixer API error: code={}, type={}, info={}",
                    error.getCode(), error.getType(), error.getInfo());
        }
    }

    private CurrencyRate buildCurrencyRate(String currency, BigDecimal rate, String base) {

        return CurrencyRate.builder()
                .currency(currency)
                .rate(rate)
                .baseCurrency(base)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String buildFixerApiUrl() {

        return fixerApiProperties.getUrl() + ACCESS_KEY + fixerApiProperties.getKey();
    }
}