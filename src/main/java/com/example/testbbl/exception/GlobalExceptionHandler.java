package com.example.testbbl.exception;

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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> errorBody(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(errorBody(status, ex.getMessage(), exchange.getRequest().getPath().value()), status);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailExistsException(EmailAlreadyExistsException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.CONFLICT;
        return new ResponseEntity<>(errorBody(status, ex.getMessage(), exchange.getRequest().getPath().value()), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());
        Map<String, Object> body = errorBody(status, "Validation failed", exchange.getRequest().getPath().value());
        body.put("errors", fieldErrors);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<Map<String, String>> violations = ex.getConstraintViolations().stream()
                .map(this::toViolation)
                .collect(Collectors.toList());
        Map<String, Object> body = errorBody(status, "Constraint violation", exchange.getRequest().getPath().value());
        body.put("errors", violations);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Map<String, Object>> handleServerWebInput(ServerWebInputException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorBody(status, ex.getReason(), exchange.getRequest().getPath().value()), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(errorBody(status, ex.getMessage(), exchange.getRequest().getPath().value()), status);
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