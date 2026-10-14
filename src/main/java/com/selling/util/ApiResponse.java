package com.selling.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private int statusCode;
    private Object data;

    public static ApiResponse success(String message, Object data, int statusCode) {
        return new ApiResponse(
                true,
                message,
                statusCode,
                data
        );
    }

    public static ApiResponse error(String message, int statusCode) {
        return new ApiResponse(
                false,
                message,
                statusCode,
                null
        );
    }
}

