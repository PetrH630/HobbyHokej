package cz.phsoft.hokej.config;

import cz.phsoft.hokej.exceptions.ApiError;
import cz.phsoft.hokej.exceptions.BusinessException;
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
 * <p>
 * Účel:
 * <ul>
 *     <li>centralizovaně zachytávat výjimky z controllerů a service vrstvy,</li>
 *     <li>převést je na jednotnou JSON odpověď typu {@link ApiError},</li>
 *     <li>zajistit konzistentní HTTP status kód pro jednotlivé typy chyb.</li>
 * </ul>
 *
 * Tato třída:
 * <ul>
 *     <li>neřeší logování (to lze doplnit do jednotlivých handlerů),</li>
 *     <li>neřeší business logiku – pouze mapuje výjimky na HTTP odpovědi.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==========================================
    // 1) BUSINESS / DOMÉNOVÉ VÝJIMKY
    // ==========================================

    /**
     * Zachytí všechny výjimky dědící z {@link BusinessException}.
     * <p>
     * Typicky:
     * <ul>
     *     <li>PlayerNotFoundException,</li>
     *     <li>InvalidPlayerStatusException,</li>
     *     <li>SeasonNotFoundException,</li>
     *     <li>MatchNotFoundException, …</li>
     * </ul>
     *
     * Každá {@link BusinessException} sama nese:
     * <ul>
     *     <li>HTTP status ({@link BusinessException#getStatus()}),</li>
     *     <li>uživatelskou chybovou zprávu (message),</li>
     *     <li>typ chyby (implicitně přes status a název výjimky).</li>
     * </ul>
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

    // ==========================================
    // 2) PŘÍSTUP ODEPŘEN (Spring Security)
    // ==========================================

    /**
     * Zachytí {@link AccessDeniedException} vyhozenou Spring Security.
     * <p>
     * Typicky jde o situace:
     * <ul>
     *     <li>uživatel nemá roli ADMIN / MANAGER pro daný endpoint,</li>
     *     <li>pokus o přístup k chráněnému zdroji bez oprávnění.</li>
     * </ul>
     *
     * HTTP status: 403 Forbidden
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

    // ==========================================
    // 3) ILLEGAL ARGUMENT – CHYBNÉ VSTUPY
    // ==========================================

    /**
     * Zachytí {@link IllegalArgumentException}.
     * <p>
     * Použití:
     * <ul>
     *     <li>obecné validační chyby vstupů,</li>
     *     <li>nesmyslné parametry předané do service vrstvy.</li>
     * </ul>
     *
     * HTTP status: 400 Bad Request
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

    // ==========================================
    // 4) ILLEGAL STATE – NEPLATNÝ STAV APLIKACE
    // ==========================================

    /**
     * Zachytí {@link IllegalStateException}.
     * <p>
     * Typicky:
     * <ul>
     *     <li>operace není povolena v aktuálním stavu (např. deaktivace poslední aktivní sezóny),</li>
     *     <li>porušení vnitřního invariantu aplikace.</li>
     * </ul>
     *
     * HTTP status: 409 Conflict
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

    // ==========================================
    // 5) DATA INTEGRITA (DB KONFLIKTY, RACE CONDITION)
    // ==========================================

    /**
     * Zachytí {@link DataIntegrityViolationException} z perzistentní vrstvy.
     * <p>
     * Typicky:
     * <ul>
     *     <li>porušení unikátních omezení v DB (unique constraint),</li>
     *     <li>race condition při paralelním ukládání stejných dat.</li>
     * </ul>
     *
     * Z bezpečnostních důvodů:
     * <ul>
     *     <li>nevrací detailní DB zprávu,</li>
     *     <li>používá generickou, ale užitečnou textovou hlášku.</li>
     * </ul>
     *
     * HTTP status: 409 Conflict
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



    // ==========================================
    // 6) FALLBACK – NEČEKANÉ CHYBY
    // ==========================================

    /**
     * Fallback handler pro všechny ostatní neošetřené výjimky.
     * <p>
     * Použití:
     * <ul>
     *     <li>zachytí runtime chyby, které nebyly explicitně ošetřeny,</li>
     *     <li>brání pádu aplikace bez odpovědi,</li>
     *     <li>vrací jednotný formát {@link ApiError} pro FE.</li>
     * </ul>
     *
     * HTTP status: 500 Internal Server Error
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
    // ==========================================
    // 7) VALIDACE VSTUPŮ (@Valid, Bean Validation)
    // ==========================================

    /**
     * Zachytí validační chyby z anotace {@code @Valid}.
     * <p>
     * Typické scénáře:
     * <ul>
     *     <li>nevyplněné povinné pole,</li>
     *     <li>neplatný formát (např. e-mail),</li>
     *     <li>porušení délkových nebo rozsahových omezení.</li>
     * </ul>
     *
     * Do pole {@code details} se vrací mapa {@code field → message},
     * kde klíčem je název pole v DTO a hodnotou text validační chyby.
     *
     * HTTP status: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Mapa field → message (použijeme LinkedHashMap pro zachování pořadí chyb)
        Map<String, String> fieldErrors = new java.util.LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();

            // Pokud je pro jedno pole více chyb, můžeme je spojit do jedné zprávy
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
