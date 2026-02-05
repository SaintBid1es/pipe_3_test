package com.mpt.practyp.repository;

import com.mpt.practyp.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Page<Profile> findByDeletedFalse(Pageable pageable);
    Page<Profile> findByAddressContainingIgnoreCaseAndDeletedFalse(String address, Pageable pageable);
    Page<Profile> findByAddressContainingIgnoreCase(String address, Pageable pageable);
    Optional<Profile> findByCustomerId(Long customerId);
}