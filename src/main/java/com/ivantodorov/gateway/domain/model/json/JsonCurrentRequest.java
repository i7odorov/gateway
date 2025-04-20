package com.ivantodorov.gateway.domain.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonCurrentRequest {

    private String requestId;
    private long timestamp;
    private String client;
    private String currency;
}
