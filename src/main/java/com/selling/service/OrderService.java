package com.selling.service;

import com.selling.dto.OrderRequest;
import com.selling.dto.PagedResponse;
import com.selling.model.*;
import com.selling.repository.OrderItemRepo;
import com.selling.repository.OrderRepo;
import com.selling.repository.ProductRepo;
import com.selling.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;

    public PagedResponse<Order> getOrders(int page, int size, String q) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("date").descending());
        Page<Order> orderPage;

        if (q != null && !q.isEmpty()) {
            orderPage = orderRepo.findByIdContainingIgnoreCase(q, pageable);
        } else {
            orderPage = orderRepo.findAll(pageable);
        }

        return PagedResponse.<Order>builder()
                .items(orderPage.getContent())
                .totalItems(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .currentPage(orderPage.getNumber() + 1)
                .build();
    }

    public Order getOrderById(Long id) {
        return orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public Order createOrder(OrderRequest request, String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElseThrow();
        
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (var itemReq : request.getItems()) {
            Product product = productRepo.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            BigDecimal itemPrice = product.getPrice().multiply(new BigDecimal(itemReq.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);

            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepo.save(product);

            orderItems.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceAtTime(product.getPrice())
                    .build());
        }

        Order order = Order.builder()
                .totalPrice(totalPrice)
                .status(OrderStatusEnum.PENDING)
                .date(LocalDateTime.now())
                .createdBy(user)
                .build();

        Order savedOrder = orderRepo.save(order);
        
        for (var item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepo.save(item);
        }

        return savedOrder;
    }

    public Order updateOrderStatus(Long id, OrderStatusEnum status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepo.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepo.deleteById(id);
    }
}
