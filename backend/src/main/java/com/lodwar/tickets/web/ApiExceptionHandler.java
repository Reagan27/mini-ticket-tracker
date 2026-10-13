package com.lodwar.tickets.web;

import com.lodwar.tickets.service.TicketNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;


@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage()).toList();
        return body(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException ex) {
        
        Throwable cause = ex.getMostSpecificCause();
        String msg = cause instanceof IllegalArgumentException ? cause.getMessage() : "Malformed JSON body";
        return body(HttpStatus.BAD_REQUEST, msg, List.of());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception ex) {
        String msg = ex instanceof MethodArgumentTypeMismatchException
                ? "Invalid value for parameter '" + ((MethodArgumentTypeMismatchException) ex).getName() + "'"
                : ex.getMessage();
        return body(HttpStatus.BAD_REQUEST, msg, List.of());
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(TicketNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String error, List<String> details) {
        return ResponseEntity.status(status).body(Map.of("error", error, "details", details));
    }
}
