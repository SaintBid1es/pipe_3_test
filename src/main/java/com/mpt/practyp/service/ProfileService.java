package com.mpt.practyp.service;

import com.mpt.practyp.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProfileService {
    Page<Profile> findAll(boolean includeDeleted, Pageable pageable);
    Profile findById(Long id);
    Profile create(Profile profile);
    Profile update(Profile profile);
    void softDelete(Long id);
    void hardDelete(Long id);
    void restore(Long id);
    Page<Profile> search(String keyword, boolean includeDeleted, Pageable pageable);

    List<Profile> findAll();
    Optional<Profile> findByIdOptional(Long id);
    boolean existsById(Long id);
    Profile save(Profile profile);
    void deleteById(Long id);
    Optional<Profile> findByCustomerId(Long customerId);

    @Async
    CompletableFuture<Page<Profile>> findAllAsync(boolean includeDeleted, Pageable pageable);

    @Async
    CompletableFuture<Optional<Profile>> findByIdAsync(Long id);

    @Async
    CompletableFuture<Profile> saveAsync(Profile profile);

    @Async
    CompletableFuture<List<Profile>> findAllAsync();

    @Async
    CompletableFuture<Boolean> existsByIdAsync(Long id);

    @Async
    CompletableFuture<Void> deleteByIdAsync(Long id);

    @Async
    CompletableFuture<Long> countAsync();

    @Async
    CompletableFuture<Optional<Profile>> findByCustomerIdAsync(Long customerId);

    @Async
    CompletableFuture<Page<Profile>> searchAsync(String keyword, boolean includeDeleted, Pageable pageable);
}