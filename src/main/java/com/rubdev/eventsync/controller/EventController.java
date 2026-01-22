package com.rubdev.eventsync.controller;


import com.rubdev.eventsync.model.response.EventResponseModel;
import com.rubdev.eventsync.model.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;


    @GetMapping("/search")
    public ResponseEntity<EventResponseModel> getEvents(
            @RequestParam(name = "starts_at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "ends_at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate){
     return ResponseEntity.ok(this.eventService.getEvents(startDate,endDate));
    }
}
