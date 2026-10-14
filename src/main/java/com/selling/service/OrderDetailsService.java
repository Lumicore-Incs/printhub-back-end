package com.selling.service;

import com.selling.dto.OrderDetailsDto;

import java.util.List;

public interface OrderDetailsService {

    OrderDetailsDto createDetails(OrderDetailsDto dto);

    List<OrderDetailsDto> getAllDetails();

    List<OrderDetailsDto> getDetailsByOrder(Integer orderId);

    OrderDetailsDto getDetailsById(Integer id);

    OrderDetailsDto updateDetails(Integer id, OrderDetailsDto dto);

    void deleteDetails(Integer id);
}

