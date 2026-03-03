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
 * Implementace SmsService využívající externí službu TextBee.
 *
 * Třída zapouzdřuje HTTP komunikaci s SMS API
 * a zajišťuje, že případné chyby při odesílání
 * neovlivní chod business vrstvy.
 */
@Service
public class SmsTextBeeService implements SmsService {

    private static final Logger log =
            LoggerFactory.getLogger(SmsTextBeeService.class);

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${textbee.api-url}")
    private String apiUrl;

    @Value("${textbee.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendSms(String phoneNumber, String message) {

        if (!smsEnabled) {
            log.info("SMS jsou vypnuté. Zpráva nebyla odeslána na {}", phoneNumber);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        Map<String, Object> body = Map.of(
                "recipients", List.of(phoneNumber),
                "message", message
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(apiUrl, request, String.class);

            log.info("SMS odeslána na {}, response: {}", phoneNumber, response.getBody());

        } catch (Exception e) {
            log.error("Chyba při odesílání SMS na {}: {}", phoneNumber, e.getMessage(), e);
        }
    }
}