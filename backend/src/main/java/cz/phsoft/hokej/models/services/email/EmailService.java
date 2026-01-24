package cz.phsoft.hokej.models.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;                         // NEW
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${email.enabled:true}")   //
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ===== 1) JEDNODUCHÝ TEXTOVÝ EMAIL =====
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {

        if (!emailEnabled) {
            log.info("MAIL JE VYPNUTÝ – email nebyl odeslán na: {}", to);
            // pro testování
            System.out.println("MAIL JE VYPNUTÝ – email nebyl odeslán na: " + to);
            return;
        }


        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromEmail);

            mailSender.send(message);
        } catch (Exception e) {
            // Doporučeno: logovat do souboru
            // pro testování
            System.out.println("Chyba při odesílání emailu na: " + to);
            log.error("Chyba při odesílání emailu na {}: {}", to, e.getMessage(), e);
        }
    }

    // ===== 2) HTML EMAIL (HEZČÍ) =====
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {

        if (!emailEnabled) {
            // CHANGED: System.out -> logger
            log.info("MAIL JE VYPNUTÝ – HTML email nebyl odeslán na: {}", to);
            // pro testování
            System.out.println("MAIL JE VYPNUTÝ – email nebyl odeslán na: " + to);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.debug("HTML email odeslán na {} se subjectem '{}'", to, subject);  // NEW
        } catch (MessagingException e) {
            // CHANGED: opět jen logujeme
            log.error("Chyba při odesílání HTML emailu na {}: {}", to, e.getMessage(), e);
            System.out.println("Chyba při odesílání HTML emailu na: " + to);
        }
    }

    public void sendActivationEmail(String to, String activationLink) {
        String subject = "Potvrďte svůj účet";
        String text = "Dobrý den,\n\n"
                + "Klikněte na tento odkaz pro aktivaci účtu:\n"
                + activationLink + "\n\n"
                + "Platnost odkazu: 24 hodin.\n\n"
                + "Děkujeme!";
        sendSimpleEmail(to, subject, text);
    }

    @Async
    public void sendActivationEmailHTML(String to, String activationLink) {
        String subject = "Potvrzení registrace - App - Hokej Stará Garda";
        String html = "<p>Děkujeme za registraci.</p>" +
                "<p>Klikněte na odkaz pro aktivaci účtu:</p>" +
                "<a href=\"" + activationLink + "\">Aktivovat účet</a>";

        sendHtmlEmail(to, subject, html);
    }

}