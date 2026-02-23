package com.rubdev.eventsync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {
    private long ttlMinutes = 20;
    private long lockTimeoutSeconds = 5;
    private String keyPrefix = "events";
}
