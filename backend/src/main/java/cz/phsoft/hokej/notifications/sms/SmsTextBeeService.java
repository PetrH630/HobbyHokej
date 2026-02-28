package cz.phsoft.hokej.notifications.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Implementace rozhraní SmsService využívající externí SMS službu TextBee.
 *
 * Třída představuje technickou implementaci odesílání SMS zpráv.
 * Business vrstvy aplikace pracují výhradně s rozhraním SmsService
 * a nejsou závislé na konkrétním SMS providerovi ani na detailech
 * HTTP komunikace.
 *
 * Odpovědnost třídy:
 * - odesílání SMS zpráv prostřednictvím TextBee API,
 * - respektování globálního nastavení zapnutí nebo vypnutí SMS,
 * - jednotné logování úspěšných i neúspěšných pokusů o odeslání,
 * - zajištění, že technické selhání SMS neovlivní business logiku aplikace.
 *
 * Třída je navržena tak, aby byla snadno nahraditelná jinou implementací
 * SmsService bez zásahu do vyšších vrstev aplikace.
 */
@Service
public class SmsTextBeeService implements SmsService {

    private static final Logger log =
            LoggerFactory.getLogger(SmsTextBeeService.class);

    /**
     * Globální přepínač pro zapnutí nebo vypnutí odesílání SMS.
     *
     * Hodnota se načítá z konfiguračních properties aplikace.
     * V produkčním prostředí je obvykle nastavena na true,
     * v lokálním vývoji nebo testech může být nastavena na false.
     */
    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    /**
     * URL endpointu TextBee API.
     *
     * Používá se jako cílová adresa pro HTTP POST požadavky
     * při odesílání SMS zpráv.
     */
    @Value("${textbee.api-url}")
    private String apiUrl;

    /**
     * API klíč pro autentizaci vůči službě TextBee.
     *
     * Klíč se odesílá v HTTP hlavičce každého požadavku
     * a slouží k autorizaci aplikace vůči externí službě.
     */
    @Value("${textbee.api-key}")
    private String apiKey;

    /**
     * HTTP klient používaný pro komunikaci s TextBee API.
     *
     * Pro jednoduchost je instancován přímo v této třídě.
     * V případě potřeby lze tuto implementaci nahradit
     * konfigurovaným Beanem nebo WebClientem.
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Odesílá SMS zprávu na zadané telefonní číslo.
     *
     * Metoda nejprve ověřuje globální nastavení odesílání SMS.
     * Pokud jsou SMS vypnuté, zpráva se neodesílá a událost se pouze zaloguje.
     * Pokud jsou SMS povolené, vytvoří se HTTP POST požadavek
     * a odešle se na TextBee API.
     *
     * Veškeré výjimky vzniklé během odesílání jsou zachyceny
     * a nejsou propagovány do vyšších vrstev aplikace.
     * Odesílání SMS je považováno za best-effort operaci,
     * jejíž selhání nesmí ohrozit stabilitu systému.
     *
     * @param phoneNumber cílové telefonní číslo příjemce
     * @param message text SMS zprávy
     */
    @Override
    public void sendSms(String phoneNumber, String message) {

        // Ověření globálního nastavení odesílání SMS.
        if (!smsEnabled) {
            log.info(
                    "SMS jsou globálně vypnuté. Zpráva nebyla odeslána. Tel: {}, msg: {}",
                    phoneNumber, message
            );

            System.out.println(
                    "SMS disabled (global), message not sent: "
                            + phoneNumber + " -> " + message
            );
            return;
        }

        // Příprava HTTP hlaviček požadavku.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        // Příprava těla požadavku dle specifikace TextBee API.
        Map<String, Object> body = Map.of(
                "recipients", List.of(phoneNumber),
                "message", message
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            // Odeslání HTTP POST požadavku na TextBee API.
            ResponseEntity<String> response =
                    restTemplate.postForEntity(apiUrl, request, String.class);

            log.info(
                    "SMS úspěšně odeslána přes TextBee na {}, response: {}",
                    phoneNumber, response.getBody()
            );

            System.out.println(
                    "SMS odeslána (TextBee) na " + phoneNumber +
                            ", response: " + response.getBody()
            );

        } catch (Exception e) {
            // Selhání odeslání SMS nesmí ovlivnit chod aplikace.
            log.error(
                    "Chyba při odesílání SMS přes TextBee na {}: {}",
                    phoneNumber, e.getMessage(), e
            );

            System.err.println(
                    "Chyba při odesílání SMS přes TextBee na "
                            + phoneNumber + ": " + e.getMessage()
            );
        }
    }
}
