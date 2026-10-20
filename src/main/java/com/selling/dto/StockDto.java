package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockDto {

    private Integer stockId;
    private String type;
    private Integer quantity;
    private String status;
    private LocalDate date;
    private Integer productId;
    private String productName;
}
