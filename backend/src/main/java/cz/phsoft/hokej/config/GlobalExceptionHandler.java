package cz.phsoft.hokej.config;

import cz.phsoft.hokej.exceptions.ApiError;
import cz.phsoft.hokej.exceptions.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1) Všechny doménové / business chyby (extends BusinessException)
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

    // 2) Přístup odepřen (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 3) Validační chyby - špatné vstupy (např. hesla se neshodují, špatné datum)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 4) Stavové chyby aplikace - typicky konflikty (např. nelze zvolit neschváleného hráče)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex,
                                                       HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 5) race condition
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "BE - Záznam porušuje unikátní omezení (pravděpodobně duplicitní hráč).",
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    // 6) Ostatní nečekané chyby (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex,
                                              HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
