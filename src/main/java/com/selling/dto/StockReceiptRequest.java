package com.selling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockReceiptRequest {
    private Long productId;
    private Long supplierId;
    private int quantity;
    private BigDecimal unitPrice;
}
