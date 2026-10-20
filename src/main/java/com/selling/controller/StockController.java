package com.selling.controller;

import com.selling.dto.StockDto;
import com.selling.service.StockService;
import com.selling.util.ApiResponse;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final JWTTokenGenerator jwtTokenGenerator;

    @PostMapping("/save")
    public ResponseEntity<Object> saveStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody StockDto stockDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return unauthorized();
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(stockService.createStock(stockDto));
        } catch (ResponseStatusException e) {
            return statusError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving stock: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody StockDto stockDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return unauthorized();
            }
            return ResponseEntity.ok(stockService.updateStock(id, stockDto));
        } catch (ResponseStatusException e) {
            return statusError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating stock: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return unauthorized();
            }
            stockService.deleteStock(id);
            return ResponseEntity.ok("Stock deleted successfully");
        } catch (ResponseStatusException e) {
            return statusError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting stock: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getStockById(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return unauthorized();
            }
            return ResponseEntity.ok(stockService.getStockById(id));
        } catch (ResponseStatusException e) {
            return statusError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving stock: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Object> getStocks(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "date", required = false) LocalDate date,
            @RequestParam(name = "dateFrom", required = false)
            LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            LocalDate dateTo,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "stockId", required = false) Integer stockId) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return unauthorized();
            }
            return ResponseEntity.ok(stockService.getStocks(
                    page, size, search, type, date, dateFrom, dateTo,
                    status, stockId));
        } catch (ResponseStatusException e) {
            return statusError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving stocks: " + e.getMessage());
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Object> getLowStock(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "threshold") int threshold,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return unauthorized();
            }
            return ResponseEntity.ok(
                    stockService.getLowStock(threshold, page, size));
        } catch (ResponseStatusException e) {
            return statusError(e);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving low stock: " + e.getMessage());
        }
    }

    private ResponseEntity<Object> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid token", 401));
    }

    private ResponseEntity<Object> statusError(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(
                        e.getReason(), e.getStatusCode().value()));
    }
}
