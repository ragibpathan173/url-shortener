package com.urlshortener.domain.models;

public record UserUrlSummary(
        Long totalLinks,
        Long activeLinks,
        Long totalClicks
) {
}
