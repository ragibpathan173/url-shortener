package com.urlshortener.domain.repositories;

import com.urlshortener.domain.entities.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    @Query(
            value = "select su from ShortUrl su left join fetch su.createdBy where su.isPrivate = false",
            countQuery = "select count(su) from ShortUrl su where su.isPrivate = false"
    )
    Page<ShortUrl> findPublicShortUrls(Pageable pageable);

    boolean existsByShortKey(String shortKey);

    Optional<ShortUrl> findByShortKey(String shortKey);

    Page<ShortUrl> findByCreatedById(Long userId, Pageable pageable);

    @Modifying
    void deleteByIdInAndCreatedById(List<Long> ids, Long userId);

    @Query(
            value = "select u from ShortUrl u left join fetch u.createdBy",
            countQuery = "select count(u) from ShortUrl u"
    )
    Page<ShortUrl> findAllShortUrls(Pageable pageable);
}
