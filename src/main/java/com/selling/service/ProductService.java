package com.selling.service;

import com.selling.dto.PagedResponse;
import com.selling.model.Product;
import com.selling.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;

    public PagedResponse<Product> getProducts(int page, int size, String q) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Product> productPage;

        if (q != null && !q.isEmpty()) {
            productPage = productRepo.findByNameContainingIgnoreCase(q, pageable);
        } else {
            productPage = productRepo.findAll(pageable);
        }

        return PagedResponse.<Product>builder()
                .items(productPage.getContent())
                .totalItems(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .currentPage(productPage.getNumber() + 1)
                .build();
    }

    public Product getProductById(Long id) {
        return productRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(Product product) {
        return productRepo.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setStock(productDetails.getStock());
        product.setReorderLevel(productDetails.getReorderLevel());
        product.setPrice(productDetails.getPrice());
        product.setStatus(productDetails.getStatus());
        return productRepo.save(product);
    }

    public void deleteProduct(Long id) {
        productRepo.deleteById(id);
    }
}
