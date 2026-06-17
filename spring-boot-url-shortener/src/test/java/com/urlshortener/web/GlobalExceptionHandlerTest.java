package com.urlshortener.web;

import com.urlshortener.domain.exceptions.ShortUrlNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesMissingShortUrlWithNotFoundViewAndMessage() {
        var model = new ConcurrentModel();

        String viewName = handler.handleShortUrlNotFoundException(
                new ShortUrlNotFoundException("Invalid short key: missing"),
                model
        );

        assertThat(viewName).isEqualTo("error/404");
        assertThat(model.getAttribute("errorCode")).isEqualTo("404");
        assertThat(model.getAttribute("errorTitle")).isEqualTo("Short link not found");
        assertThat((String) model.getAttribute("errorMessage")).contains("expired");
    }

    @Test
    void handlesUnexpectedErrorsWithServerErrorViewAndMessage() {
        var model = new ConcurrentModel();

        String viewName = handler.handleException(new RuntimeException("boom"), model);

        assertThat(viewName).isEqualTo("error/500");
        assertThat(model.getAttribute("errorCode")).isEqualTo("500");
        assertThat(model.getAttribute("errorTitle")).isEqualTo("Something went wrong");
        assertThat((String) model.getAttribute("errorMessage")).contains("could not process");
    }
}
