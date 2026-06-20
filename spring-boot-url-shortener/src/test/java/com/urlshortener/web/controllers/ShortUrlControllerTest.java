package com.urlshortener.web.controllers;

import com.urlshortener.ApplicationProperties;
import com.urlshortener.domain.exceptions.InvalidOriginalUrlException;
import com.urlshortener.domain.exceptions.ShortKeyAlreadyExistsException;
import com.urlshortener.domain.models.CreateShortUrlCmd;
import com.urlshortener.domain.models.PagedResult;
import com.urlshortener.domain.models.ShortUrlDto;
import com.urlshortener.domain.models.UserUrlSummary;
import com.urlshortener.domain.services.ShortUrlService;
import com.urlshortener.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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

    @Test
    void invalidCustomAliasRendersFormError() throws Exception {
        mockPublicUrls();

        mockMvc.perform(post("/short-urls")
                        .param("originalUrl", "https://example.com/my-page")
                        .param("customAlias", "bad alias"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("createShortUrlForm", "customAlias"));

        verify(shortUrlService, never()).createShortUrl(any(CreateShortUrlCmd.class));
    }

    @Test
    void takenCustomAliasRendersFriendlyFormError() throws Exception {
        mockPublicUrls();
        when(securityUtils.getCurrentUserId()).thenReturn(null);
        when(shortUrlService.createShortUrl(any(CreateShortUrlCmd.class)))
                .thenThrow(new ShortKeyAlreadyExistsException("my-link"));

        mockMvc.perform(post("/short-urls")
                        .param("originalUrl", "https://example.com/my-page")
                        .param("customAlias", "my-link"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("createShortUrlForm", "customAlias"));
    }

    @Test
    void createdShortUrlRedirectsWithShareableResultCardData() throws Exception {
        when(properties.baseUrl()).thenReturn("http://localhost:8080");
        when(securityUtils.getCurrentUserId()).thenReturn(null);
        when(shortUrlService.createShortUrl(any(CreateShortUrlCmd.class))).thenReturn(
                new ShortUrlDto(
                        1L,
                        "abc123",
                        "https://example.com/my-page",
                        false,
                        null,
                        null,
                        0L,
                        Instant.parse("2026-06-18T00:00:00Z")
                )
        );

        mockMvc.perform(post("/short-urls")
                        .param("originalUrl", "https://example.com/my-page"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("successMessage", "Short URL created successfully."))
                .andExpect(flash().attribute("shortUrlResult", "http://localhost:8080/s/abc123"))
                .andExpect(flash().attribute("shortUrlOriginalUrl", "https://example.com/my-page"));
    }

    @Test
    void myUrlsPageIncludesTheCurrentUsersLinks() throws Exception {
        Long userId = 7L;
        PagedResult<ShortUrlDto> urls = new PagedResult<>(
                List.of(), 1, 0, 0, true, true, false, false
        );
        UserUrlSummary summary = new UserUrlSummary(0L, 0L, 0L);

        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(properties.pageSize()).thenReturn(10);
        when(properties.baseUrl()).thenReturn("http://localhost:8080");
        when(shortUrlService.getUserShortUrls(userId, 1, 10)).thenReturn(urls);
        when(shortUrlService.getUserUrlSummary(userId)).thenReturn(summary);

        mockMvc.perform(get("/my-urls"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-urls"))
                .andExpect(model().attribute("shortUrls", urls))
                .andExpect(model().attribute("urlSummary", summary))
                .andExpect(model().attribute("baseUrl", "http://localhost:8080"));
    }

    private void mockPublicUrls() {
        when(properties.pageSize()).thenReturn(10);
        when(shortUrlService.findAllPublicShortUrls(1, 10)).thenReturn(
                new PagedResult<>(List.of(), 1, 0, 0, true, true, false, false)
        );
    }
}
