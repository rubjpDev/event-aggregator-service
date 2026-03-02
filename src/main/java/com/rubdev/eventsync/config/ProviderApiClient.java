package com.rubdev.eventsync.config;

import com.rubdev.eventsync.utils.ConnectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderApiClient {

    private final RestTemplate restTemplate;
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 5000;

    public String fetchProviderEvents() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("XML FETCH -- RETRY NUMBER: {}", attempt);
                return restTemplate.getForObject(ConnectionUtils.PROVIDER_URL.getValue(), String.class);
            } catch (HttpServerErrorException e) {
                log.warn("PROVIDER ERROR: {}", e.getStatusCode());
            } catch (RestClientException r) {
                log.warn("CONNECTION ERROR: {}", r.getMessage());
            }
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("-- THREAD ERROR, RETRY INTERRUPTED --");
                break;
            }
        }
        return null;
    }
}
