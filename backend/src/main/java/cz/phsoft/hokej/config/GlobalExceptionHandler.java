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
@ControllerAdvice
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
                ex.getMessage(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
