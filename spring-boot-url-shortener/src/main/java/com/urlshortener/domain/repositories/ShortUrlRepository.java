package com.urlshortener.domain.repositories;

import com.urlshortener.domain.entities.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    @Query(
            value = "select su from ShortUrl su left join fetch su.createdBy " +
                    "where su.isPrivate = false and (su.expiresAt is null or su.expiresAt > CURRENT_TIMESTAMP)",
            countQuery = "select count(su) from ShortUrl su " +
                    "where su.isPrivate = false and (su.expiresAt is null or su.expiresAt > CURRENT_TIMESTAMP)"
    )
    Page<ShortUrl> findPublicShortUrls(Pageable pageable);

    boolean existsByShortKey(String shortKey);

    Optional<ShortUrl> findByShortKey(String shortKey);

    Page<ShortUrl> findByCreatedById(Long userId, Pageable pageable);

    @Query(
            value = "select su from ShortUrl su left join fetch su.createdBy " +
                    "where su.createdBy.id = :userId " +
                    "and (:search is null or lower(su.shortKey) like lower(concat('%', :search, '%')) " +
                    "or lower(cast(su.originalUrl as string)) like lower(concat('%', :search, '%'))) " +
                    "and ((:includeActive = true and (su.expiresAt is null or su.expiresAt > CURRENT_TIMESTAMP)) " +
                    "or (:includeExpired = true and su.expiresAt <= CURRENT_TIMESTAMP))",
            countQuery = "select count(su) from ShortUrl su " +
                    "where su.createdBy.id = :userId " +
                    "and (:search is null or lower(su.shortKey) like lower(concat('%', :search, '%')) " +
                    "or lower(cast(su.originalUrl as string)) like lower(concat('%', :search, '%'))) " +
                    "and ((:includeActive = true and (su.expiresAt is null or su.expiresAt > CURRENT_TIMESTAMP)) " +
                    "or (:includeExpired = true and su.expiresAt <= CURRENT_TIMESTAMP))"
    )
    Page<ShortUrl> searchUserShortUrls(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("includeActive") boolean includeActive,
            @Param("includeExpired") boolean includeExpired,
            Pageable pageable
    );

    @Query("""
            select new com.urlshortener.domain.models.UserUrlSummary(
                count(su),
                coalesce(sum(case when su.expiresAt is null or su.expiresAt > CURRENT_TIMESTAMP then 1 else 0 end), 0),
                coalesce(sum(su.clickCount), 0)
            )
            from ShortUrl su
            where su.createdBy.id = :userId
            """)
    com.urlshortener.domain.models.UserUrlSummary getUserUrlSummary(@Param("userId") Long userId);

    @Modifying
    void deleteByIdInAndCreatedById(List<Long> ids, Long userId);

    @Query(
            value = "select u from ShortUrl u left join fetch u.createdBy",
            countQuery = "select count(u) from ShortUrl u"
    )
    Page<ShortUrl> findAllShortUrls(Pageable pageable);
}
