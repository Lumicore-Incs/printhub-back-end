package com.selling.service;

import com.selling.dto.OrderDto;
import com.selling.dto.get.OrderDtoGet;
import com.selling.util.PaginationResponse;

import java.util.List;

public interface OrderService {

    OrderDto createOrder(OrderDto dto, Long userId);

    List<OrderDto> getAllOrders();

    List<OrderDto> getOrdersByUser(Long userId);

    OrderDto getOrderById(Integer id);

    OrderDto updateOrderStatus(Integer id, String status);

    void deleteOrder(Integer id);

    PaginationResponse<OrderDtoGet> getAllOrdersPaginated(
            int page,
            int size,
            String search,
            String status
    );

    PaginationResponse<OrderDtoGet> getOrdersByUserPaginated(
            Integer userId,
            int page,
            int size,
            String search,
            String status
    );

    boolean isOrderOwnedByUser(Integer orderId, Integer userId);

}
