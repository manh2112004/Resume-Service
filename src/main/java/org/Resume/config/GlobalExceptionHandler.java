package org.Resume.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Request failed: status={}, message={}", ex.getStatusCode(), ex.getReason());
        return ResponseEntity.status(ex.getStatusCode())
                .body(buildBody(ex.getStatusCode().value(), ex.getReason()));
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<Map<String, Object>> handleCompletionException(CompletionException ex) {
        Throwable cause = unwrap(ex);
        if (cause instanceof ResponseStatusException responseStatusException) {
            return handleResponseStatusException(responseStatusException);
        }

        log.error("Async request failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống"));
    }

    private Throwable unwrap(Throwable ex) {
        Throwable cause = ex;
        while (cause != null && (cause instanceof CompletionException || cause.getClass().getName().contains("ExecutionException"))) {
            if (cause.getCause() == null) {
                break;
            }
            cause = cause.getCause();
        }
        return cause;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = "Dữ liệu request không hợp lệ";
        if (fieldError != null) {
            message = fieldError.getField() + " không đúng định dạng hoặc giá trị không hợp lệ";
        }

        log.warn("Request validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("Unexpected request failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống"));
    }

    private Map<String, Object> buildBody(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("message", message);
        return body;
    }
}
