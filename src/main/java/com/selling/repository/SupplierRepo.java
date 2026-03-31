package com.selling.repository;

import com.selling.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepo extends JpaRepository<Supplier, Long> {
    Page<Supplier> findByNameContainingIgnoreCaseOrContactPersonContainingIgnoreCase(String name, String contact, Pageable pageable);
}
