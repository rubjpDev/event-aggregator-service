package com.rubdev.eventsync.model.response;

import lombok.Data;

import java.util.List;

@Data
public class EventResponseModel {
    private DataWrapper data;

    @Data
    public static class DataWrapper {
       private List<EventResponseDataModel> events;
    }
}
