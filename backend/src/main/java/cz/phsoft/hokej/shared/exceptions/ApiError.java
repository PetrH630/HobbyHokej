package cz.phsoft.hokej.shared.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO reprezentující jednotný formát chybové odpovědi backendu.
 *
 * Používá se v globálním handleru výjimek pro serializaci
 * výjimek do JSON odpovědi, kterou následně zpracovává frontend.
 *
 * Struktura odpovědi obsahuje čas vzniku chyby, HTTP status,
 * stručný název chyby, detailní zprávu, cestu požadavku,
 * IP adresu klienta a volitelné doplňující detaily.
 *
 * Tato struktura umožňuje jednotné zpracování chyb
 * v klientské části aplikace.
 */
public class ApiError {

    /**
     * Datum a čas vzniku chyby na serveru.
     *
     * Formát "yyyy-MM-dd HH:mm:ss" usnadňuje čitelnost
     * v logu i ve frontendové části.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status kód odpovědi.
     */
    private int status;

    /**
     * Stručný textový popis statusu.
     */
    private String error;

    /**
     * Detailnější chybová zpráva.
     *
     * Typicky pochází z metody Exception.getMessage().
     */
    private String message;

    /**
     * URL cesta požadavku, ve kterém chyba vznikla.
     */
    private String path;

    /**
     * IP adresa klienta, který požadavek odeslal.
     */
    private String clientIp;

    /**
     * Volitelná mapa s doplňujícími detaily o chybě.
     *
     * Používá se například pro validační chyby formuláře.
     */
    private Map<String, String> details;

    /**
     * Vytvoří chybovou odpověď bez detailů.
     *
     * @param status HTTP status kód odpovědi
     * @param error krátký název chyby
     * @param message popisná zpráva chyby určená pro klienta
     * @param path cesta požadavku, ve které k chybě došlo
     * @param clientIp IP adresa klienta
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
     * Vytvoří chybovou odpověď včetně detailů.
     *
     * @param status HTTP status kód odpovědi
     * @param error krátký název chyby
     * @param message popisná zpráva chyby určená pro klienta
     * @param path cesta požadavku, ve které k chybě došlo
     * @param clientIp IP adresa klienta
     * @param details mapa detailních informací o chybě
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

    /**
     * Vrací čas vzniku chybové odpovědi.
     *
     * @return čas vytvoření odpovědi
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Vrací HTTP status kód odpovědi.
     *
     * @return HTTP status kód
     */
    public int getStatus() {
        return status;
    }

    /**
     * Vrací stručný název chyby.
     *
     * @return název chyby
     */
    public String getError() {
        return error;
    }

    /**
     * Vrací detailní zprávu chyby.
     *
     * @return text chybové zprávy
     */
    public String getMessage() {
        return message;
    }

    /**
     * Vrací cestu požadavku, kde chyba vznikla.
     *
     * @return URL cesta
     */
    public String getPath() {
        return path;
    }

    /**
     * Vrací IP adresu klienta.
     *
     * @return IP adresa klienta
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Vrací detailní informace o chybě.
     *
     * @return mapa detailů nebo null
     */
    public Map<String, String> getDetails() {
        return details;
    }

    /**
     * Nastaví detailní informace o chybě.
     *
     * @param details mapa detailních informací
     */
    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}