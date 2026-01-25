package cz.phsoft.hokej.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO reprezentující jednotný formát chybové odpovědi backendu.
 * <p>
 * Používá se v {@link cz.phsoft.hokej.config.GlobalExceptionHandler} pro
 * serializaci výjimek do JSON odpovědi, kterou zpracovává frontend.
 *
 * Struktura:
 * <ul>
 *     <li>{@code timestamp} – čas vzniku chyby na serveru,</li>
 *     <li>{@code status} – HTTP status kód (např. 400, 403, 409, 500),</li>
 *     <li>{@code error} – stručný textový popis statusu (např. "Bad Request"),</li>
 *     <li>{@code message} – detailnější zpráva (typicky z výjimky),</li>
 *     <li>{@code path} – URL, kde k chybě došlo,</li>
 *     <li>{@code clientIp} – IP adresa klienta, který požadavek poslal,</li>
 *     <li>{@code details} – volitelné doplňující informace (např. validační chyby).</li>
 * </ul>
 *
 * Příklad JSON odpovědi:
 * <pre>
 * {
 *   "timestamp": "2026-01-24 22:21:09",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "BE - Datum 'od' musí být před 'do'.",
 *   "path": "/api/inactivity/admin",
 *   "clientIp": "0:0:0:0:0:0:0:1",
 *   "details": {
 *     "field": "inactiveFrom",
 *     "reason": "must be before inactiveTo"
 *   }
 * }
 * </pre>
 */
public class ApiError {

    /**
     * Datum a čas vzniku chyby na serveru.
     * <p>
     * Formát {@code yyyy-MM-dd HH:mm:ss} usnadňuje čitelnost v logu i ve FE.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status kód (např. 400, 403, 404, 409, 500).
     */
    private int status;

    /**
     * Stručný textový popis statusu (např. "Bad Request", "Forbidden", "Conflict").
     */
    private String error;

    /**
     * Detailnější chybová zpráva – typicky pochází z {@link Exception#getMessage()}.
     * <p>
     * V doménových výjimkách je záměrně formulována tak,
     * aby byla použitelná přímo pro uživatele (přes FE).
     */
    private String message;

    /**
     * URL (path) požadavku, ve kterém chyba vznikla (např. {@code /api/matches/1}).
     */
    private String path;

    /**
     * IP adresa klienta, který požadavek odeslal.
     * <p>
     * Hodí se pro audit / diagnostiku (např. při řešení problémů uživatelů).
     */
    private String clientIp;

    /**
     * Volitelná mapa s doplňujícími detaily o chybě.
     * <p>
     * Typické použití:
     * <ul>
     *     <li>validační chyby formuláře (field → message),</li>
     *     <li>více chyb v jednom requestu (key → popis problému).</li>
     * </ul>
     * Může být {@code null}.
     */
    private Map<String, String> details;

    /**
     * Základní konstruktor pro chybovou odpověď bez detailní mapy.
     * <p>
     * {@code timestamp} se nastavuje automaticky na aktuální čas.
     */
    public ApiError(int status, String error, String message, String path,
                    String clientIp) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.clientIp = clientIp;
    }

    /**
     * Rozšířený konstruktor umožňující doplnit i {@code details} mapu.
     */
    public ApiError(int status,
                    String error,
                    String message,
                    String path,
                    String clientIp,
                    Map<String, String> details) {
        this(status, error, message, path, clientIp);
        this.details = details;
    }

    // ======================
    // GETTERY
    // ======================

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getClientIp() { return clientIp; }
    public Map<String, String> getDetails() { return details; }
}
