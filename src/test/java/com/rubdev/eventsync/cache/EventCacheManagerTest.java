package com.rubdev.eventsync.cache;

import com.rubdev.eventsync.config.CacheProperties;
import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventCacheManagerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CacheProperties cacheProperties;

    @InjectMocks
    private EventCacheManager eventCacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(cacheProperties.getKeyPrefix()).thenReturn("events");
        when(cacheProperties.getTtlMinutes()).thenReturn(20L);
        when(cacheProperties.getLockTimeoutSeconds()).thenReturn(5L);
    }

    @Test
    void testGetEvents_CacheHit_ReturnsCachedEntities() {
        EventEntity entity = new EventEntity();
        entity.setId(1L);
        entity.setTitle("Cached Event");
        entity.setStartDate(LocalDateTime.now());
        entity.setEndDate(LocalDateTime.now().plusHours(2));

        when(valueOps.get("events:all")).thenReturn(List.of(entity));

        List<EventEntity> result = eventCacheManager.getEvents(LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Cached Event");
        verifyNoInteractions(eventRepository);
    }

    @Test
    void testGetEvents_CacheMiss_QueriesDbAndPopulatesCache() {
        EventEntity entity = new EventEntity();
        entity.setId(2L);
        entity.setTitle("DB Event");
        entity.setStartDate(LocalDateTime.now());
        entity.setEndDate(LocalDateTime.now().plusHours(1));

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        when(valueOps.get("events:all")).thenReturn(null);
        when(valueOps.setIfAbsent(eq("events:lock"), eq("1"), any(Duration.class))).thenReturn(true);
        when(eventRepository.findByStartDateBetween(start, end)).thenReturn(List.of(entity));

        List<EventEntity> result = eventCacheManager.getEvents(start, end);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("DB Event");
        verify(valueOps).set(eq("events:all"), anyList(), any(Duration.class));
        verify(redisTemplate).delete("events:lock");
    }

    @Test
    void testPutEvents_SetsValueWithCorrectTtl() {
        EventEntity entity = new EventEntity();
        entity.setId(3L);

        eventCacheManager.putEvents(List.of(entity));

        verify(valueOps).set(eq("events:all"), eq(List.of(entity)), eq(Duration.ofMinutes(20)));
    }

    @Test
    void testEvictEvents_DeletesCacheKey() {
        eventCacheManager.evictEvents();

        verify(redisTemplate).delete("events:all");
    }
}
