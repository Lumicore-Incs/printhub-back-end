package com.selling.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Unified API response wrapper for all endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;
    private long timestamp;

    private ApiResponse() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    // ── Factory helpers ─────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.status  = 200;
        r.message = message;
        r.data    = data;
        return r;
    }

    public static <T> ApiResponse<T> created(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.status  = 201;
        r.message = "Created successfully";
        r.data    = data;
        return r;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.status  = 207;   // Multi-Status (reuse case)
        r.message = message;
        r.data    = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String message, int httpStatus) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.status  = httpStatus;
        r.message = message;
        return r;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public boolean isSuccess()  { return success; }
    public int     getStatus()  { return status; }
    public String  getMessage() { return message; }
    public T       getData()    { return data; }
    public long    getTimestamp(){ return timestamp; }
}
