package com.rubdev.eventsync.task;

import com.rubdev.eventsync.model.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncProviderTask {
    private final ProviderService providerService;

    @Scheduled(fixedRate = 900000) // 15min
    public void syncEventsFromProvider() {
        log.info("TASK: -- syncEventsFromProvider -- INIT");
        try {
            this.providerService.syncWithProvider();
            log.info("TASK: -- syncEventsFromProvider -- SUCCESS");
        } catch (Exception e) {
            log.error("TASK: -- syncEventsFromProvider -- ERROR: {}", e.getMessage(), e);
        }
    }

}
