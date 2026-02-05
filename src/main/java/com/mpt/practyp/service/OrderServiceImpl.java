package com.mpt.practyp.service;

import com.mpt.practyp.model.Order;
import com.mpt.practyp.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository repository;

    @Override
    public Page<Order> findAll(boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findAll(pageable);
        return repository.findByDeletedFalse(pageable);
    }

    @Override
    public Order findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Order create(Order order) {
        order.calculateTotalAmount();
        return repository.save(order);
    }

    @Override
    public Order update(Order order) {
        order.calculateTotalAmount();
        return repository.save(order);
    }

    @Override
    public void softDelete(Long id) {
        Order order = repository.findById(id).orElseThrow();
        order.setDeleted(true);
        repository.save(order);
    }

    @Override
    public void hardDelete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public Page<Order> findByCustomerId(Long customerId, Pageable pageable) {
        return repository.findByCustomerIdAndDeletedFalse(customerId, pageable);
    }

    @Override
    public List<Order> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Order> findByIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Order save(Order order) {
        order.calculateTotalAmount();
        return repository.save(order);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Async
    @Override
    public CompletableFuture<Page<Order>> findAllAsync(boolean includeDeleted, Pageable pageable) {
        return CompletableFuture.completedFuture(findAll(includeDeleted, pageable));
    }

    @Async
    @Override
    public CompletableFuture<Optional<Order>> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findByIdOptional(id));
    }

    @Async
    @Override
    public CompletableFuture<Order> saveAsync(Order order) {
        order.calculateTotalAmount();
        return CompletableFuture.completedFuture(save(order));
    }

    @Async
    @Override
    public CompletableFuture<List<Order>> findAllAsync() {
        return CompletableFuture.completedFuture(findAll());
    }

    @Async
    @Override
    public CompletableFuture<Boolean> existsByIdAsync(Long id) {
        return CompletableFuture.completedFuture(existsById(id));
    }

    @Async
    @Override
    public CompletableFuture<Void> deleteByIdAsync(Long id) {
        return CompletableFuture.runAsync(() -> deleteById(id));
    }

    @Async
    @Override
    public CompletableFuture<Long> countAsync() {
        return CompletableFuture.completedFuture(repository.count());
    }

    @Async
    @Override
    public CompletableFuture<List<Order>> findByCustomerIdAsync(Long customerId) {
        return CompletableFuture.completedFuture(findByCustomerId(customerId));
    }

    @Async
    @Override
    public CompletableFuture<Page<Order>> findByCustomerIdAsync(Long customerId, Pageable pageable) {
        return CompletableFuture.completedFuture(findByCustomerId(customerId, pageable));
    }
}