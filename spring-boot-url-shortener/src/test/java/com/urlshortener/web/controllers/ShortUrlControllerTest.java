package com.urlshortener.web.controllers;

import com.urlshortener.ApplicationProperties;
import com.urlshortener.domain.exceptions.InvalidOriginalUrlException;
import com.urlshortener.domain.models.CreateShortUrlCmd;
import com.urlshortener.domain.models.PagedResult;
import com.urlshortener.domain.services.ShortUrlService;
import com.urlshortener.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ShortUrlController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @MockitoBean
    private ApplicationProperties properties;

    @MockitoBean
    private SecurityUtils securityUtils;

    @Test
    void missingShortKeyReturnsNotFoundPage() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(null);
        when(shortUrlService.accessShortUrl("missing", null)).thenReturn(Optional.empty());

        mockMvc.perform(get("/s/missing"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void invalidOriginalUrlRendersFormError() throws Exception {
        mockPublicUrls();

        mockMvc.perform(post("/short-urls")
                        .param("originalUrl", "not-a-url"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("createShortUrlForm", "originalUrl"));

        verify(shortUrlService, never()).createShortUrl(any(CreateShortUrlCmd.class));
    }

    @Test
    void unverifiedOriginalUrlRendersFriendlyFormError() throws Exception {
        mockPublicUrls();
        when(securityUtils.getCurrentUserId()).thenReturn(null);
        when(shortUrlService.createShortUrl(any(CreateShortUrlCmd.class)))
                .thenThrow(new InvalidOriginalUrlException("URL not reachable"));

        mockMvc.perform(post("/short-urls")
                        .param("originalUrl", "https://example.com/my-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("createShortUrlForm", "originalUrl"));
    }

    private void mockPublicUrls() {
        when(properties.pageSize()).thenReturn(10);
        when(shortUrlService.findAllPublicShortUrls(1, 10)).thenReturn(
                new PagedResult<>(List.of(), 1, 0, 0, true, true, false, false)
        );
    }
}
