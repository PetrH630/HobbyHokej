package cz.phsoft.hokej.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO reprezentující jednotný formát chybové odpovědi backendu.
 *
 * Používá se v globálním handleru výjimek
 * (cz.phsoft.hokej.config.GlobalExceptionHandler) pro serializaci
 * výjimek do JSON odpovědi, kterou zpracovává frontend.
 *
 * Struktura odpovědi:
 * - timestamp: čas vzniku chyby na serveru,
 * - status: HTTP status kód (například 400, 403, 409, 500),
 * - error: stručný textový popis statusu (například "Bad Request"),
 * - message: detailnější zpráva (typicky z výjimky),
 * - path: URL, kde k chybě došlo,
 * - clientIp: IP adresa klienta, který požadavek poslal,
 * - details: volitelné doplňující informace (například validační chyby).
 *
 * Tato struktura umožňuje frontend části jednotně zpracovávat chyby
 * a zobrazovat uživatelsky přívětivé zprávy.
 */
public class ApiError {

    /**
     * Datum a čas vzniku chyby na serveru.
     *
     * Formát "yyyy-MM-dd HH:mm:ss" usnadňuje čitelnost v logu
     * i ve frontendové části.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status kód (například 400, 403, 404, 409, 500).
     */
    private int status;

    /**
     * Stručný textový popis statusu (například "Bad Request", "Forbidden", "Conflict").
     */
    private String error;

    /**
     * Detailnější chybová zpráva.
     *
     * Typicky pochází z metody Exception.getMessage().
     * V doménových výjimkách je záměrně formulována tak,
     * aby byla použitelná přímo pro uživatele.
     */
    private String message;

    /**
     * URL (path) požadavku, ve kterém chyba vznikla
     * (například "/api/matches/1").
     */
    private String path;

    /**
     * IP adresa klienta, který požadavek odeslal.
     *
     * Hodí se pro audit a diagnostiku, například při řešení
     * problémů konkrétních uživatelů.
     */
    private String clientIp;

    /**
     * Volitelná mapa s doplňujícími detaily o chybě.
     *
     * Typické použití:
     * - validační chyby formuláře (klíč pole → chybová zpráva),
     * - více chyb v jednom requestu (klíč → popis problému).
     *
     * Může být null.
     */
    private Map<String, String> details;

    /**
     * Základní konstruktor pro chybovou odpověď bez detailní mapy.
     *
     * Čas vzniku chyby (timestamp) se nastavuje automaticky
     * na aktuální čas.
     */
    public ApiError(int status,
                    String error,
                    String message,
                    String path,
                    String clientIp) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.clientIp = clientIp;
    }

    /**
     * Rozšířený konstruktor umožňující doplnit i mapu details.
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

    // gettery a setter

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getClientIp() {
        return clientIp;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
