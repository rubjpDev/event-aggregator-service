package com.rubdev.eventsync.model.service;

import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.model.response.EventResponseModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventResponseModel getEvents(LocalDateTime startDate, LocalDateTime endDate);
}
