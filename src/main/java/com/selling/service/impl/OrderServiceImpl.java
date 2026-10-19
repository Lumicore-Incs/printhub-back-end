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
import com.selling.util.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
        details.setTotal(dto.getTotal());
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


    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<OrderDto> getAllOrdersPaginated(
            int page,
            int size,
            String search,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be >= 0 and size must be between 1 and 100"
            );
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dateFrom must be on or before dateTo"
            );
        }

        // Database-level pagination and sorting
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "date")
        );

        Specification<Order> specification =
                buildOrderSpecification(search, status, dateFrom, dateTo);

        // Only the requested page is fetched from the database
        Page<Order> orderPage =
                orderRepository.findAll(specification, pageable);

        List<Order> orders = orderPage.getContent();
        List<Integer> orderIds = orders.stream()
                .map(Order::getOrderId)
                .toList();
        Map<Integer, List<OrderDetails>> detailsByOrderId = orderIds.isEmpty()
                ? Map.of()
                : detailsRepository.findByOrder_OrderIdIn(orderIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                details -> details.getOrder().getOrderId()));

        List<OrderDto> content = orders.stream()
                .map(order -> mapOrderToDto(
                        order,
                        detailsByOrderId.getOrDefault(
                                order.getOrderId(), List.of())))
                .toList();

        PaginationResponse<OrderDto> response =
                new PaginationResponse<>();

        response.setContent(content);
        response.setCurrentPage(orderPage.getNumber());
        response.setPageSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        response.setFirst(orderPage.isFirst());
        response.setLast(orderPage.isLast());
        response.setEmpty(orderPage.isEmpty());

        return response;
    }

    private Specification<Order> buildOrderSpecification(
            String search,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Status filter
            if (status != null
                    && !status.isBlank()
                    && !status.equalsIgnoreCase("ALL STATUS")) {

                predicates.add(
                        cb.equal(
                                cb.lower(root.get("status")),
                                status.trim().toLowerCase(Locale.ROOT)
                        )
                );
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.<LocalDateTime>get("date"),
                        dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThan(
                        root.<LocalDateTime>get("date"),
                        dateTo.plusDays(1).atStartOfDay()));
            }

            // Search filter
            if (search != null && !search.isBlank()) {
                String searchTerm = search.trim()
                        .replace('T', ' ')
                        .replace('t', ' ')
                        .toLowerCase(Locale.ROOT);
                String keyword = "%" + searchTerm + "%";
                var formattedOrderDate = cb.function(
                        "date_format",
                        String.class,
                        root.get("date"),
                        cb.literal("%Y-%m-%d %H:%i:%s")
                );

                Subquery<Integer> matchingOrderDetails = query.subquery(
                        Integer.class);
                Root<OrderDetails> detailsRoot =
                        matchingOrderDetails.from(OrderDetails.class);
                var itemJoin = detailsRoot.join("item", JoinType.INNER);
                matchingOrderDetails
                        .select(detailsRoot.get("order").get("orderId"))
                        .where(
                                cb.equal(
                                        detailsRoot.get("order").get("orderId"),
                                        root.get("orderId")),
                                cb.or(
                                        cb.like(
                                                cb.lower(itemJoin.get("name")),
                                                keyword),
                                        cb.like(
                                                detailsRoot.get("qty")
                                                        .as(String.class),
                                                keyword),
                                        cb.like(
                                                detailsRoot.get("total")
                                                        .as(String.class),
                                                keyword),
                                        cb.like(
                                                detailsRoot.get("orderDetailsId")
                                                        .as(String.class),
                                                keyword),
                                        cb.like(
                                                itemJoin.get("productId")
                                                        .as(String.class),
                                                keyword)
                                )
                        );

                Predicate searchPredicate = cb.or(
                        cb.like(
                                root.get("orderId").as(String.class),
                                "%" + search.trim() + "%"
                        ),
                        cb.like(
                                formattedOrderDate,
                                "%" + searchTerm + "%"
                        ),
                        cb.exists(matchingOrderDetails)
                );

                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private OrderDto mapOrderToDto(
            Order order, List<OrderDetails> orderDetails) {
        return new OrderDto(
                order.getOrderId(),
                order.getTotalPrice(),
                order.getOrderType(),
                order.getDate(),
                order.getStatus(),
                order.getUser() == null
                        ? null
                        : Math.toIntExact(order.getUser().getId()),
                orderDetails.stream()
                        .map(this::toDetailsDto)
                        .toList()
        );
    }


}
