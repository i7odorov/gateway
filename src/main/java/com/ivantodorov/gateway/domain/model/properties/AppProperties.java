package com.ivantodorov.gateway.domain.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.duplication-check")
public class AppProperties {
    private long redisTtlHours;
}
