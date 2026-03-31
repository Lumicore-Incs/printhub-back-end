package com.selling.service;

import com.selling.dto.PagedResponse;
import com.selling.model.Supplier;
import com.selling.repository.SupplierRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepo supplierRepo;

    public PagedResponse<Supplier> getSuppliers(int page, int size, String q) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<Supplier> supplierPage;

        if (q != null && !q.isEmpty()) {
            supplierPage = supplierRepo.findByNameContainingIgnoreCaseOrContactPersonContainingIgnoreCase(q, q, pageable);
        } else {
            supplierPage = supplierRepo.findAll(pageable);
        }

        return PagedResponse.<Supplier>builder()
                .items(supplierPage.getContent())
                .totalItems(supplierPage.getTotalElements())
                .totalPages(supplierPage.getTotalPages())
                .currentPage(supplierPage.getNumber() + 1)
                .build();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepo.findById(id).orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepo.save(supplier);
    }

    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = getSupplierById(id);
        supplier.setName(supplierDetails.getName());
        supplier.setContactPerson(supplierDetails.getContactPerson());
        supplier.setEmail(supplierDetails.getEmail());
        supplier.setPhone(supplierDetails.getPhone());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setStatus(supplierDetails.getStatus());
        return supplierRepo.save(supplier);
    }

    public void deleteSupplier(Long id) {
        supplierRepo.deleteById(id);
    }
}
