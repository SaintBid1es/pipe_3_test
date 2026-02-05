package com.mpt.practyp.service;

import com.mpt.practyp.model.Customer;
import com.mpt.practyp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository repository;

    @Override
    public Page<Customer> findAll(boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findAll(pageable);
        return repository.findByDeletedFalse(pageable);
    }

    @Override
    public Customer findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Customer create(Customer customer) {
        return repository.save(customer);
    }

    @Override
    public Customer update(Customer customer) {
        return repository.save(customer);
    }

    @Override
    public void softDelete(Long id) {
        Customer customer = repository.findById(id).orElseThrow();
        customer.setDeleted(true);
        repository.save(customer);
    }

    @Override
    public void hardDelete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void restore(Long id) {
        Customer customer = repository.findById(id).orElseThrow();
        customer.setDeleted(false);
        repository.save(customer);
    }

    @Override
    public Page<Customer> search(String keyword, boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) {
            return repository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            return repository.findByFirstNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
        }
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Customer> findByIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Customer save(Customer customer) {
        return repository.save(customer);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Customer> search(String query) {
        return repository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                query, query, query, query);
    }

    @Async
    @Override
    public CompletableFuture<Page<Customer>> findAllAsync(boolean includeDeleted, Pageable pageable) {
        return CompletableFuture.completedFuture(findAll(includeDeleted, pageable));
    }

    @Async
    @Override
    public CompletableFuture<Optional<Customer>> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findByIdOptional(id));
    }

    @Async
    @Override
    public CompletableFuture<Customer> saveAsync(Customer customer) {
        return CompletableFuture.completedFuture(save(customer));
    }

    @Async
    @Override
    public CompletableFuture<List<Customer>> findAllAsync() {
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
    public CompletableFuture<List<Customer>> searchAsync(String query) {
        return CompletableFuture.completedFuture(search(query));
    }
}