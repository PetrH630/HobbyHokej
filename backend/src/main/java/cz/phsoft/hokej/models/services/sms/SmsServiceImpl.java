/* package cz.phsoft.hokej.models.services.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SmsServiceImpl {

   // private final RestTemplate restTemplate = new RestTemplate();

    @Value("${textbee.api-url}")
    private String apiUrl;

    @Value("${textbee.api-key}")
    private String apiKey;

    // testovací číslo
    //private final String testNumber = "+420776609956";

    /**
     * Odešle SMS na testovací číslo.
     * @param message text zprávy
     */

/*
    public void sendSms(String phoneNumber, String message) {
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
            System.out.println("SMS poslána na testovací číslo  xxx   : " + response.getBody());
        } catch (Exception e) {
            System.err.println("Chyba při odesílání SMS: " + e.getMessage());
        }
    }

*/