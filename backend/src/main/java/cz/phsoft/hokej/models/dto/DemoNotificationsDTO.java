package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.NotificationType;

import java.util.List;

/**
 * DTO pro přenos demo notifikací na frontend.
 *
 * Obsahuje seznam zachycených e-mailů a SMS zpráv,
 * které byly v demo režimu uloženy místo reálného odeslání.
 */
public class DemoNotificationsDTO {

    private List<DemoEmailDTO> emails;
    private List<DemoSmsDTO> sms;

    public DemoNotificationsDTO(List<DemoEmailDTO> emails,
                                List<DemoSmsDTO> sms) {
        this.emails = emails;
        this.sms = sms;
    }

    public List<DemoEmailDTO> getEmails() {
        return emails;
    }

    public List<DemoSmsDTO> getSms() {
        return sms;
    }

    // ============================================
    // VNITŘNÍ DTO PRO EMAIL
    // ============================================

    public static class DemoEmailDTO {

        private String to;
        private String subject;
        private String body;
        private boolean html;
        private NotificationType type;
        private String recipientKind; // USER / PLAYER / MANAGER

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

        public String getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public boolean isHtml() {
            return html;
        }

        public NotificationType getType() {
            return type;
        }

        public String getRecipientKind() {
            return recipientKind;
        }
    }

    // ============================================
    // VNITŘNÍ DTO PRO SMS
    // ============================================

    public static class DemoSmsDTO {

        private String to;
        private String text;
        private NotificationType type;

        public DemoSmsDTO(String to,
                          String text,
                          NotificationType type) {
            this.to = to;
            this.text = text;
            this.type = type;
        }

        public String getTo() {
            return to;
        }

        public String getText() {
            return text;
        }

        public NotificationType getType() {
            return type;
        }
    }
}
