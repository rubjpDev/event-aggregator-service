package com.rubdev.eventsync.cache;

import com.rubdev.eventsync.config.CacheProperties;
import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EventRepository eventRepository;
    private final CacheProperties cacheProperties;

    public List<EventEntity> getEvents(LocalDateTime start, LocalDateTime end) {
        String key = cacheKey();
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof List<?> raw) {
            log.info("Cache hit for key={}", key);
            return raw.stream()
                    .filter(EventEntity.class::isInstance)
                    .map(EventEntity.class::cast)
                    .toList();
        }

        String lockKey = lockKey();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(cacheProperties.getLockTimeoutSeconds()));

        if (Boolean.TRUE.equals(acquired)) {
            try {
                log.info("Cache miss, lock acquired, querying DB for key={}", key);
                List<EventEntity> entities = eventRepository.findByStartDateBetween(start, end);
                putEvents(entities);
                return entities;
            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        log.warn("Cache miss, lock not acquired, retrying cache for key={}", key);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Object retry = redisTemplate.opsForValue().get(key);
        if (retry instanceof List<?> raw) {
            return raw.stream()
                    .filter(EventEntity.class::isInstance)
                    .map(EventEntity.class::cast)
                    .toList();
        }

        log.warn("Cache still empty after retry, falling back to DB without caching for key={}", key);
        return eventRepository.findByStartDateBetween(start, end);
    }

    public void putEvents(List<EventEntity> events) {
        redisTemplate.opsForValue().set(cacheKey(), events, Duration.ofMinutes(cacheProperties.getTtlMinutes()));
        log.info("Cache populated for key={} with TTL={}min", cacheKey(), cacheProperties.getTtlMinutes());
    }

    public void evictEvents() {
        redisTemplate.delete(cacheKey());
        log.info("Cache evicted for key={}", cacheKey());
    }

    private String cacheKey() {
        return cacheProperties.getKeyPrefix() + ":all";
    }

    private String lockKey() {
        return cacheProperties.getKeyPrefix() + ":lock";
    }
}
