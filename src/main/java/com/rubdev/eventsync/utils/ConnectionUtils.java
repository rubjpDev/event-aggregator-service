package com.rubdev.eventsync.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConnectionUtils {
    CACHE_KEY("provider:events"),
    ONLINE_MODE("online");

    private final String value;
}
