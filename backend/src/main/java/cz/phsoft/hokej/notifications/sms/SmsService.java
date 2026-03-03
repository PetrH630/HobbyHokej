package cz.phsoft.hokej.notifications.sms;

/**
 * Rozhraní definující kontrakt pro odesílání SMS zpráv.
 *
 * Business vrstva aplikace pracuje výhradně s tímto rozhraním
 * a není závislá na konkrétním poskytovateli SMS služby.
 */
public interface SmsService {

    /**
     * Odesílá SMS zprávu na zadané telefonní číslo.
     *
     * @param phoneNumber telefonní číslo příjemce
     * @param message text SMS zprávy
     */
    void sendSms(String phoneNumber, String message);
}