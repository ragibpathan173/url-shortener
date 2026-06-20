package com.urlshortener.web.dtos;

import com.urlshortener.web.validation.HttpUrl;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record UpdateShortUrlForm(
        @NotBlank(message = "Destination URL is required")
        @HttpUrl
        String originalUrl,
        Boolean isPrivate,
        @Future(message = "Choose a future expiry date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate expiresOn
) {
    public UpdateShortUrlForm {
        originalUrl = originalUrl == null ? null : originalUrl.trim();
    }
}
