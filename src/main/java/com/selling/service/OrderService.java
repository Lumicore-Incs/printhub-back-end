package com.selling.service;

import com.selling.dto.OrderDto;
import com.selling.util.PaginationResponse;

import java.time.LocalDate;

public interface OrderService {

    OrderDto createOrder(OrderDto dto, Long userId);

    OrderDto updateOrderStatus(Integer id, String status);

    void deleteOrder(Integer id);

    PaginationResponse<OrderDto> getAllOrdersPaginated(
            int page,
            int size,
            String search,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo
    );
}
