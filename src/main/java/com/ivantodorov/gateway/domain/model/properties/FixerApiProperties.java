package com.ivantodorov.gateway.domain.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fixer.api")
@Data
public class FixerApiProperties {

    private String key;
    private String url;
    private String base;
}