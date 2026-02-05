package com.mpt.practyp.repository;

import com.mpt.practyp.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Page<Tag> findByDeletedFalse(Pageable pageable);
    Page<Tag> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);
}