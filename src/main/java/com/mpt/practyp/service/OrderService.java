package com.mpt.practyp.service;

import com.mpt.practyp.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface OrderService {
    Page<Order> findAll(boolean includeDeleted, Pageable pageable);
    Order findById(Long id);
    Order create(Order order);
    Order update(Order order);
    void softDelete(Long id);
    void hardDelete(Long id);

    List<Order> findByCustomerId(Long customerId);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    List<Order> findAll();
    Optional<Order> findByIdOptional(Long id);
    boolean existsById(Long id);
    Order save(Order order);
    void deleteById(Long id);

    @Async
    CompletableFuture<Page<Order>> findAllAsync(boolean includeDeleted, Pageable pageable);

    @Async
    CompletableFuture<Optional<Order>> findByIdAsync(Long id);

    @Async
    CompletableFuture<Order> saveAsync(Order order);

    @Async
    CompletableFuture<List<Order>> findAllAsync();

    @Async
    CompletableFuture<Boolean> existsByIdAsync(Long id);

    @Async
    CompletableFuture<Void> deleteByIdAsync(Long id);

    @Async
    CompletableFuture<Long> countAsync();

    @Async
    CompletableFuture<List<Order>> findByCustomerIdAsync(Long customerId);

    @Async
    CompletableFuture<Page<Order>> findByCustomerIdAsync(Long customerId, Pageable pageable);
}