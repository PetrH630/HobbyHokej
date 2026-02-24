package cz.phsoft.hokej.models.dto.requests;

import java.util.List;

/**
 * DTO pro vytvoření speciální zprávy administrátorem.
 *
 * Obsahuje text zprávy, seznam cílových příjemců
 * a volbu komunikačních kanálů.
 */
public class SpecialNotificationRequestDTO {

    /**
     * Nadpis notifikace, který se používá v UI a v e-mailech.
     */
    private String title;

    /**
     * Text zprávy, který se používá v UI, e-mailech a SMS.
     */
    private String message;

    /**
     * Příznak, zda se má zpráva odeslat také e-mailem.
     */
    private boolean sendEmail;

    /**
     * Příznak, zda se má zpráva odeslat také SMS.
     */
    private boolean sendSms;

    /**
     * Cílové kombinace uživatele a hráče.
     */
    private List<Target> targets;

    public static class Target {
        private Long userId;
        private Long playerId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Long playerId) {
            this.playerId = playerId;
        }
    }

    // gettery/settery

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isSendSms() {
        return sendSms;
    }

    public void setSendSms(boolean sendSms) {
        this.sendSms = sendSms;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }
}