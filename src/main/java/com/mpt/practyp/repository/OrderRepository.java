package com.mpt.practyp.repository;

import com.mpt.practyp.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByDeletedFalse(Pageable pageable);
    List<Order> findByCustomerId(Long customerId);
    Page<Order> findByCustomerIdAndDeletedFalse(Long customerId, Pageable pageable);
}