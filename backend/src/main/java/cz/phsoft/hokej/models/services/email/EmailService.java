package cz.phsoft.hokej.models.services.email;

/**
 * Rozhraní pro odesílání emailových notifikací.
 *
 * Účel:
 * <ul>
 *     <li>poskytnout jednotný vstupní bod pro odesílání emailů,</li>
 *     <li>oddělit business logiku od konkrétní implementace (WEDOS, jiný provider),</li>
 *     <li>umožnit snadnou výměnu implementace bez změny business kódu.</li>
 * </ul>
 *
 * Implementační poznámky:
 * <ul>
 *     <li>odesílání emailu je „best-effort“ operace,</li>
 *     <li>výjimky by se neměly propagovat do business vrstvy,</li>
 *     <li>implementace může odesílat asynchronně.</li>
 * </ul>
 */
public interface EmailService {

    /**
     * Odešle jednoduchý textový email.
     *
     * @param to      cílová emailová adresa
     * @param subject předmět emailu
     * @param text    textový obsah emailu
     */
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * Odešle HTML email.
     *
     * @param to          cílová emailová adresa
     * @param subject     předmět emailu
     * @param htmlContent HTML obsah zprávy
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Odešle jednoduchý textový aktivační email.
     *
     * @param to             cílový email
     * @param activationLink aktivační odkaz
     */
    void sendActivationEmail(String to, String salutation, String activationLink);

    /**
     * Odešle HTML aktivační email.
     *
     * @param to             cílový email
     * @param activationLink aktivační odkaz
     */
    void sendActivationEmailHTML(String to, String salutation, String activationLink);

    /**
     * Odešle jednoduchý textový aktivační email.
     *
     * @param to             cílový email
     *
     */
    void sendSuccesActivationEmail(String to, String salutation);

    /**
     * Odešle HTML aktivační email.
     *
     * @param to             cílový email
     *
     */
    void sendSuccesActivationEmailHTML(String to, String salutation);
}
