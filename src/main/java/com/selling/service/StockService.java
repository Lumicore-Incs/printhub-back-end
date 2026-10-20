package com.selling.service;

import com.selling.dto.StockDto;
import com.selling.util.PaginationResponse;

import java.time.LocalDate;

public interface StockService {

    StockDto createStock(StockDto dto);

    StockDto updateStock(Integer id, StockDto dto);

    void deleteStock(Integer id);

    StockDto getStockById(Integer id);

    PaginationResponse<StockDto> getStocks(
            int page,
            int size,
            String search,
            String type,
            LocalDate date,
            LocalDate dateFrom,
            LocalDate dateTo,
            String status,
            Integer stockId);

    PaginationResponse<StockDto> getLowStock(
            int threshold,
            int page,
            int size);
}
