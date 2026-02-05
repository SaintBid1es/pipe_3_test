package com.mpt.practyp.service;

import com.mpt.practyp.model.Tag;
import com.mpt.practyp.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository repository;

    @Override
    public Page<Tag> findAll(boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findAll(pageable);
        return repository.findByDeletedFalse(pageable);
    }

    @Override
    public Tag findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Tag create(Tag tag) {
        return repository.save(tag);
    }

    @Override
    public Tag update(Tag tag) {
        return repository.save(tag);
    }

    @Override
    public void softDelete(Long id) {
        Tag tag = repository.findById(id).orElseThrow();
        tag.setDeleted(true);
        repository.save(tag);
    }

    @Override
    public void hardDelete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void restore(Long id) {
        Tag tag = repository.findById(id).orElseThrow();
        tag.setDeleted(false);
        repository.save(tag);
    }

    @Override
    public Page<Tag> search(String keyword, boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findByNameContainingIgnoreCase(keyword, pageable);
        return repository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
    }

    @Override
    public List<Tag> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Tag> findByIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Tag save(Tag tag) {
        return repository.save(tag);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Async
    @Override
    public CompletableFuture<Page<Tag>> findAllAsync(boolean includeDeleted, Pageable pageable) {
        return CompletableFuture.completedFuture(findAll(includeDeleted, pageable));
    }

    @Async
    @Override
    public CompletableFuture<Optional<Tag>> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findByIdOptional(id));
    }

    @Async
    @Override
    public CompletableFuture<Tag> saveAsync(Tag tag) {
        return CompletableFuture.completedFuture(save(tag));
    }

    @Async
    @Override
    public CompletableFuture<List<Tag>> findAllAsync() {
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
    public CompletableFuture<Page<Tag>> searchAsync(String keyword, boolean includeDeleted, Pageable pageable) {
        return CompletableFuture.completedFuture(search(keyword, includeDeleted, pageable));
    }
}