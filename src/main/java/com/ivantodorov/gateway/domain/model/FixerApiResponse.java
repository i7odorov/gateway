package com.ivantodorov.gateway.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FixerApiResponse {

    private boolean success;
    private String base;
    private String date;
    private Map<String, BigDecimal> rates;
    private ErrorInfo errorInfo;
}
