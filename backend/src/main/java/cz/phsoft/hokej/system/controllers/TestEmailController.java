package cz.phsoft.hokej.system.controllers;

import cz.phsoft.hokej.notifications.email.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Testovací REST controller pro odeslání e-mailu.
 *
 * Slouží k ověření konfigurace e-mailové služby v prostředí,
 * například při vývoji nebo testování.
 *
 * Veškerá logika odesílání e-mailů se předává do {@link EmailService}.
 */
@RestController
@RequestMapping("/api/email/test")
public class TestEmailController {

    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Odesílá testovací e-mail na pevně danou adresu.
     *
     * Endpoint se používá pro ověření, že e-mailová služba je správně
     * nakonfigurována a že lze e-maily z backendu odesílat.
     *
     * @return textová zpráva o odeslání e-mailu
     */
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
