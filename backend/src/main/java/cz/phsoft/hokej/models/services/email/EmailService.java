package cz.phsoft.hokej.models.services.email;

/**
 * Rozhraní definující kontrakt pro odesílání emailových zpráv.
 *
 * Slouží jako abstrahovaná vrstva nad konkrétní implementací
 * emailového providera. Umožňuje oddělit business logiku
 * notifikací od technického způsobu odesílání emailů.
 *
 * Implementace rozhraní:
 * - nesmí propagovat chyby do business vrstvy,
 * - může odesílat emaily asynchronně,
 * - má fungovat v režimu best-effort.
 */
public interface EmailService {

    /**
     * Odešle jednoduchý textový email.
     */
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * Odešle email s HTML obsahem.
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Odešle textový aktivační email.
     */
    void sendActivationEmail(String to, String salutation, String activationLink);

    /**
     * Odešle HTML aktivační email.
     */
    void sendActivationEmailHTML(String to, String salutation, String activationLink);

    /**
     * Odešle textový email o úspěšné aktivaci účtu.
     */
    void sendSuccesActivationEmail(String to, String salutation);

    /**
     * Odešle HTML email o úspěšné aktivaci účtu.
     */
    void sendSuccesActivationEmailHTML(String to, String salutation);
}
