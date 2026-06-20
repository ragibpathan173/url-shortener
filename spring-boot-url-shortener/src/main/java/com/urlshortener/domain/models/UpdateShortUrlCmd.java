package com.urlshortener.domain.models;

import java.time.LocalDate;

public record UpdateShortUrlCmd(
        String originalUrl,
        Boolean isPrivate,
        LocalDate expiresOn,
        Long userId
) {
}
