package com.rubdev.eventsync.service;

import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.model.response.EventResponseModel;
import com.rubdev.eventsync.repository.EventRepository;
import com.rubdev.eventsync.utils.ConnectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    /**
     * TEST THE HAPPY PATH, EVERYTHING WORKING EVEN CACHE
     */
    @Test
    void testGetEvents_FiltersByDateRange() {
        // GEN MOCKUPS
        LocalDateTime start = LocalDateTime.of(2021, 7, 31, 20, 0);
        LocalDateTime end   = LocalDateTime.of(2021, 7, 31, 21, 20);

        EventEntity inside = new EventEntity();
        inside.setId(1L);
        inside.setTitle("title1");
        inside.setStartDate(LocalDateTime.of(2021, 7, 31, 20, 0));
        inside.setEndDate(LocalDateTime.of(2021, 7, 31, 21, 0));
        inside.setMinPrice(65.0);
        inside.setMaxPrice(75.0);

        EventEntity outside = new EventEntity();
        outside.setId(2L);
        outside.setTitle("title2");
        outside.setStartDate(LocalDateTime.of(2021, 6, 30, 20, 0));
        outside.setEndDate(LocalDateTime.of(2021, 6, 30, 22, 0));
        outside.setMinPrice(15.0);
        outside.setMaxPrice(30.0);

        when(redisTemplate.opsForValue().get(ConnectionUtils.CACHE_KEY.getValue()))
                .thenReturn(List.of(inside, outside));


        EventResponseModel response = eventService.getEvents(start, end);

        // ASSERTIONS
        assertThat(response.getData().getEvents()).hasSize(1);
        assertThat(response.getData().getEvents().getFirst().getTitle()).isEqualTo("title1");
    }

    /**
     * TEST THE RARE CASE OF CACHE BEING EMPTY
     */
    @Test
    void testGetEvents_UsesDbFallbackWhenCacheEmpty() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end   = LocalDateTime.now().plusDays(1);

        // MOCK
        EventEntity event = new EventEntity();
        event.setTitle("test");
        event.setStartDate(LocalDateTime.now());
        event.setEndDate(LocalDateTime.now().plusHours(1));
        event.setMinPrice(10.0);
        event.setMaxPrice(20.0);

        when(redisTemplate.opsForValue().get(ConnectionUtils.CACHE_KEY.getValue())).thenReturn(null);
        when(eventRepository.findByStartDateBetween(start, end)).thenReturn(List.of(event));

        EventResponseModel response = eventService.getEvents(start, end);

        assertThat(response.getData().getEvents()).hasSize(1);
        assertThat(response.getData().getEvents().getFirst().getTitle()).isEqualTo("test");
    }
}
