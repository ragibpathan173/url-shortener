package com.urlshortener.domain.repositories;

import com.urlshortener.domain.entities.ShortUrl;
import com.urlshortener.domain.entities.User;
import com.urlshortener.domain.models.Role;
import com.urlshortener.domain.models.UserUrlSummary;
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

    @Autowired
    private UserRepository userRepository;

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

    @Test
    void calculatesSummaryForUsersLinks() {
        User user = userRepository.save(createUser());
        shortUrlRepository.save(createShortUrl("active02", false, Instant.now().plusSeconds(3600), user, 12L));
        shortUrlRepository.save(createShortUrl("forever02", false, null, user, 4L));
        shortUrlRepository.save(createShortUrl("expired02", false, Instant.now().minusSeconds(3600), user, 2L));

        UserUrlSummary summary = shortUrlRepository.getUserUrlSummary(user.getId());

        assertThat(summary.totalLinks()).isEqualTo(3L);
        assertThat(summary.activeLinks()).isEqualTo(2L);
        assertThat(summary.totalClicks()).isEqualTo(18L);
    }

    private ShortUrl createShortUrl(String shortKey, boolean isPrivate, Instant expiresAt) {
        return createShortUrl(shortKey, isPrivate, expiresAt, null, 0L);
    }

    private ShortUrl createShortUrl(
            String shortKey,
            boolean isPrivate,
            Instant expiresAt,
            User createdBy,
            Long clickCount
    ) {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortKey(shortKey);
        shortUrl.setOriginalUrl("https://example.com/" + shortKey);
        shortUrl.setIsPrivate(isPrivate);
        shortUrl.setExpiresAt(expiresAt);
        shortUrl.setCreatedBy(createdBy);
        shortUrl.setClickCount(clickCount);
        shortUrl.setCreatedAt(Instant.now());
        return shortUrl;
    }

    private User createUser() {
        User user = new User();
        user.setEmail("summary@example.com");
        user.setPassword("password");
        user.setName("Summary User");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(Instant.now());
        return user;
    }
}
