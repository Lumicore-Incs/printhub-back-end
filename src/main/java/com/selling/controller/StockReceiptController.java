package com.selling.controller;

import com.selling.dto.PagedResponse;
import com.selling.dto.StockReceiptRequest;
import com.selling.model.StockReceipt;
import com.selling.service.StockReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockReceiptController {

    private final StockReceiptService stockReceiptService;

    @GetMapping("/receipts")
    public ResponseEntity<PagedResponse<StockReceipt>> getReceipts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(stockReceiptService.getStockReceipts(page, size, q));
    }

    @GetMapping("/receipts/{id}")
    public ResponseEntity<StockReceipt> getReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(stockReceiptService.getStockReceiptById(id));
    }

    @PostMapping("/receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERUSER')")
    public ResponseEntity<StockReceipt> recordStockReceipt(
            @RequestBody StockReceiptRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(201).body(stockReceiptService.recordStockReceipt(request, userDetails.getUsername()));
    }

    @DeleteMapping("/receipt/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERUSER')")
    public ResponseEntity<Void> deleteStockReceipt(@PathVariable Long id) {
        stockReceiptService.deleteStockReceipt(id);
        return ResponseEntity.noContent().build();
    }
}
