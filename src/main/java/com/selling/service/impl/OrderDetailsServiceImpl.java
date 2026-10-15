package com.selling.service.impl;

import com.selling.dto.OrderDetailsDto;
import com.selling.model.Item;
import com.selling.model.Order;
import com.selling.model.OrderDetails;
import com.selling.repository.ItemRepository;
import com.selling.repository.OrderDetailsRepository;
import com.selling.repository.OrderRepository;
import com.selling.service.OrderDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderDetailsServiceImpl
        implements OrderDetailsService {

    private final OrderDetailsRepository detailsRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    private OrderDetailsDto toDto(OrderDetails details) {
        return new OrderDetailsDto(
                details.getOrderDetailsId(),
                details.getQty(),
                details.getTotal(),
                details.getItem().getProductId(),
                details.getOrder().getOrderId()
        );
    }

    private Item findItem(Integer productId) {
        return itemRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found"));
    }

    private Order findOrder(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));
    }

    private BigDecimal calculateTotal(Item item, Integer qty) {
        if (qty == null || qty <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        if (item.getPrice() == null
                || item.getPrice().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid item price");
        }

        return item.getPrice()
                .multiply(BigDecimal.valueOf(qty));
    }

    private void recalculateOrderTotal(Integer orderId) {
        Order order = findOrder(orderId);

        BigDecimal total = detailsRepository
                .findByOrder_OrderId(orderId)
                .stream()
                .map(OrderDetails::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
        orderRepository.save(order);
    }

    @Override
    public OrderDetailsDto createDetails(OrderDetailsDto dto) {
        Item item = findItem(dto.getProductId());
        Order order = findOrder(dto.getOrderId());

        OrderDetails details = new OrderDetails();
        details.setItem(item);
        details.setOrder(order);
        details.setQty(dto.getQty());
        details.setTotal(calculateTotal(item, dto.getQty()));

        OrderDetails saved = detailsRepository.save(details);

        recalculateOrderTotal(order.getOrderId());

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailsDto> getAllDetails() {
        return detailsRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailsDto> getDetailsByOrder(Integer orderId) {
        findOrder(orderId);

        return detailsRepository.findByOrder_OrderId(orderId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailsDto getDetailsById(Integer id) {
        OrderDetails details = detailsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order details not found"));

        return toDto(details);
    }

    @Override
    public OrderDetailsDto updateDetails(
            Integer id, OrderDetailsDto dto) {

        OrderDetails details = detailsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order details not found"));

        Item item = findItem(dto.getProductId());

        Integer orderId = details.getOrder().getOrderId();

        details.setItem(item);
        details.setQty(dto.getQty());
        details.setTotal(calculateTotal(item, dto.getQty()));

        OrderDetails saved = detailsRepository.save(details);

        recalculateOrderTotal(orderId);

        return toDto(saved);
    }

    @Override
    public void deleteDetails(Integer id) {
        OrderDetails details = detailsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order details not found"));

        Integer orderId = details.getOrder().getOrderId();

        detailsRepository.delete(details);
        detailsRepository.flush();

        recalculateOrderTotal(orderId);
    }
}

