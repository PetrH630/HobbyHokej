package cz.phsoft.hokej.data.entities;

import jakarta.persistence.Embeddable;

@Embeddable
public class NotificationSettings {

    private boolean emailEnabled;
    private boolean smsEnabled;

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }
}
