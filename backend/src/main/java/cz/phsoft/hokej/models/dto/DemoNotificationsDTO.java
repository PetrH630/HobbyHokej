package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.NotificationType;

import java.util.List;

/**
 * DTO pro přenos demo notifikací na frontend.
 *
 * Slouží k předání zachycených e-mailových a SMS notifikací,
 * které byly v demo režimu uloženy místo reálného odeslání.
 * Používá se zejména pro simulaci notifikačního systému
 * a pro zobrazení obsahu zpráv v uživatelském rozhraní.
 *
 * DTO neobsahuje žádnou business logiku. Slouží výhradně
 * jako datový přenosový objekt mezi servisní vrstvou
 * a prezentační vrstvou.
 */
public class DemoNotificationsDTO {

    private List<DemoEmailDTO> emails;
    private List<DemoSmsDTO> sms;

    /**
     * Vytvoří přenosový objekt obsahující seznam
     * demo e-mailů a SMS zpráv.
     *
     * @param emails seznam zachycených e-mailových zpráv
     * @param sms seznam zachycených SMS zpráv
     */
    public DemoNotificationsDTO(List<DemoEmailDTO> emails,
                                List<DemoSmsDTO> sms) {
        this.emails = emails;
        this.sms = sms;
    }

    /**
     * Vrátí seznam zachycených e-mailových zpráv.
     *
     * @return seznam demo e-mailů
     */
    public List<DemoEmailDTO> getEmails() {
        return emails;
    }

    /**
     * Vrátí seznam zachycených SMS zpráv.
     *
     * @return seznam demo SMS zpráv
     */
    public List<DemoSmsDTO> getSms() {
        return sms;
    }

    /**
     * DTO reprezentující jednu zachycenou e-mailovou zprávu
     * v demo režimu.
     *
     * Obsahuje základní metadata e-mailu včetně příznaku,
     * zda je tělo zprávy ve formátu HTML, a typu notifikace.
     */
    public static class DemoEmailDTO {

        private String to;
        private String subject;
        private String body;
        private boolean html;
        private NotificationType type;
        private String recipientKind;

        /**
         * Vytvoří přenosový objekt reprezentující demo e-mail.
         *
         * @param to e-mailová adresa příjemce
         * @param subject předmět zprávy
         * @param body obsah zprávy
         * @param html příznak, zda je obsah ve formátu HTML
         * @param type typ notifikace
         * @param recipientKind typ příjemce, například USER, PLAYER nebo MANAGER
         */
        public DemoEmailDTO(String to,
                            String subject,
                            String body,
                            boolean html,
                            NotificationType type,
                            String recipientKind) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.html = html;
            this.type = type;
            this.recipientKind = recipientKind;
        }

        /**
         * Vrátí e-mailovou adresu příjemce.
         *
         * @return e-mailová adresa
         */
        public String getTo() {
            return to;
        }

        /**
         * Vrátí předmět zprávy.
         *
         * @return předmět e-mailu
         */
        public String getSubject() {
            return subject;
        }

        /**
         * Vrátí obsah zprávy.
         *
         * @return text nebo HTML obsah e-mailu
         */
        public String getBody() {
            return body;
        }

        /**
         * Vrátí informaci, zda je obsah zprávy ve formátu HTML.
         *
         * @return true, pokud je obsah HTML
         */
        public boolean isHtml() {
            return html;
        }

        /**
         * Vrátí typ notifikace.
         *
         * @return typ notifikace
         */
        public NotificationType getType() {
            return type;
        }

        /**
         * Vrátí typ příjemce notifikace.
         *
         * @return typ příjemce
         */
        public String getRecipientKind() {
            return recipientKind;
        }
    }

    /**
     * DTO reprezentující jednu zachycenou SMS zprávu
     * v demo režimu.
     *
     * Obsahuje telefonní číslo příjemce,
     * text zprávy a typ notifikace.
     */
    public static class DemoSmsDTO {

        private String to;
        private String text;
        private NotificationType type;

        /**
         * Vytvoří přenosový objekt reprezentující demo SMS zprávu.
         *
         * @param to telefonní číslo příjemce
         * @param text text SMS zprávy
         * @param type typ notifikace
         */
        public DemoSmsDTO(String to,
                          String text,
                          NotificationType type) {
            this.to = to;
            this.text = text;
            this.type = type;
        }

        /**
         * Vrátí telefonní číslo příjemce.
         *
         * @return telefonní číslo
         */
        public String getTo() {
            return to;
        }

        /**
         * Vrátí text SMS zprávy.
         *
         * @return text zprávy
         */
        public String getText() {
            return text;
        }

        /**
         * Vrátí typ notifikace.
         *
         * @return typ notifikace
         */
        public NotificationType getType() {
            return type;
        }
    }
}
