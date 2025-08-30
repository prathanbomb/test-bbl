package com.example.testbbl.exception;

import com.example.testbbl.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiResponse<Void> response = ApiResponse.error(status.value(), ex.getMessage());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailExistsException(EmailAlreadyExistsException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.CONFLICT;
        ApiResponse<Void> response = ApiResponse.error(status.value(), ex.getMessage());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());
        ApiResponse<Void> response = ApiResponse.error(status.value(), "Validation failure", fieldErrors);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<Map<String, String>> violations = ex.getConstraintViolations().stream()
                .map(this::toViolation)
                .collect(Collectors.toList());
        ApiResponse<Void> response = ApiResponse.error(status.value(), "Constraint violation", violations);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiResponse<Void>> handleServerWebInput(ServerWebInputException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiResponse<Void> response = ApiResponse.error(status.value(), ex.getReason());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiResponse<Void> response = ApiResponse.error(status.value(), ex.getMessage());
        return new ResponseEntity<>(response, status);
    }

    private Map<String, String> toFieldError(FieldError error) {
        Map<String, String> map = new HashMap<>();
        map.put("field", error.getField());
        map.put("rejectedValue", String.valueOf(error.getRejectedValue()));
        map.put("message", error.getDefaultMessage());
        return map;
    }

    private Map<String, String> toViolation(ConstraintViolation<?> violation) {
        Map<String, String> map = new HashMap<>();
        map.put("field", violation.getPropertyPath().toString());
        map.put("rejectedValue", String.valueOf(violation.getInvalidValue()));
        map.put("message", violation.getMessage());
        return map;
    }
}