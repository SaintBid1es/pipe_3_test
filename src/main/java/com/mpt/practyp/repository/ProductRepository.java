package com.mpt.practyp.repository;

import com.mpt.practyp.model.Category;
import com.mpt.practyp.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface    ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByDeletedFalse(Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndCategory(String name, Category category, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndCategoryAndDeletedFalse(String name, Category category, Pageable pageable);
    List<Product> findByNameContainingIgnoreCase(String name);
}