package cz.phsoft.hokej.notifications.dto;

import cz.phsoft.hokej.notifications.enums.NotificationType;

import java.util.List;

/**
 * DTO pro přenos zachycených notifikací v demo režimu.
 *
 * Slouží k předání e-mailových a SMS zpráv, které byly
 * v demo režimu zachyceny místo jejich reálného odeslání.
 *
 * Třída neobsahuje žádnou aplikační logiku.
 * Slouží výhradně jako datový přenosový objekt
 * mezi servisní a prezentační vrstvou.
 */
public class DemoNotificationsDTO {

    private List<DemoEmailDTO> emails;
    private List<DemoSmsDTO> sms;

    /**
     * Vytváří přenosový objekt obsahující seznam
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
     * Vrací seznam zachycených e-mailových zpráv.
     *
     * @return seznam demo e-mailů
     */
    public List<DemoEmailDTO> getEmails() {
        return emails;
    }

    /**
     * Vrací seznam zachycených SMS zpráv.
     *
     * @return seznam demo SMS zpráv
     */
    public List<DemoSmsDTO> getSms() {
        return sms;
    }

    /**
     * DTO reprezentující jednu zachycenou e-mailovou zprávu.
     *
     * Obsahuje metadata e-mailu a informaci o typu notifikace.
     */
    public static class DemoEmailDTO {

        private String to;
        private String subject;
        private String body;
        private boolean html;
        private NotificationType type;
        private String recipientKind;

        /**
         * Vytváří přenosový objekt reprezentující demo e-mail.
         *
         * @param to e-mailová adresa příjemce
         * @param subject předmět zprávy
         * @param body obsah zprávy
         * @param html příznak, zda je obsah ve formátu HTML
         * @param type typ notifikace
         * @param recipientKind typ příjemce
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

        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public boolean isHtml() { return html; }
        public NotificationType getType() { return type; }
        public String getRecipientKind() { return recipientKind; }
    }

    /**
     * DTO reprezentující jednu zachycenou SMS zprávu.
     */
    public static class DemoSmsDTO {

        private String to;
        private String text;
        private NotificationType type;

        /**
         * Vytváří přenosový objekt reprezentující demo SMS zprávu.
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

        public String getTo() { return to; }
        public String getText() { return text; }
        public NotificationType getType() { return type; }
    }
}