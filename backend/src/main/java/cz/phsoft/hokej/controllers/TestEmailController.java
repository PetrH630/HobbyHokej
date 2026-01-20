package cz.phsoft.hokej.controllers;


import cz.phsoft.hokej.models.services.email.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email/test")
public class TestEmailController {
    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-mail")
    public String sendTestMail() {
        emailService.sendSimpleEmail(
                "petrhlista@seznam.cz",
                "APP - Testovací email",
                "Ahoj, toto je test z backendu."
        );
        return "Email odeslán";
    }

}
