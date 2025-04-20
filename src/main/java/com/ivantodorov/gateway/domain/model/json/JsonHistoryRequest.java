package com.ivantodorov.gateway.domain.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonHistoryRequest {

    private String requestId;
    private long timestamp;
    private String client;
    private String currency;
    private int period; // in hours
}
