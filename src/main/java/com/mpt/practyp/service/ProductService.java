package com.mpt.practyp.service;

import com.mpt.practyp.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProductService {
    Page<Product> findAll(Pageable pageable, boolean includeDeleted);
    Page<Product> searchByName(String name, Long categoryId, Pageable pageable, boolean includeDeleted);
    Product findById(Long id);
    Product create(Product product);
    Product update(Product product);
    void softDelete(Long id);
    void hardDelete(Long id);

    List<Product> findAll();
    Optional<Product> findByIdOptional(Long id);
    boolean existsById(Long id);
    Product save(Product product);
    void deleteById(Long id);
    List<Product> searchByName(String name);

    @Async
    CompletableFuture<Page<Product>> findAllAsync(Pageable pageable, boolean includeDeleted);

    @Async
    CompletableFuture<Optional<Product>> findByIdAsync(Long id);

    @Async
    CompletableFuture<Product> saveAsync(Product product);

    @Async
    CompletableFuture<List<Product>> findAllAsync();

    @Async
    CompletableFuture<Boolean> existsByIdAsync(Long id);

    @Async
    CompletableFuture<Void> deleteByIdAsync(Long id);

    @Async
    CompletableFuture<Long> countAsync();

    @Async
    CompletableFuture<List<Product>> searchByNameAsync(String name);
}