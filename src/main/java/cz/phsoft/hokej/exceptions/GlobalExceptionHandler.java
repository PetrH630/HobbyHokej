package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<cz.phsoft.hokej.exceptions.ApiError> buildError(HttpStatus status, String message, String path) {
        cz.phsoft.hokej.exceptions.ApiError error = new cz.phsoft.hokej.exceptions.ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(error);
    }

    // 1) DuplicateRegistrationException → 400
    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<cz.phsoft.hokej.exceptions.ApiError> handleDuplicateRegistration(
            DuplicateRegistrationException ex,
            HttpServletRequest request) {

        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    // 2) Validace DTO → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<cz.phsoft.hokej.exceptions.ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                                                HttpServletRequest request) {

        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return buildError(HttpStatus.BAD_REQUEST, msg, request.getRequestURI());
    }

    // 3) Libovolná jiná neočekávaná vyjímka → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<cz.phsoft.hokej.exceptions.ApiError> handleAll(Exception ex, HttpServletRequest request) {

        ex.printStackTrace(); // nebo logger

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getRequestURI()
        );
    }


}
