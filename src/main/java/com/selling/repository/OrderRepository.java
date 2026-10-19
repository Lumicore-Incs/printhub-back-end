package com.selling.repository;

import com.selling.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Integer>,
        JpaSpecificationExecutor<Order> {

}

