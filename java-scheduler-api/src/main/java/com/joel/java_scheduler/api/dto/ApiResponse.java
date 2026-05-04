package com.joel.java_scheduler.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        Instant time,
        T data,
        String error) {

    public static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(
                status.value(),
                status.getReasonPhrase(),
                Instant.now(),
                data,
                null);
    }

    public static ApiResponse<Void> failure(HttpStatus status, String error) {
        return new ApiResponse<>(
                status.value(),
                status.getReasonPhrase(),
                Instant.now(),
                null,
                error);
    }
}
