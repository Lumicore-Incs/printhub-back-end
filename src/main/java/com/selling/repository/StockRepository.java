package com.selling.repository;

import com.selling.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StockRepository
        extends JpaRepository<Stock, Integer>,
        JpaSpecificationExecutor<Stock> {

    Optional<Stock> findByItem_ProductId(Integer productId);
}
