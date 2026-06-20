package com.urlshortener.domain.models;

import java.util.Arrays;

public enum LinkStatusFilter {
    ALL,
    ACTIVE,
    EXPIRED;

    public static LinkStatusFilter from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }

        return Arrays.stream(values())
                .filter(filter -> filter.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(ALL);
    }
}
