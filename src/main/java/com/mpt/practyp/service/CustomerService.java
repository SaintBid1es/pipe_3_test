package com.mpt.practyp.service;

import com.mpt.practyp.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CustomerService {
    Page<Customer> findAll(boolean includeDeleted, Pageable pageable);
    Customer findById(Long id);
    Customer create(Customer customer);
    Customer update(Customer customer);
    void softDelete(Long id);
    void hardDelete(Long id);
    void restore(Long id);
    Page<Customer> search(String keyword, boolean includeDeleted, Pageable pageable);

    List<Customer> findAll();
    Optional<Customer> findByIdOptional(Long id);
    boolean existsById(Long id);
    Customer save(Customer customer);
    void deleteById(Long id);
    List<Customer> search(String query);

    @Async
    CompletableFuture<Page<Customer>> findAllAsync(boolean includeDeleted, Pageable pageable);

    @Async
    CompletableFuture<Optional<Customer>> findByIdAsync(Long id);

    @Async
    CompletableFuture<Customer> saveAsync(Customer customer);

    @Async
    CompletableFuture<List<Customer>> findAllAsync();

    @Async
    CompletableFuture<Boolean> existsByIdAsync(Long id);

    @Async
    CompletableFuture<Void> deleteByIdAsync(Long id);

    @Async
    CompletableFuture<Long> countAsync();

    @Async
    CompletableFuture<List<Customer>> searchAsync(String query);
}