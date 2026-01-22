package com.rubdev.eventsync.model.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EventResponseDataModel {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double minPrice;
    private Double maxPrice;
}
