package com.ivantodorov.gateway.application.service;

import com.ivantodorov.gateway.application.mapper.XmlMapper;
import com.ivantodorov.gateway.domain.exception.InvalidXmlRequestException;
import com.ivantodorov.gateway.domain.model.xml.XmlRequest;
import com.ivantodorov.gateway.domain.model.xml.XmlResponse;
import com.ivantodorov.gateway.infrastructure.db.CurrencyRateRepository;
import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XmlApiService {

    private static final String SERVICE_ID = "EXT_SERVICE_2";

    private final CurrencyRateRepository currencyRateRepository;
    private final RequestTrackerService requestTrackerService;
    private final RequestLogService requestLogService;
    private final XmlMapper xmlMapper;

    public XmlResponse process(XmlRequest request) {

        requestTrackerService.validateNoDuplicateRequest(SERVICE_ID, request.getRequestId());
        requestTrackerService.markRequestAsProcessed(SERVICE_ID, request.getRequestId());

        LocalDateTime requestTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneOffset.UTC);
        XmlResponse response;

        if (request.getGet() != null) {
            response = buildGetResponse(request);
        } else if (request.getHistory() != null) {
            response = buildHistoryResponse(request, requestTime);
        } else {
            throw new InvalidXmlRequestException("XML request must contain <get> or <history>");
        }

        requestLogService.saveTrackingLog(SERVICE_ID, request.getRequestId(), request.getClient(), requestTime);

        return response;
    }

    private XmlResponse buildGetResponse(XmlRequest request) {

        String currency = request.getGet().getCurrency().toUpperCase();
        CurrencyRate latest = currencyRateRepository.findTopByCurrencyOrderByTimestampDesc(currency);
        List<CurrencyRate> rates = latest != null ? List.of(latest) : List.of();

        return xmlMapper.toXmlResponse(currency, rates);
    }

    private XmlResponse buildHistoryResponse(XmlRequest request, LocalDateTime requestTime) {

        String currency = request.getHistory().getCurrency().toUpperCase();
        int period = request.getHistory().getPeriod();
        LocalDateTime from = requestTime.minusHours(period);
        List<CurrencyRate> rates = currencyRateRepository.findByCurrencyAndTimestampAfter(currency, from);

        return xmlMapper.toXmlResponse(currency, rates);
    }
}
