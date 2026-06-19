package com.urlshortener.domain.services;

import com.urlshortener.ApplicationProperties;
import com.urlshortener.domain.entities.ShortUrl;
import com.urlshortener.domain.exceptions.ShortKeyAlreadyExistsException;
import com.urlshortener.domain.models.CreateShortUrlCmd;
import com.urlshortener.domain.models.ShortUrlDto;
import com.urlshortener.domain.repositories.ShortUrlRepository;
import com.urlshortener.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShortUrlService shortUrlService;

    @Test
    void createsShortUrlWithAvailableCustomAlias() {
        CreateShortUrlCmd command = new CreateShortUrlCmd(
                "https://example.com/page", "my-link", false, null, null
        );
        ShortUrlDto expected = new ShortUrlDto(
                1L, "my-link", command.originalUrl(), false, null, null, 0L, Instant.now()
        );
        ArgumentCaptor<ShortUrl> shortUrlCaptor = ArgumentCaptor.forClass(ShortUrl.class);

        when(properties.validateOriginalUrl()).thenReturn(false);
        when(shortUrlRepository.existsByShortKey("my-link")).thenReturn(false);
        when(entityMapper.toShortUrlDto(any(ShortUrl.class))).thenReturn(expected);

        ShortUrlDto result = shortUrlService.createShortUrl(command);

        verify(shortUrlRepository).save(shortUrlCaptor.capture());
        assertThat(shortUrlCaptor.getValue().getShortKey()).isEqualTo("my-link");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void rejectsTakenCustomAlias() {
        CreateShortUrlCmd command = new CreateShortUrlCmd(
                "https://example.com/page", "my-link", false, null, null
        );

        when(properties.validateOriginalUrl()).thenReturn(false);
        when(shortUrlRepository.existsByShortKey("my-link")).thenReturn(true);

        assertThatThrownBy(() -> shortUrlService.createShortUrl(command))
                .isInstanceOf(ShortKeyAlreadyExistsException.class);
    }
}
