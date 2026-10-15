package com.selling.controller;

import com.selling.dto.OrderDetailsDto;
import com.selling.service.OrderDetailsService;
import com.selling.util.ApiResponse;
import com.selling.util.JWTTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/order-details")
@RequiredArgsConstructor
public class OrderDetailsController {

    private final OrderDetailsService orderDetailsService;
    private final JWTTokenGenerator jwtTokenGenerator;

    @PostMapping("/save")
    public ResponseEntity<Object> saveOrderDetails(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody OrderDetailsDto orderDetailsDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            OrderDetailsDto savedDetails =
                    orderDetailsService.createDetails(orderDetailsDto);

            return new ResponseEntity<>(savedDetails, HttpStatus.CREATED);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(
                            e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error saving order details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOrderDetails(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    orderDetailsService.getAllDetails(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving order details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Object> getDetailsByOrder(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer orderId) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    orderDetailsService.getDetailsByOrder(orderId),
                    HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(
                            e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving order details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrderDetailsById(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    orderDetailsService.getDetailsById(id), HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(
                            e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving order detail: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateOrderDetails(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody OrderDetailsDto orderDetailsDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    orderDetailsService.updateDetails(id, orderDetailsDto),
                    HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(
                            e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error updating order details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteOrderDetails(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            orderDetailsService.deleteDetails(id);

            return new ResponseEntity<>(
                    "Order details deleted successfully", HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(
                            e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error deleting order details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

