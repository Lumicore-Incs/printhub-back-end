package com.selling.repository;

import com.selling.model.StockReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockReceiptRepo extends JpaRepository<StockReceipt, Long> {
    Page<StockReceipt> findByProduct_NameContainingIgnoreCaseOrSupplier_NameContainingIgnoreCase(String productName, String supplierName, Pageable pageable);
}
