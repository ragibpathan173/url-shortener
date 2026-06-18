package com.urlshortener.web.dtos;


import com.urlshortener.web.validation.HttpUrl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateShortUrlForm(
        @NotBlank(message = "Original URL is required")
        @HttpUrl
        String originalUrl,
        Boolean isPrivate,
        @Min(value = 1, message = "Expiration must be at least 1 day")
        @Max(value = 365, message = "Expiration cannot be more than 365 days")
        Integer expirationInDays
        ) {
    public CreateShortUrlForm {
        originalUrl = originalUrl == null ? null : originalUrl.trim();
    }
}
