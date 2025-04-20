package com.ivantodorov.gateway.domain.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonCurrencyResponse {

    private String currency;
    private String baseCurrency;
    private List<RateEntry> rates;
}
