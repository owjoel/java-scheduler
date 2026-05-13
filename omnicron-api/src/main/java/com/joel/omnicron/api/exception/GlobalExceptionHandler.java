package com.joel.omnicron.api.exception;

import com.joel.omnicron.api.dto.ApiResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(status, "Request body is missing or malformed"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String error = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(status, error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(status, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(status, e.getMessage()));
    }
}
