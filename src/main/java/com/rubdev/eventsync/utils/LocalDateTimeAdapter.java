package com.rubdev.eventsync.utils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * THIS CLASS IS NEEDED BECAUSE XML ONLY UNDERSTANDS PRIMITIVE TYPES AND LOCALDATETIME OR SIMILAR ARE NOT, NEED PARSING
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    @Override
    public LocalDateTime unmarshal(String s) throws Exception {
        return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s, FORMATTER);
    }

    @Override
    public String marshal(LocalDateTime localDateTime) throws Exception {
        return (localDateTime == null) ? null : localDateTime.format(FORMATTER);
    }
}
