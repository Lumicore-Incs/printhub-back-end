package com.selling.service;

import com.selling.dto.OrderDto;

import java.util.List;

public interface OrderService {

    OrderDto createOrder(OrderDto dto, Long userId);

    List<OrderDto> getAllOrders();

    List<OrderDto> getOrdersByUser(Long userId);

    OrderDto getOrderById(Integer id);

    OrderDto updateOrderStatus(Integer id, String status);

    void deleteOrder(Integer id);
}
