package com.mpt.practyp.service;

import com.mpt.practyp.model.Profile;
import com.mpt.practyp.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository repository;

    @Override
    public Page<Profile> findAll(boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findAll(pageable);
        return repository.findByDeletedFalse(pageable);
    }

    @Override
    public Profile findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Profile create(Profile profile) {
        return repository.save(profile);
    }

    @Override
    public Profile update(Profile profile) {
        return repository.save(profile);
    }

    @Override
    public void softDelete(Long id) {
        Profile profile = repository.findById(id).orElseThrow();
        profile.setDeleted(true);
        repository.save(profile);
    }

    @Override
    public void hardDelete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void restore(Long id) {
        Profile profile = repository.findById(id).orElseThrow();
        profile.setDeleted(false);
        repository.save(profile);
    }

    @Override
    public Page<Profile> search(String keyword, boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findByAddressContainingIgnoreCase(keyword, pageable);
        return repository.findByAddressContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
    }

    @Override
    public List<Profile> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Profile> findByIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Profile save(Profile profile) {
        return repository.save(profile);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Profile> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Async
    @Override
    public CompletableFuture<Page<Profile>> findAllAsync(boolean includeDeleted, Pageable pageable) {
        return CompletableFuture.completedFuture(findAll(includeDeleted, pageable));
    }

    @Async
    @Override
    public CompletableFuture<Optional<Profile>> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findByIdOptional(id));
    }

    @Async
    @Override
    public CompletableFuture<Profile> saveAsync(Profile profile) {
        return CompletableFuture.completedFuture(save(profile));
    }

    @Async
    @Override
    public CompletableFuture<List<Profile>> findAllAsync() {
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
    public CompletableFuture<Optional<Profile>> findByCustomerIdAsync(Long customerId) {
        return CompletableFuture.completedFuture(findByCustomerId(customerId));
    }

    @Async
    @Override
    public CompletableFuture<Page<Profile>> searchAsync(String keyword, boolean includeDeleted, Pageable pageable) {
        return CompletableFuture.completedFuture(search(keyword, includeDeleted, pageable));
    }
}