package com.urlshortener.domain.models;

public record CreateShortUrlCmd(
        String originalUrl,
        String customAlias,
        Boolean isPrivate,
        Integer expirationInDays,
        Long userId
) {
}
