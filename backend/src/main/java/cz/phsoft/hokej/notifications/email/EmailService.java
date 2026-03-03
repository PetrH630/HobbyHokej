package cz.phsoft.hokej.notifications.email;
/**
 * Rozhraní definující kontrakt pro odesílání e-mailových zpráv.
 *
 * Slouží jako abstraktní vrstva nad konkrétním poskytovatelem e-mailové služby.
 * Odděluje business logiku notifikací od technického způsobu odesílání.
 *
 * Implementace:
 * - nesmí propagovat výjimky do business vrstvy,
 * - může odesílat zprávy asynchronně,
 * - funguje v režimu best-effort.
 */
public interface EmailService {

    /**
     * Odešle jednoduchý textový e-mail.
     *
     * @param to e-mailová adresa příjemce
     * @param subject předmět zprávy
     * @param text textový obsah zprávy
     */
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * Odešle e-mail s HTML obsahem.
     *
     * @param to e-mailová adresa příjemce
     * @param subject předmět zprávy
     * @param htmlContent HTML obsah zprávy
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Odešle aktivační e-mail v textové podobě.
     */
    void sendActivationEmail(String to, String greetings, String activationLink);

    /**
     * Odešle aktivační e-mail v HTML podobě.
     */
    void sendActivationEmailHTML(String to, String greetings, String activationLink);

    /**
     * Odešle potvrzení o úspěšné aktivaci účtu v textové podobě.
     */
    void sendSuccesActivationEmail(String to, String greetings);

    /**
     * Odešle potvrzení o úspěšné aktivaci účtu v HTML podobě.
     */
    void sendSuccesActivationEmailHTML(String to, String greetings);
}
