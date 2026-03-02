package com.rubdev.eventsync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.provider")
public class ProviderProperties {
    private String url = "http://localhost:9090/api/events";
}
