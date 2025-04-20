package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.domain.model.json.JsonCurrencyResponse;
import com.ivantodorov.gateway.domain.model.json.JsonCurrentRequest;
import com.ivantodorov.gateway.domain.model.json.JsonHistoryRequest;
import com.ivantodorov.gateway.domain.model.json.RateEntry;
import com.ivantodorov.gateway.infrastructure.db.CurrencyRateRepository;
import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JsonApiService {

    private static final String SERVICE_ID = "EXT_SERVICE_1";

    private final CurrencyRateRepository currencyRateRepository;
    private final RequestTrackerService requestTrackerService;
    private final RequestLogService requestLogService;

    public JsonCurrencyResponse handleCurrentRequest(JsonCurrentRequest request) {

        requestTrackerService.validateNoDuplicateRequest(SERVICE_ID, request.getRequestId());
        requestTrackerService.markRequestAsProcessed(SERVICE_ID, request.getRequestId());

        CurrencyRate latest = currencyRateRepository
                .findTopByCurrencyOrderByTimestampDesc(request.getCurrency().toUpperCase());

        requestLogService.saveTrackingLog(SERVICE_ID, request.getRequestId(), request.getClient(), LocalDateTime.now());

        return buildResponse(request.getCurrency(), latest, List.of(latest));
    }

    public JsonCurrencyResponse handleHistoryRequest(JsonHistoryRequest request) {

        requestTrackerService.validateNoDuplicateRequest(SERVICE_ID, request.getRequestId());
        requestTrackerService.markRequestAsProcessed(SERVICE_ID, request.getRequestId());

        LocalDateTime fromTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(request.getTimestamp()), ZoneOffset.UTC)
                .minusHours(request.getPeriod());

        List<CurrencyRate> rates = currencyRateRepository
                .findByCurrencyAndTimestampAfter(request.getCurrency().toUpperCase(), fromTime);

        CurrencyRate latest = rates.stream()
                .max(Comparator.comparing(CurrencyRate::getTimestamp))
                .orElse(null);

        requestLogService.saveTrackingLog(SERVICE_ID, request.getRequestId(), request.getClient(), LocalDateTime.now());

        return buildResponse(request.getCurrency(), latest, rates);
    }

    private JsonCurrencyResponse buildResponse(String currency, CurrencyRate latest, List<CurrencyRate> all) {

        List<RateEntry> rateEntries = all.stream()
                .map(rate -> RateEntry.builder().rate(rate.getRate()).timestamp(rate.getTimestamp()).build())
                .collect(Collectors.toList());

        return JsonCurrencyResponse.builder()
                .currency(currency.toUpperCase())
                .baseCurrency(latest != null ? latest.getBaseCurrency() : null)
                .rates(rateEntries)
                .build();
    }
}
