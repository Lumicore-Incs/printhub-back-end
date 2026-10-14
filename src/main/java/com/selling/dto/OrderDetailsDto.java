package com.selling.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDto {

    private Integer orderDetailsId;
    private Integer qty;
    private BigDecimal total;
    private Integer productId;
    private Integer orderId;
}

