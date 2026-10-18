package com.selling.controller;

import com.selling.dto.OrderDto;
import com.selling.dto.UserDto;
import com.selling.dto.get.OrderDtoGet;
import com.selling.service.OrderService;
import com.selling.util.ApiResponse;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JWTTokenGenerator jwtTokenGenerator;

    @PostMapping("/save")
    public ResponseEntity<Object> saveOrder(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody OrderDto orderDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            UserDto userDto = jwtTokenGenerator
                    .getUserFromJwtToken(authorizationHeader);

            OrderDto savedOrder = orderService.createOrder(
                    orderDto, userDto.getId());

            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason(),
                            e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error saving order: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET ALL ORDERS / MY ORDERS / ORDER BY ID
    @GetMapping
    public ResponseEntity<Object> getOrders(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "id", required = false) Integer id) {

        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            if (page < 0 || size < 1 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(
                                "Page must be >= 0 and size must be between 1 and 100",
                                400));
            }

            UserDto userDto =
                    jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);

            String role = userDto.getRole();

            boolean isAdmin =
                    "SUPER USER".equalsIgnoreCase(role)
                            || "ADMIN".equalsIgnoreCase(role);

            // Get one order by ID
            if (id != null) {
                OrderDto order = orderService.getOrderById(id);

                // Non-admin users can access only their own orders
                if (!isAdmin &&
                        !orderService.isOrderOwnedByUser(id, Math.toIntExact(userDto.getId()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error(
                                    "You do not have permission to access this order",
                                    403));
                }

                return ResponseEntity.ok(order);
            }

            // Admin and Super User: all orders with pagination
            if (isAdmin) {
                PaginationResponse<OrderDtoGet> response =
                        orderService.getAllOrdersPaginated(
                                page, size, search, status);

                return ResponseEntity.ok(response);
            }

            // Other users: only their own orders with pagination
            PaginationResponse<OrderDtoGet> response =
                    orderService.getOrdersByUserPaginated(
                            Math.toIntExact(userDto.getId()), page, size, search, status);

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(
                            e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving orders: " + e.getMessage());
        }
    }

    @PatchMapping("/status/{id}")
    public ResponseEntity<Object> updateOrderStatus(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            String status = request.get("status");

            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status is required", 400));
            }

            return new ResponseEntity<>(
                    orderService.updateOrderStatus(id, status),
                    HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason(),
                            e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error updating order: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteOrder(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            orderService.deleteOrder(id);

            return new ResponseEntity<>(
                    "Order deleted successfully", HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason(),
                            e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error deleting order: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

