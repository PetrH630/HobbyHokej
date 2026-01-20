package cz.phsoft.hokej.config;

import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.exceptions.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()); // ← IP klienta);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // --- Nenalezené zdroje (404) ---
    @ExceptionHandler({
            MatchNotFoundException.class,
            PlayerNotFoundException.class,
            RegistrationNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()); // ← IP klienta
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // --- Konflikty (409) ---
    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<ApiError> handleConflict(DuplicateRegistrationException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()); // ← IP klienta);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // --- Obecné chyby (500) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()); // ← IP klienta);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
