package com.ivantodorov.gateway.domain.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateEntry {

    private BigDecimal rate;
    private LocalDateTime timestamp;
}
