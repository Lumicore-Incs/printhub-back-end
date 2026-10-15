package com.selling.controller;

import com.selling.dto.OrderDto;
import com.selling.dto.UserDto;
import com.selling.service.OrderService;
import com.selling.util.ApiResponse;
import com.selling.util.JWTTokenGenerator;
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

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOrders(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    orderService.getAllOrders(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving orders: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Object> getMyOrders(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            UserDto userDto = jwtTokenGenerator
                    .getUserFromJwtToken(authorizationHeader);

            return new ResponseEntity<>(
                    orderService.getOrdersByUser(userDto.getId()),
                    HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving user orders: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrderById(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    orderService.getOrderById(id), HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason(),
                            e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving order: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
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

