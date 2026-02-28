package cz.phsoft.hokej.config;

import cz.phsoft.hokej.shared.exceptions.ApiError;
import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Globální handler výjimek pro REST API.
 *
 * Slouží k centralizovanému zachytávání výjimek z controllerů a service
 * vrstvy, jejich převodu na jednotný JSON formát {@link ApiError} a
 * k nastavení odpovídajících HTTP status kódů.
 *
 * Třída neřeší business logiku ani detailní logování. Tyto aspekty lze
 * doplnit do jednotlivých handler metod podle potřeby.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) Doménové výjimky

    /**
     * Zachytává výjimky typu {@link BusinessException}.
     *
     * Výjimka předává HTTP status, uživatelskou zprávu a další informace,
     * které se používají při sestavení odpovědi {@link ApiError}.
     */
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

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    // 2) Přístup odepřen (Spring Security)

    /**
     * Zachytává {@link AccessDeniedException} vyhozenou Spring Security.
     *
     * Typicky jde o situace, kdy uživatel nemá potřebnou roli pro volání
     * daného endpointu. Vrací se HTTP status 403 Forbidden.
     */
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

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    // 3) Chybné vstupy (IllegalArgumentException)

    /**
     * Zachytává {@link IllegalArgumentException}.
     *
     * Používá se pro obecné validační chyby vstupů nebo nesprávné
     * parametry předané do service vrstvy. Vrací se HTTP status 400.
     */
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

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // 4) Neplatný stav aplikace (IllegalStateException)

    /**
     * Zachytává {@link IllegalStateException}.
     *
     * Používá se pro situace, kdy je aplikace v neplatném stavu a
     * daná operace nemůže být provedena. Typicky se vrací HTTP status 409.
     */
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

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // 5) Porušení integrity dat

    /**
     * Zachytává {@link DataIntegrityViolationException} z databázové vrstvy.
     *
     * Typicky jde o porušení unikátních omezení nebo jiné konflikty při
     * ukládání dat. Z bezpečnostních důvodů se nevrací detailní databázová
     * zpráva, ale obecnější chybové hlášení.
     */
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

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // 6) Neočekávané chyby (fallback)

    /**
     * Fallback handler pro všechny neošetřené výjimky.
     *
     * Slouží jako poslední ochrana proti pádu aplikace bez odpovědi
     * a vrací jednotný formát chyby s HTTP statusem 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex,
                                              HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "BE - Došlo k neočekávané chybě na serveru.",
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    // 7) Validační chyby (@Valid, Bean Validation)

    /**
     * Zachytává validační chyby vyvolané anotací {@code @Valid}.
     *
     * Do pole {@code details} se ukládá mapa ve tvaru název pole → text
     * validační chyby. Vrací se HTTP status 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new java.util.LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();

            fieldErrors.merge(
                    fieldName,
                    errorMessage,
                    (existing, added) -> existing + "; " + added
            );
        }

        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                "BE - Neplatná vstupní data.",
                request.getRequestURI(),
                request.getRemoteAddr(),
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
