package cz.phsoft.hokej.models.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementace EmailService pro odesílání emailů přes SMTP (WEDOS).
 *
 * Třída zapouzdřuje práci s JavaMailSenderem a představuje
 * centrální místo pro veškerou emailovou komunikaci aplikace.
 *
 * Odesílání emailů probíhá asynchronně a je možné jej globálně
 * vypnout pomocí konfigurační vlastnosti.
 */
@Service
public class EmailWedosService implements EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailWedosService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    public EmailWedosService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Odešle jednoduchý textový email.
     *
     * Pokud jsou emaily globálně vypnuté, zpráva se pouze zaloguje.
     * Chyby při odesílání nejsou propagovány do vyšších vrstev.
     */
    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {

        if (!emailEnabled) {
            log.info("Email je vypnutý – zpráva nebyla odeslána na {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.debug("Textový email byl odeslán na {}", to);

        } catch (Exception e) {
            log.error("Chyba při odesílání emailu na {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Odešle HTML email.
     *
     * Používá se například pro aktivační a notifikační emaily.
     */
    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {

        if (!emailEnabled) {
            log.info("Email je vypnutý – HTML zpráva nebyla odeslána na {}", to);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);

            log.debug("HTML email byl odeslán na {}", to);

        } catch (MessagingException e) {
            log.error("Chyba při odesílání HTML emailu na {}: {}", to, e.getMessage(), e);
        }
    }

    // Konkrétní emaily – aktivace účtu

    @Override
    public void sendActivationEmail(String to, String greetings, String activationLink) {
        String subject = "Potvrzení registrace – Hokej Stará Garda";
        String text =
                "Dobrý den, " + greetings + "\n\n" +
                        "Pro aktivaci účtu klikněte na následující odkaz:\n" +
                        activationLink + "\n\n" +
                        "Platnost odkazu je 24 hodin.";

        sendSimpleEmail(to, subject, text);
    }

    @Override
    @Async
    public void sendActivationEmailHTML(String to, String greetings, String activationLink) {
        String subject = "Potvrzení registrace – Hokej Stará Garda";
        String html =
                "<p>Dobrý den,</p>" +
                        "<p>" + greetings + "</p>" +
                        "<p>Pro aktivaci účtu klikněte na následující odkaz:</p>" +
                        "<a href=\"" + activationLink + "\">Aktivovat účet</a>";

        sendHtmlEmail(to, subject, html);
    }

    @Override
    @Async
    public void sendSuccesActivationEmail(String to, String greetings) {
        String subject = "Účet byl aktivován – Hokej Stará Garda";
        String text =
                "Dobrý den, " + greetings + "\n\n" +
                        "Váš účet byl úspěšně aktivován.";

        sendSimpleEmail(to, subject, text);
    }

    @Override
    @Async
    public void sendSuccesActivationEmailHTML(String to, String greetings) {
        String subject = "Účet byl aktivován – Hokej Stará Garda";
        String html =
                "<p>Dobrý den,</p>" +
                        "<p>" + greetings + "</p>" +
                        "<p>Váš účet byl úspěšně aktivován.</p>";

        sendHtmlEmail(to, subject, html);
    }
}
