package cz.phsoft.hokej.models.services.sms;

/**
 * Rozhraní definující kontrakt pro odesílání SMS zpráv v aplikaci.
 *
 * Rozhraní slouží jako jednotný vstupní bod pro odesílání SMS
 * bez vazby na konkrétní technickou implementaci nebo poskytovatele služby.
 * Business logika aplikace pracuje výhradně s tímto rozhraním
 * a není závislá na detailech odesílání zpráv.
 *
 * Odpovědnost rozhraní:
 * - definování operace pro odeslání SMS zprávy,
 * - oddělení business logiky od technické implementace,
 * - umožnění snadné výměny nebo rozšíření SMS providerů.
 *
 * Rozhraní je typicky používáno ve službách aplikační vrstvy,
 * například v notifikačních službách nebo plánovaných schedulerech.
 */
public interface SmsService {

    /**
     * Odesílá SMS zprávu na zadané telefonní číslo.
     *
     * Metoda představuje abstraktní operaci odeslání SMS
     * a nezaručuje její skutečné doručení koncovému příjemci.
     * Způsob odeslání, práce s externí službou a případné chyby
     * jsou plně v odpovědnosti konkrétní implementace rozhraní.
     *
     * Očekávané chování implementace:
     * - selhání odeslání SMS nesmí ovlivnit chod aplikace,
     * - technické chyby by měly být zachyceny a zalogovány,
     * - odesílání SMS je považováno za best-effort operaci.
     *
     * @param phoneNumber telefonní číslo příjemce SMS zprávy
     * @param message text SMS zprávy
     */
    void sendSms(String phoneNumber, String message);
}
