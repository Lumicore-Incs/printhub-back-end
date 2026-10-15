package com.selling.controller;

import com.selling.dto.ItemDto;
import com.selling.service.ItemService;
import com.selling.util.ApiResponse;
import com.selling.util.JWTTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final JWTTokenGenerator jwtTokenGenerator;

    @PostMapping("/save")
    public ResponseEntity<Object> saveItem(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody ItemDto itemDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            ItemDto savedItem = itemService.createItem(itemDto);

            return new ResponseEntity<>(savedItem, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error saving item: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItems(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    itemService.getAllItems(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving items: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    itemService.getItemById(id), HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason(),
                            e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error retrieving item: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody ItemDto itemDto) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            return new ResponseEntity<>(
                    itemService.updateItem(id, itemDto), HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason(),
                            e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error updating item: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteItem(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid token", 401));
            }

            itemService.deleteItem(id);

            return new ResponseEntity<>(
                    "Item deleted successfully", HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason(),
                            e.getStatusCode().value()));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Error deleting item: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

