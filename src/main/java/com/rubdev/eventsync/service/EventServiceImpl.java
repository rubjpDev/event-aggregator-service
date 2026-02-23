package com.rubdev.eventsync.service;

import com.rubdev.eventsync.cache.EventCacheManager;
import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.model.response.EventResponseDataModel;
import com.rubdev.eventsync.model.response.EventResponseModel;
import com.rubdev.eventsync.model.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventCacheManager eventCacheManager;

    @Override
    public EventResponseModel getEvents(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("SERVICE -- getEvents -- INIT");

        List<EventEntity> eventEntities = eventCacheManager.getEvents(startDate, endDate);

        List<EventResponseDataModel> filtered = eventEntities.stream()
                .filter(event -> !event.getStartDate().isBefore(startDate) &&
                        !event.getEndDate().isAfter(endDate))
                .map(this::toResponseData)
                .toList();

        EventResponseModel.DataWrapper wrapper = new EventResponseModel.DataWrapper();
        wrapper.setEvents(filtered);

        EventResponseModel response = new EventResponseModel();
        response.setData(wrapper);

        log.info("SERVICE -- getEvents -- END");
        return response;
    }

    private EventResponseDataModel toResponseData(EventEntity eventEntity) {
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
