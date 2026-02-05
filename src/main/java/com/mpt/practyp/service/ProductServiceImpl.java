package com.mpt.practyp.service;

import com.mpt.practyp.model.Category;
import com.mpt.practyp.model.Product;
import com.mpt.practyp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryService categoryService;

    @Override
    public Page<Product> findAll(Pageable pageable, boolean includeDeleted) {
        if (includeDeleted) {
            return repository.findAll(pageable);
        } else {
            return repository.findByDeletedFalse(pageable);
        }
    }

    @Override
    public Page<Product> searchByName(String name, Long categoryId, Pageable pageable, boolean includeDeleted) {
        if (includeDeleted) {
            if (categoryId != null) {
                Category cat = categoryService.findById(categoryId);
                return repository.findByNameContainingIgnoreCaseAndCategory(name, cat, pageable);
            } else {
                return repository.findByNameContainingIgnoreCase(name, pageable);
            }
        } else {
            if (categoryId != null) {
                Category cat = categoryService.findById(categoryId);
                return repository.findByNameContainingIgnoreCaseAndCategoryAndDeletedFalse(name, cat, pageable);
            } else {
                return repository.findByNameContainingIgnoreCaseAndDeletedFalse(name, pageable);
            }
        }
    }

    @Override
    public Product findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Product create(Product product) {
        return repository.save(product);
    }

    @Override
    public Product update(Product product) {
        return repository.save(product);
    }

    @Override
    public void softDelete(Long id) {
        Product product = repository.findById(id).orElseThrow();
        product.setDeleted(true);
        repository.save(product);
    }

    @Override
    public void hardDelete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Product> findByIdOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Product save(Product product) {
        return repository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Product> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    @Async
    @Override
    public CompletableFuture<Page<Product>> findAllAsync(Pageable pageable, boolean includeDeleted) {
        return CompletableFuture.completedFuture(findAll(pageable, includeDeleted));
    }

    @Async
    @Override
    public CompletableFuture<Optional<Product>> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findByIdOptional(id));
    }

    @Async
    @Override
    public CompletableFuture<Product> saveAsync(Product product) {
        return CompletableFuture.completedFuture(save(product));
    }

    @Async
    @Override
    public CompletableFuture<List<Product>> findAllAsync() {
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
    public CompletableFuture<List<Product>> searchByNameAsync(String name) {
        return CompletableFuture.completedFuture(searchByName(name));
    }
}