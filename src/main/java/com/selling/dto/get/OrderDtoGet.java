package com.selling.dto.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDtoGet {

    private Integer id;

    private String orderId;

    private String customerName;

    private String productName;

    private Integer quantity;

    private BigDecimal totalPrice;

    private String status;

    private String remark;

    private LocalDateTime orderDate;

    private LocalDateTime deliveryDate;

    private Integer userId;

    private String username;
}

