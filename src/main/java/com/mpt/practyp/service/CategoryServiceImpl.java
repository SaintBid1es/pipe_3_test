package com.mpt.practyp.service;

import com.mpt.practyp.model.Category;
import com.mpt.practyp.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Override
    public Page<Category> findAll(boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findAll(pageable);
        else return repository.findByDeletedFalse(pageable);
    }

    @Override
    public Category findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Category create(Category category) {
        return repository.save(category);
    }

    @Override
    public Category update(Category category) {
        return repository.save(category);
    }

    @Override
    public void softDelete(Long id) {
        Category cat = repository.findById(id).orElseThrow();
        cat.setDeleted(true);
        repository.save(cat);
    }

    @Override
    public void hardDelete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void restore(Long id) {
        Category cat = repository.findById(id).orElseThrow();
        cat.setDeleted(false);
        repository.save(cat);
    }

    @Override
    public Page<Category> search(String keyword, boolean includeDeleted, Pageable pageable) {
        if (includeDeleted) return repository.findByNameContainingIgnoreCase(keyword, pageable);
        else return repository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
    }

    @Override
    public List<Category> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Category> findByIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Category save(Category category) {
        return repository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Async
    @Override
    public CompletableFuture<Page<Category>> findAllAsync(boolean includeDeleted, Pageable pageable) {
        return CompletableFuture.completedFuture(findAll(includeDeleted, pageable));
    }

    @Async
    @Override
    public CompletableFuture<Optional<Category>> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findByIdOptional(id));
    }

    @Async
    @Override
    public CompletableFuture<Category> saveAsync(Category category) {
        return CompletableFuture.completedFuture(save(category));
    }

    @Async
    @Override
    public CompletableFuture<List<Category>> findAllAsync() {
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
}