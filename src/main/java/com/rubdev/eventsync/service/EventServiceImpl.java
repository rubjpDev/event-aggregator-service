package com.rubdev.eventsync.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.model.response.EventResponseDataModel;
import com.rubdev.eventsync.model.response.EventResponseModel;
import com.rubdev.eventsync.model.service.EventService;
import com.rubdev.eventsync.repository.EventRepository;
import com.rubdev.eventsync.utils.ConnectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    @Autowired
    private final EventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); //FOR CORRECT DESERIALIZATION OF CACHE SAME AS IN CACHE CONFIG

    @Override
    public EventResponseModel getEvents(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("SERVICE -- getEvents -- INIT");
        EventResponseModel response = new EventResponseModel();

        //READ FROM CACHE OR DB IF ALL FAILS
        Object cached = redisTemplate.opsForValue().get(ConnectionUtils.CACHE_KEY.getValue());
        List<EventEntity> eventEntities = this.castCache(cached,startDate,endDate);


        //THIS FILTERS AGAIN FOR SECURITY DUE TO CACHE VOLATILITY
        List<EventResponseDataModel> filter = eventEntities.stream()
                .filter(event -> !event.getStartDate().isBefore(startDate) &&
                            !event.getEndDate().isAfter(endDate)).map(this::toResponseData).toList();

        //CREATE AND FILL WRAPPER MODEL
        EventResponseModel.DataWrapper wrapper = new EventResponseModel.DataWrapper();
        wrapper.setEvents(filter);
        response.setData(wrapper);

        log.info("SERVICE -- getEvents -- END");
        return response;
    }

    private List<EventEntity> castCache (Object cached, LocalDateTime startDate, LocalDateTime endDate){
        if(cached instanceof List<?> raw){
            log.info("SERVICE -- getEvents -- USING DATA FROM CACHE");
            return raw.stream().map(item -> objectMapper.convertValue(item,EventEntity.class)).toList();
        }


        //THIS PATH ONLY AS LAST RESORT IF CACHE SOMEHOW FAILS
        log.warn("SERVICE -- getEvents -- CACHE WAS EMPTY, DB FALLBACK USED");
        List<EventEntity> dbResponse = eventRepository.findByStartDateBetween(startDate, endDate);
        redisTemplate.opsForValue().set(ConnectionUtils.CACHE_KEY.getValue(), dbResponse, Duration.ofMinutes(16));
        return dbResponse;
    }

    private EventResponseDataModel toResponseData(EventEntity eventEntity){
        log.info("SERVICE -- getEvents -- GENERATING RESPONSE MODELS");
        EventResponseDataModel data = new EventResponseDataModel();
        data.setId(eventEntity.getId());
        data.setTitle(eventEntity.getTitle());
        data.setStartDate(eventEntity.getStartDate().toLocalDate());
        data.setEndDate(eventEntity.getEndDate().toLocalDate());
        data.setStartTime(eventEntity.getStartDate().toLocalTime());
        data.setEndTime(eventEntity.getEndDate().toLocalTime());
        data.setMaxPrice(eventEntity.getMaxPrice());
        data.setMinPrice(eventEntity.getMinPrice());
        return data;
    }
}
