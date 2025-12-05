package cz.phsoft.hokej.models.services.sms;

import cz.phsoft.hokej.models.services.sms.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SmsTextBeeService implements SmsService {

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
            System.out.println("SMS disabled, message not sent: " + message);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        Map<String, Object> body = Map.of(
                "recipients", List.of(phoneNumber),
                "message", message
        );

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            System.out.println("SMS odeslána hráči: " + phoneNumber + ", response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Chyba při odesílání SMS: " + e.getMessage());
        }
    }
}
