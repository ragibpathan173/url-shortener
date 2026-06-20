package com.urlshortener.domain.services;

import com.urlshortener.ApplicationProperties;
import com.urlshortener.domain.entities.ShortUrl;
import com.urlshortener.domain.exceptions.ShortKeyAlreadyExistsException;
import com.urlshortener.domain.models.CreateShortUrlCmd;
import com.urlshortener.domain.models.LinkSortOption;
import com.urlshortener.domain.models.LinkStatusFilter;
import com.urlshortener.domain.models.PagedResult;
import com.urlshortener.domain.models.ShortUrlDto;
import com.urlshortener.domain.models.UpdateShortUrlCmd;
import com.urlshortener.domain.repositories.ShortUrlRepository;
import com.urlshortener.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

        verify(shortUrlRepository).saveAndFlush(shortUrlCaptor.capture());
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

    @Test
    void translatesDatabaseAliasCollisionsToDomainException() {
        CreateShortUrlCmd command = new CreateShortUrlCmd(
                "https://example.com/page", "my-link", false, null, null
        );

        when(properties.validateOriginalUrl()).thenReturn(false);
        when(shortUrlRepository.existsByShortKey("my-link")).thenReturn(false);
        when(shortUrlRepository.saveAndFlush(any(ShortUrl.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate short key"));

        assertThatThrownBy(() -> shortUrlService.createShortUrl(command))
                .isInstanceOf(ShortKeyAlreadyExistsException.class);
    }

    @Test
    void sortsUsersLinksByClicksWhenRequested() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortKey("popular01");
        ShortUrlDto expected = new ShortUrlDto(
                1L, "popular01", "https://example.com/popular", false, null, null, 9L, Instant.now()
        );
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(shortUrlRepository.searchUserShortUrls(
                eq(7L), eq(null), eq(true), eq(true), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(shortUrl)));
        when(entityMapper.toShortUrlDto(shortUrl)).thenReturn(expected);

        PagedResult<ShortUrlDto> result = shortUrlService.getUserShortUrls(
                7L, 1, 10, null, LinkStatusFilter.ALL, LinkSortOption.MOST_CLICKED
        );

        verify(shortUrlRepository).searchUserShortUrls(
                eq(7L), eq(null), eq(true), eq(true), pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("clickCount").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
        assertThat(result.data()).containsExactly(expected);
    }

    @Test
    void updatesOnlyTheOwnersLinkSettings() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl("https://example.com/old");
        shortUrl.setIsPrivate(false);
        ShortUrlDto expected = new ShortUrlDto(
                1L, "private01", "https://example.com/new", true, null, null, 0L, Instant.now()
        );

        when(shortUrlRepository.findByIdAndCreatedById(1L, 7L)).thenReturn(java.util.Optional.of(shortUrl));
        when(entityMapper.toShortUrlDto(shortUrl)).thenReturn(expected);

        var updated = shortUrlService.updateUserShortUrl(
                1L,
                new UpdateShortUrlCmd("https://example.com/new", true, null, 7L)
        );

        verify(shortUrlRepository).save(shortUrl);
        assertThat(shortUrl.getOriginalUrl()).isEqualTo("https://example.com/new");
        assertThat(shortUrl.getIsPrivate()).isTrue();
        assertThat(shortUrl.getExpiresAt()).isNull();
        assertThat(updated).contains(expected);
    }

    @Test
    void returnsEmptyWhenAnotherUserTriesToUpdateALink() {
        when(shortUrlRepository.findByIdAndCreatedById(1L, 8L)).thenReturn(java.util.Optional.empty());

        var updated = shortUrlService.updateUserShortUrl(
                1L,
                new UpdateShortUrlCmd("https://example.com/new", false, LocalDate.now().plusDays(7), 8L)
        );

        assertThat(updated).isEmpty();
    }
}
