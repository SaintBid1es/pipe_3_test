package com.mpt.practyp.repository;

import com.mpt.practyp.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByDeletedFalse(Pageable pageable);
    Page<Customer> findByFirstNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Page<Customer> findByLastNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String firstName, String lastName, String email, String username);
}