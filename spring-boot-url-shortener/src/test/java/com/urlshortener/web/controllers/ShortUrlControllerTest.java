package com.urlshortener.web.controllers;

import com.urlshortener.ApplicationProperties;
import com.urlshortener.domain.services.ShortUrlService;
import com.urlshortener.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
