package com.mpt.practyp.repository;

import com.mpt.practyp.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByDeletedFalse(Pageable pageable);
    Page<Category> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}