package com.selling.repository;

import com.selling.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailsRepository
        extends JpaRepository<OrderDetails, Integer> {

    List<OrderDetails> findByOrder_OrderId(Integer orderId);

    boolean existsByOrder_OrderId(Integer orderId);
}

