package com.ivantodorov.gateway.presentation.controller;

import com.ivantodorov.gateway.application.service.JsonApiService;
import com.ivantodorov.gateway.domain.model.json.JsonCurrencyResponse;
import com.ivantodorov.gateway.domain.model.json.JsonCurrentRequest;
import com.ivantodorov.gateway.domain.model.json.JsonHistoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/json")
@RequiredArgsConstructor
public class JsonApiController {

    private final JsonApiService jsonApiService;

    @PostMapping("/current")
    public ResponseEntity<JsonCurrencyResponse> getCurrentRate(@RequestBody JsonCurrentRequest request) {
        JsonCurrencyResponse response = jsonApiService.handleCurrentRequest(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/history")
    public ResponseEntity<JsonCurrencyResponse> getRateHistory(@RequestBody JsonHistoryRequest request) {
        JsonCurrencyResponse response = jsonApiService.handleHistoryRequest(request);
        return ResponseEntity.ok(response);
    }
}
