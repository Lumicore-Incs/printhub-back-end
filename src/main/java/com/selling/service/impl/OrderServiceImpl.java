package com.selling.service.impl;

import com.selling.dto.OrderDto;
import com.selling.dto.OrderDetailsDto;
import com.selling.model.Item;
import com.selling.model.Order;
import com.selling.model.OrderDetails;
import com.selling.model.User;
import com.selling.repository.OrderDetailsRepository;
import com.selling.repository.ItemRepository;
import com.selling.repository.OrderRepository;
import com.selling.repository.UserRepo;
import com.selling.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepo userRepository;
    private final OrderDetailsRepository detailsRepository;
    private final ItemRepository itemRepository;

    private OrderDto toDto(Order order) {
        return new OrderDto(
                order.getOrderId(),
                order.getTotalPrice(),
                order.getOrderType(),
                order.getDate(),
                order.getStatus(),
                Math.toIntExact(order.getUser().getId()),
                null
        );
    }

    @Override
    public OrderDto createOrder(OrderDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(BigDecimal.ZERO);
        order.setOrderType(dto.getOrderType());
        order.setDate(LocalDateTime.now());
        order.setStatus(
                dto.getStatus() == null
                        ? "PENDING" : dto.getStatus());

        Order savedOrder = orderRepository.save(order);
        Order finalSavedOrder = savedOrder;
        List<OrderDetails> savedDetails = dto.getOrderDetails() == null
            ? List.of()
            : detailsRepository.saveAll(dto.getOrderDetails().stream()
                .map(detailsDto -> createDetails(detailsDto, finalSavedOrder))
                .toList());

        savedOrder.setTotalPrice(savedDetails.stream()
            .map(OrderDetails::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        savedOrder = orderRepository.save(savedOrder);

        OrderDto savedOrderDto = toDto(savedOrder);
        savedOrderDto.setOrderDetails(savedDetails.stream()
            .map(this::toDetailsDto)
            .toList());
        return savedOrderDto;
    }

        private OrderDetails createDetails(OrderDetailsDto dto, Order order) {
        if (dto.getQty() == null || dto.getQty() <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Quantity must be greater than zero");
        }

        Item item = itemRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Item not found"));
        if (item.getPrice() == null || item.getPrice().signum() < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid item price");
        }

        OrderDetails details = new OrderDetails();
        details.setItem(item);
        details.setOrder(order);
        details.setQty(dto.getQty());
        details.setTotal(item.getPrice()
            .multiply(BigDecimal.valueOf(dto.getQty())));
        return details;
        }

        private OrderDetailsDto toDetailsDto(OrderDetails details) {
        return new OrderDetailsDto(
            details.getOrderDetailsId(),
            details.getQty(),
            details.getTotal(),
            details.getItem().getProductId(),
            details.getOrder().getOrderId()
        );
        }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUser_Id(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        return toDto(order);
    }

    @Override
    public OrderDto updateOrderStatus(Integer id, String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        order.setStatus(status);

        return toDto(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (detailsRepository.existsByOrder_OrderId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Delete order details before deleting this order");
        }

        orderRepository.delete(order);
    }
}

