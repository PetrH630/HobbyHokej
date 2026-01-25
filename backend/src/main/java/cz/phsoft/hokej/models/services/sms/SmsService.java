package cz.phsoft.hokej.models.services.sms;

/**
 * Rozhraní pro odesílání SMS zpráv v aplikaci.
 * <p>
 * Definuje kontrakt pro odesílání SMS bez závislosti
 * na konkrétním technickém řešení nebo poskytovateli služby.
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>poskytnout jednotný vstupní bod pro odesílání SMS,</li>
 *     <li>oddělit business logiku od technické implementace,</li>
 *     <li>umožnit snadnou výměnu nebo rozšíření SMS providerů.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se v business službách (např. NotificationService, schedulery),</li>
 *     <li>implementace zajišťuje konkrétní způsob odeslání SMS.</li>
 * </ul>
 *
 * Implementační poznámky:
 * <ul>
 *     <li>implementace by měla být odolná vůči chybám externích služeb,</li>
 *     <li>výjimky by neměly být propagovány do business vrstvy,</li>
 *     <li>odesílání SMS je typicky „best-effort“ operace.</li>
 * </ul>
 */
public interface SmsService {

    /**
     * Odešle SMS zprávu na zadané telefonní číslo.
     * <p>
     * Metoda představuje abstraktní operaci odeslání SMS
     * a nezaručuje její skutečné doručení koncovému uživateli.
     * </p>
     *
     * Očekávané chování implementace:
     * <ul>
     *     <li>validní formát telefonního čísla je předpokladem,</li>
     *     <li>selhání odeslání SMS nesmí shodit aplikaci,</li>
     *     <li>chyby by měly být zalogovány.</li>
     * </ul>
     *
     * @param phoneNumber cílové telefonní číslo příjemce
     * @param message     text SMS zprávy
     */
    void sendSms(String phoneNumber, String message);
}
