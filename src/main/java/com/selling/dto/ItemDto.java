package com.selling.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Integer productId;
    private String name;
    private BigDecimal price;
    private String status;
}

