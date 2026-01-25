package cz.phsoft.hokej.models.services.sms;

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
 * Implementace {@link SmsService} využívající externí službu TextBee.
 * <p>
 * Tato service představuje jediný vstupní bod pro odesílání SMS zpráv
 * v aplikaci. Business vrstvy pracují výhradně s rozhraním
 * {@link SmsService} a nejsou závislé na konkrétním SMS providerovi.
 * </p>
 *
 * Odpovědnost:
 * <ul>
 *     <li>odesílání SMS zpráv přes TextBee API,</li>
 *     <li>respektování globálního přepínače {@code sms.enabled},</li>
 *     <li>jednotné logování úspěchů i chyb,</li>
 *     <li>zajištění, že selhání SMS neovlivní business logiku.</li>
 * </ul>
 *
 * Technické poznámky:
 * <ul>
 *     <li>integrace probíhá pomocí REST API služby TextBee,</li>
 *     <li>chyby jsou zachyceny a nejsou propagovány výše,</li>
 *     <li>service je vhodná jak pro produkci, tak pro dev/test režim.</li>
 * </ul>
 */
@Service
public class SmsTextBeeService implements SmsService {

    private static final Logger log =
            LoggerFactory.getLogger(SmsTextBeeService.class);

    /**
     * Globální zapnutí / vypnutí SMS.
     * <p>
     * Typické použití:
     * </p>
     * <ul>
     *     <li>{@code true} – produkční prostředí (SMS se skutečně odesílají),</li>
     *     <li>{@code false} – lokální vývoj / testy (SMS se pouze logují).</li>
     * </ul>
     */
    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    /**
     * URL endpointu TextBee API.
     */
    @Value("${textbee.api-url}")
    private String apiUrl;

    /**
     * API klíč pro autentizaci vůči TextBee.
     * <p>
     * Klíč se odesílá v HTTP hlavičce {@code x-api-key}.
     * </p>
     */
    @Value("${textbee.api-key}")
    private String apiKey;

    /**
     * HTTP klient pro komunikaci s TextBee API.
     * <p>
     * Pro jednoduchost je vytvořen přímo zde. V případě potřeby
     * lze tuto implementaci snadno nahradit injektovaným {@code @Bean}
     * nebo {@code WebClientem}.
     * </p>
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Odešle SMS zprávu na zadané telefonní číslo.
     * <p>
     * Chování metody:
     * </p>
     * <ul>
     *     <li>pokud jsou SMS globálně vypnuté, zpráva se neodesílá, pouze se zaloguje,</li>
     *     <li>jinak se odešle HTTP POST požadavek na TextBee API,</li>
     *     <li>jakákoli chyba při odesílání je zachycena a zalogována.</li>
     * </ul>
     *
     * Důležité:
     * <ul>
     *     <li>výjimky nejsou propagovány do business vrstvy,</li>
     *     <li>odesílání SMS je „best-effort“ operace,</li>
     *     <li>selhání SMS nesmí shodit aplikaci.</li>
     * </ul>
     *
     * @param phoneNumber cílové telefonní číslo
     * @param message     text SMS zprávy
     */
    @Override
    public void sendSms(String phoneNumber, String message) {

        // Globální vypnutí SMS (typicky dev / test)
        if (!smsEnabled) {
            log.info(
                    "SMS jsou globálně vypnuté (sms.enabled=false). Zpráva NEODESLÁNA. Tel: {}, msg: {}",
                    phoneNumber, message
            );

            System.out.println(
                    "SMS disabled (global), message not sent: "
                            + phoneNumber + " -> " + message
            );
            return;
        }

        // HTTP hlavičky
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        // Tělo requestu
        Map<String, Object> body = Map.of(
                "recipients", List.of(phoneNumber),
                "message", message
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            // Odeslání požadavku na TextBee API
            ResponseEntity<String> response =
                    restTemplate.postForEntity(apiUrl, request, String.class);

            log.info(
                    "SMS odeslána (TextBee) na {}, response: {}",
                    phoneNumber, response.getBody()
            );

            // Paralelní výstup do konzole (užitečné zejména v DEV)
            System.out.println(
                    "SMS odeslána (TextBee) na " + phoneNumber +
                            ", response: " + response.getBody()
            );

        } catch (Exception e) {
            // Chyby při odesílání SMS nesmí ovlivnit chod aplikace
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
