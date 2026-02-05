package com.mpt.practyp.service;

import com.mpt.practyp.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TagService {
    Page<Tag> findAll(boolean includeDeleted, Pageable pageable);
    Tag findById(Long id);
    Tag create(Tag tag);
    Tag update(Tag tag);
    void softDelete(Long id);
    void hardDelete(Long id);
    void restore(Long id);
    Page<Tag> search(String keyword, boolean includeDeleted, Pageable pageable);

    List<Tag> findAll();
    Optional<Tag> findByIdOptional(Long id);
    boolean existsById(Long id);
    Tag save(Tag tag);
    void deleteById(Long id);

    @Async
    CompletableFuture<Page<Tag>> findAllAsync(boolean includeDeleted, Pageable pageable);

    @Async
    CompletableFuture<Optional<Tag>> findByIdAsync(Long id);

    @Async
    CompletableFuture<Tag> saveAsync(Tag tag);

    @Async
    CompletableFuture<List<Tag>> findAllAsync();

    @Async
    CompletableFuture<Boolean> existsByIdAsync(Long id);

    @Async
    CompletableFuture<Void> deleteByIdAsync(Long id);

    @Async
    CompletableFuture<Long> countAsync();

    @Async
    CompletableFuture<Page<Tag>> searchAsync(String keyword, boolean includeDeleted, Pageable pageable);
}