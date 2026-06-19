package com.urlshortener.domain.repositories;

import com.urlshortener.domain.entities.ShortUrl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ShortUrlRepositoryTest {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Test
    void findPublicShortUrlsExcludesExpiredAndPrivateLinks() {
        shortUrlRepository.save(createShortUrl("active01", false, Instant.now().plusSeconds(3600)));
        shortUrlRepository.save(createShortUrl("forever01", false, null));
        shortUrlRepository.save(createShortUrl("expired01", false, Instant.now().minusSeconds(3600)));
        shortUrlRepository.save(createShortUrl("private01", true, null));

        var publicUrls = shortUrlRepository.findPublicShortUrls(PageRequest.of(0, 10));

        assertThat(publicUrls.getContent())
                .extracting(ShortUrl::getShortKey)
                .containsExactlyInAnyOrder("active01", "forever01");
    }

    @Test
    void rejectsDuplicateShortKeys() {
        shortUrlRepository.saveAndFlush(createShortUrl("same-key", false, null));

        assertThatThrownBy(() -> shortUrlRepository.saveAndFlush(createShortUrl("same-key", false, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private ShortUrl createShortUrl(String shortKey, boolean isPrivate, Instant expiresAt) {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortKey(shortKey);
        shortUrl.setOriginalUrl("https://example.com/" + shortKey);
        shortUrl.setIsPrivate(isPrivate);
        shortUrl.setExpiresAt(expiresAt);
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        return shortUrl;
    }
}
