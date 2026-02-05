package com.mpt.practyp.service;

import com.mpt.practyp.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CategoryService {
    Page<Category> findAll(boolean includeDeleted, Pageable pageable);
    Category findById(Long id);
    Category create(Category category);
    Category update(Category category);
    void softDelete(Long id);
    void hardDelete(Long id);
    void restore(Long id);
    Page<Category> search(String keyword, boolean includeDeleted, Pageable pageable);

    List<Category> findAll();
    Optional<Category> findByIdOptional(Long id);
    boolean existsById(Long id);
    Category save(Category category);
    void deleteById(Long id);

    @Async
    CompletableFuture<Page<Category>> findAllAsync(boolean includeDeleted, Pageable pageable);

    @Async
    CompletableFuture<Optional<Category>> findByIdAsync(Long id);

    @Async
    CompletableFuture<Category> saveAsync(Category category);

    @Async
    CompletableFuture<List<Category>> findAllAsync();

    @Async
    CompletableFuture<Boolean> existsByIdAsync(Long id);

    @Async
    CompletableFuture<Void> deleteByIdAsync(Long id);

    @Async
    CompletableFuture<Long> countAsync();
}