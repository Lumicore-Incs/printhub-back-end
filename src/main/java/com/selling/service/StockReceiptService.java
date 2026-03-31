package com.selling.service;

import com.selling.dto.PagedResponse;
import com.selling.dto.StockReceiptRequest;
import com.selling.model.*;
import com.selling.repository.ProductRepo;
import com.selling.repository.StockReceiptRepo;
import com.selling.repository.SupplierRepo;
import com.selling.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StockReceiptService {

    private final StockReceiptRepo stockReceiptRepo;
    private final ProductRepo productRepo;
    private final SupplierRepo supplierRepo;
    private final UserRepo userRepo;

    public PagedResponse<StockReceipt> getStockReceipts(int page, int size, String q) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("receivedDate").descending());
        Page<StockReceipt> receiptPage;

        if (q != null && !q.isEmpty()) {
            receiptPage = stockReceiptRepo.findByProduct_NameContainingIgnoreCaseOrSupplier_NameContainingIgnoreCase(q, q, pageable);
        } else {
            receiptPage = stockReceiptRepo.findAll(pageable);
        }
        return PagedResponse.<StockReceipt>builder()
                .items(receiptPage.getContent())
                .totalItems(receiptPage.getTotalElements())
                .totalPages(receiptPage.getTotalPages())
                .currentPage(receiptPage.getNumber() + 1)
                .build();
    }

    public StockReceipt getStockReceiptById(Long id) {
        return stockReceiptRepo.findById(id).orElseThrow(() -> new RuntimeException("Stock receipt not found"));
    }

    @Transactional
    public StockReceipt recordStockReceipt(StockReceiptRequest request, String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElseThrow();
        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Supplier supplier = supplierRepo.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        StockReceipt receipt = StockReceipt.builder()
                .product(product)
                .supplier(supplier)
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .receivedDate(LocalDateTime.now())
                .receivedBy(user)
                .build();

        product.setStock(product.getStock() + request.getQuantity());
        productRepo.save(product);

        return stockReceiptRepo.save(receipt);
    }

    public void deleteStockReceipt(Long id) {
        stockReceiptRepo.deleteById(id);
    }
}
