package com.rubdev.eventsync.controller;

import com.rubdev.eventsync.model.response.EventResponseDataModel;
import com.rubdev.eventsync.model.response.EventResponseModel;
import com.rubdev.eventsync.model.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private EventResponseModel mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventController = new EventController(eventService);
        EventResponseDataModel event = new EventResponseDataModel();
        event.setId(1L);
        event.setTitle("test");
        event.setMinPrice(1.0);
        event.setMaxPrice(2.0);

        EventResponseModel.DataWrapper wrapper = new EventResponseModel.DataWrapper();
        wrapper.setEvents(List.of(event));

        mockResponse = new EventResponseModel();
        mockResponse.setData(wrapper);
    }

    @Test
    void testGetEvents_ReturnsValidResponse() {
        when(eventService.getEvents(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponse);

        LocalDateTime start = LocalDateTime.of(2021, 7, 31, 20, 0);
        LocalDateTime end = LocalDateTime.of(2021, 7, 31, 21, 0);
        ResponseEntity<EventResponseModel> response = eventController.getEvents(start,end);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getEvents()).hasSize(1);
        assertThat(response.getBody().getData().getEvents().getFirst().getTitle())
                .isEqualTo("test");
    }
}
