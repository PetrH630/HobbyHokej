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
 * Služba pro odesílání emailových notifikací.
 * <p>
 * Odpovědnosti:
 * <ul>
 *     <li>odesílání jednoduchých textových emailů,</li>
 *     <li>odesílání HTML emailů (např. aktivační emaily),</li>
 *     <li>zapouzdření práce s {@link JavaMailSender},</li>
 *     <li>centrální místo pro emailovou komunikaci aplikace.</li>
 * </ul>
 *
 * Vlastnosti:
 * <ul>
 *     <li>odesílání probíhá asynchronně ({@link Async}),</li>
 *     <li>emaily lze globálně vypnout pomocí konfigurace ({@code email.enabled}),</li>
 *     <li>chyby při odesílání NIKDY neshodí aplikaci – pouze se zalogují.</li>
 * </ul>
 *
 * Tato třída:
 * <ul>
 *     <li>neřeší business logiku (kdy a komu se email posílá),</li>
 *     <li>neřeší šablonování (lze později rozšířit např. o Thymeleaf).</li>
 * </ul>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Spring JavaMailSender.
     * <p>
     * Konfigurace (SMTP server, port, auth, atd.)
     * se načítá z application.properties / application.yml.
     */
    private final JavaMailSender mailSender;

    /**
     * Výchozí email odesílatele (FROM).
     */
    @Value("${spring.mail.from}")
    private String fromEmail;

    /**
     * Globální přepínač pro zapnutí / vypnutí emailů.
     * <p>
     * Vhodné pro:
     * <ul>
     *     <li>lokální vývoj,</li>
     *     <li>testovací prostředí,</li>
     *     <li>ladění bez reálného odesílání.</li>
     * </ul>
     */
    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ====================================================
    // 1) JEDNODUCHÝ TEXTOVÝ EMAIL
    // ====================================================

    /**
     * Odešle jednoduchý textový email.
     * <p>
     * Odesílání probíhá asynchronně – request nečeká
     * na odpověď SMTP serveru.
     *
     * @param to      cílová emailová adresa
     * @param subject předmět emailu
     * @param text    textový obsah emailu
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {

        // globální vypnutí emailů (např. v dev/test prostředí)
        if (!emailEnabled) {
            log.info("MAIL JE VYPNUTÝ – email nebyl odeslán na: {}", to);
            // pomocný výpis pro lokální ladění
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

            System.out.println("Email byl odeslán na " + to);

        } catch (Exception e) {
            // chyby při odesílání emailů NESMÍ shodit aplikaci
            log.error("Chyba při odesílání emailu na {}: {}", to, e.getMessage(), e);
            System.out.println("Chyba při odesílání emailu na: " + to);
        }
    }

    // ====================================================
    // 2) HTML EMAIL
    // ====================================================

    /**
     * Odešle HTML email.
     * <p>
     * Používá se např. pro:
     * <ul>
     *     <li>aktivační emaily,</li>
     *     <li>vizuálně bohatší notifikace.</li>
     * </ul>
     *
     * @param to          cílová emailová adresa
     * @param subject     předmět emailu
     * @param htmlContent HTML obsah zprávy
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {

        // globální vypnutí emailů
        if (!emailEnabled) {
            log.info("MAIL JE VYPNUTÝ – HTML email nebyl odeslán na: {}", to);
            System.out.println("MAIL JE VYPNUTÝ – email nebyl odeslán na: " + to);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML obsah
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            System.out.println("Email byl odeslán na " + to);

            log.debug(
                    "HTML email odeslán na {} se subjectem '{}'",
                    to,
                    subject
            );

        } catch (MessagingException e) {
            // opět pouze logujeme, aplikace běží dál
            log.error(
                    "Chyba při odesílání HTML emailu na {}: {}",
                    to,
                    e.getMessage(),
                    e
            );
            System.out.println("Chyba při odesílání HTML emailu na: " + to);
        }
    }

    // ====================================================
    // KONKRÉTNÍ EMAILY – AKTIVACE ÚČTU
    // ====================================================

    /**
     * Odešle jednoduchý textový aktivační email.
     *
     * @param to             cílový email
     * @param activationLink aktivační odkaz
     */
    public void sendActivationEmail(String to, String activationLink) {
        String subject = "Potvrďte svůj účet";
        String text =
                "Dobrý den,\n\n" +
                        "Klikněte na tento odkaz pro aktivaci účtu:\n" +
                        activationLink + "\n\n" +
                        "Platnost odkazu: 24 hodin.\n\n" +
                        "Děkujeme!";

        sendSimpleEmail(to, subject, text);
    }

    /**
     * Odešle HTML aktivační email.
     *
     * @param to             cílový email
     * @param activationLink aktivační odkaz
     */
    @Async
    public void sendActivationEmailHTML(String to, String activationLink) {
        String subject = "Potvrzení registrace - App - Hokej Stará Garda";
        String html =
                "<p>Děkujeme za registraci.</p>" +
                        "<p>Klikněte na odkaz pro aktivaci účtu:</p>" +
                        "<a href=\"" + activationLink + "\">Aktivovat účet</a>";

        sendHtmlEmail(to, subject, html);
    }
}
