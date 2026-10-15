package com.selling.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Integer orderId;
    private BigDecimal totalPrice;
    private String orderType;
    private LocalDateTime date;
    private String status;
    private Integer userId;
    private List<OrderDetailsDto> orderDetails;
}

