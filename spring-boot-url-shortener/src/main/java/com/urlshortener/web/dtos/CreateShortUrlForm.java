package com.urlshortener.web.dtos;


import com.urlshortener.web.validation.HttpUrl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateShortUrlForm(
        @NotBlank(message = "Original URL is required")
        @HttpUrl
        String originalUrl,
        @Pattern(regexp = "[A-Za-z0-9_-]{4,10}", message = "Use 4-10 letters, numbers, hyphens, or underscores")
        String customAlias,
        Boolean isPrivate,
        @Min(value = 1, message = "Expiration must be at least 1 day")
        @Max(value = 365, message = "Expiration cannot be more than 365 days")
        Integer expirationInDays
        ) {
    public CreateShortUrlForm {
        originalUrl = originalUrl == null ? null : originalUrl.trim();
        customAlias = customAlias == null || customAlias.isBlank() ? null : customAlias.trim();
    }
}
