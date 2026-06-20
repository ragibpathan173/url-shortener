package com.urlshortener.domain.models;

import java.util.Arrays;

public enum LinkSortOption {
    NEWEST,
    OLDEST,
    MOST_CLICKED;

    public static LinkSortOption from(String value) {
        if (value == null || value.isBlank()) {
            return NEWEST;
        }

        return Arrays.stream(values())
                .filter(option -> option.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(NEWEST);
    }
}
