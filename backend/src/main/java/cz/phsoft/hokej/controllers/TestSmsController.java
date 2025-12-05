package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.services.SmsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSmsController {

    private final SmsService smsService;

    public TestSmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Testovací endpoint pro odeslání SMS na testovací číslo.
     * Zavolej: GET /api/test-sms
     */
    @GetMapping("/api/test-sms")
    public String sendTestSms() {
        smsService.sendSms("Testovací SMS z aplikace Spring Boot!");
        return "SMS byla odeslána na testovací číslo +420776609956";
    }
}
