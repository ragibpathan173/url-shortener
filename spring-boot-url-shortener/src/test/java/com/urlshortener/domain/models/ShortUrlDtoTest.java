package com.urlshortener.domain.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUrlDtoTest {

    @Test
    void identifiesExpiredLinks() {
        ShortUrlDto expiredUrl = shortUrl("expired", Instant.now().minusSeconds(60));
        ShortUrlDto justExpiredUrl = shortUrl("just-expired", Instant.now());
        ShortUrlDto activeUrl = shortUrl("active", Instant.now().plusSeconds(60));
        ShortUrlDto noExpiryUrl = shortUrl("no-expiry", null);

        assertThat(expiredUrl.isExpired()).isTrue();
        assertThat(justExpiredUrl.isExpired()).isTrue();
        assertThat(activeUrl.isExpired()).isFalse();
        assertThat(noExpiryUrl.isExpired()).isFalse();
    }

    private ShortUrlDto shortUrl(String shortKey, Instant expiresAt) {
        return new ShortUrlDto(
                1L,
                shortKey,
                "https://example.com/" + shortKey,
                false,
                expiresAt,
                null,
                0L,
                Instant.now()
        );
    }
}
