package com.urlshortener.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            boolean isHttpUrl = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            return isHttpUrl && host != null && !host.isBlank();
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
