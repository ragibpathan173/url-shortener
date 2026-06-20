package com.urlshortener.domain.services;

import com.urlshortener.ApplicationProperties;
import com.urlshortener.domain.entities.ShortUrl;
import com.urlshortener.domain.exceptions.InvalidOriginalUrlException;
import com.urlshortener.domain.exceptions.ShortKeyAlreadyExistsException;
import com.urlshortener.domain.models.CreateShortUrlCmd;
import com.urlshortener.domain.models.LinkSortOption;
import com.urlshortener.domain.models.LinkStatusFilter;
import com.urlshortener.domain.models.PagedResult;
import com.urlshortener.domain.models.ShortUrlDto;
import com.urlshortener.domain.models.UpdateShortUrlCmd;
import com.urlshortener.domain.models.UserUrlSummary;
import com.urlshortener.domain.repositories.ShortUrlRepository;
import com.urlshortener.domain.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.urlshortener.domain.services.RandomUtils.generateRandomShortKey;
import static java.time.temporal.ChronoUnit.*;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties properties;
    private final UserRepository userRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository,
                           EntityMapper entityMapper,
                           ApplicationProperties properties, UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.properties = properties;
        this.userRepository = userRepository;
    }

    public PagedResult<ShortUrlDto> findAllPublicShortUrls(int pageNo, int pageSize) {
        Pageable pageable = getPageable(pageNo, pageSize);
        Page<ShortUrlDto> shortUrlDtoPage = shortUrlRepository.findPublicShortUrls(pageable)
                .map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlDtoPage);
    }

    public PagedResult<ShortUrlDto> getUserShortUrls(
            Long userId,
            int page,
            int pageSize,
            String search,
            LinkStatusFilter statusFilter,
            LinkSortOption sortOption
    ) {
        LinkStatusFilter effectiveStatusFilter = statusFilter == null ? LinkStatusFilter.ALL : statusFilter;
        LinkSortOption effectiveSortOption = sortOption == null ? LinkSortOption.NEWEST : sortOption;
        Pageable pageable = getPageable(page, pageSize, effectiveSortOption);
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        boolean includeActive = effectiveStatusFilter != LinkStatusFilter.EXPIRED;
        boolean includeExpired = effectiveStatusFilter != LinkStatusFilter.ACTIVE;
        var shortUrlsPage = shortUrlRepository.searchUserShortUrls(
                        userId,
                        normalizedSearch,
                        includeActive,
                        includeExpired,
                        pageable
                )
                .map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlsPage);
    }

    public UserUrlSummary getUserUrlSummary(Long userId) {
        return shortUrlRepository.getUserUrlSummary(userId);
    }

    public Optional<ShortUrlDto> findUserShortUrl(Long shortUrlId, Long userId) {
        if (shortUrlId == null || userId == null) {
            return Optional.empty();
        }

        return shortUrlRepository.findByIdAndCreatedById(shortUrlId, userId)
                .map(entityMapper::toShortUrlDto);
    }

    @Transactional
    public void deleteUserShortUrls(List<Long> ids, Long userId) {
        if (ids != null && !ids.isEmpty() && userId != null) {
            shortUrlRepository.deleteByIdInAndCreatedById(ids, userId);
        }
    }

    public PagedResult<ShortUrlDto> findAllShortUrls(int page, int pageSize) {
        Pageable pageable = getPageable(page, pageSize);
        var shortUrlsPage =  shortUrlRepository.findAllShortUrls(pageable).map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlsPage);
    }

    private Pageable getPageable(int page, int size) {
        return getPageable(page, size, LinkSortOption.NEWEST);
    }

    @Transactional
    public Optional<ShortUrlDto> updateUserShortUrl(Long shortUrlId, UpdateShortUrlCmd command) {
        if (shortUrlId == null || command.userId() == null) {
            return Optional.empty();
        }

        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByIdAndCreatedById(shortUrlId, command.userId());
        if (shortUrlOptional.isEmpty()) {
            return Optional.empty();
        }

        if (properties.validateOriginalUrl() && !UrlExistenceValidator.isUrlExists(command.originalUrl())) {
            throw new InvalidOriginalUrlException("Original URL could not be verified: " + command.originalUrl());
        }

        ShortUrl shortUrl = shortUrlOptional.get();
        shortUrl.setOriginalUrl(command.originalUrl());
        shortUrl.setIsPrivate(Boolean.TRUE.equals(command.isPrivate()));
        shortUrl.setExpiresAt(command.expiresOn() == null
                ? null
                : command.expiresOn().atStartOfDay(ZoneOffset.UTC).toInstant());
        shortUrlRepository.save(shortUrl);
        return Optional.of(entityMapper.toShortUrlDto(shortUrl));
    }

    private Pageable getPageable(int page, int size, LinkSortOption sortOption) {
        page = page > 1 ? page - 1: 0;
        Sort sort = switch (sortOption) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case OLDEST -> Sort.by(Sort.Direction.ASC, "createdAt");
            case MOST_CLICKED -> Sort.by(Sort.Direction.DESC, "clickCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        };
        return PageRequest.of(page, size, sort);
    }

    @Transactional
    public ShortUrlDto createShortUrl(CreateShortUrlCmd cmd) {
        if(properties.validateOriginalUrl()) {
            boolean urlExists = UrlExistenceValidator.isUrlExists(cmd.originalUrl());
            if(!urlExists) {
                throw new InvalidOriginalUrlException("Original URL could not be verified: " + cmd.originalUrl());
            }
        }
        var shortKey = cmd.customAlias() != null ? getAvailableCustomAlias(cmd.customAlias()) : generateUniqueShortKey();
        var shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(cmd.originalUrl());
        shortUrl.setShortKey(shortKey);
        if(cmd.userId() == null) {
            shortUrl.setCreatedBy(null);
            shortUrl.setIsPrivate(false);
            shortUrl.setExpiresAt(Instant.now().plus(properties.defaultExpiryInDays(), DAYS));
        } else {
            shortUrl.setCreatedBy(userRepository.findById(cmd.userId()).orElseThrow());
            shortUrl.setIsPrivate(cmd.isPrivate() != null && cmd.isPrivate());
            shortUrl.setExpiresAt(cmd.expirationInDays() != null ? Instant.now().plus(cmd.expirationInDays(), DAYS) : null);
        }
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        try {
            shortUrlRepository.saveAndFlush(shortUrl);
        } catch (DataIntegrityViolationException ex) {
            if (cmd.customAlias() != null) {
                throw new ShortKeyAlreadyExistsException(cmd.customAlias());
            }
            throw ex;
        }
        return entityMapper.toShortUrlDto(shortUrl);
    }

    @Transactional
    public Optional<ShortUrlDto> accessShortUrl(String shortKey, Long userId) {
        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByShortKey(shortKey);
        if(shortUrlOptional.isEmpty()) {
            return Optional.empty();
        }
        ShortUrl shortUrl = shortUrlOptional.get();
        if(shortUrl.getExpiresAt() != null && !shortUrl.getExpiresAt().isAfter(Instant.now())) {
            return Optional.empty();
        }
        if(shortUrl.getIsPrivate() != null && shortUrl.getIsPrivate()
                && shortUrl.getCreatedBy() != null
                && !Objects.equals(shortUrl.getCreatedBy().getId(), userId)) {
            return Optional.empty();
        }
        shortUrl.setClickCount(shortUrl.getClickCount()+1);
        shortUrlRepository.save(shortUrl);
        return shortUrlOptional.map(entityMapper::toShortUrlDto);
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do {
            shortKey = generateRandomShortKey();
        } while (shortUrlRepository.existsByShortKey(shortKey));
        return shortKey;
    }

    private String getAvailableCustomAlias(String customAlias) {
        if (shortUrlRepository.existsByShortKey(customAlias)) {
            throw new ShortKeyAlreadyExistsException(customAlias);
        }
        return customAlias;
    }
}
