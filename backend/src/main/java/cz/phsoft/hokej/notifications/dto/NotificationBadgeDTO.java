package cz.phsoft.hokej.notifications.dto;

import java.time.Instant;

/**
 * DTO, které reprezentuje badge s počtem nepřečtených notifikací.
 *
 * Používá se pro zobrazení indikátoru nových notifikací v UI.
 * Hodnota unreadCountSinceLastLogin udává počet nepřečtených
 * notifikací vytvořených po posledním přihlášení uživatele.
 */
public class NotificationBadgeDTO {

    /**
     * Počet nepřečtených notifikací vytvořených po posledním přihlášení.
     */
    private long unreadCountSinceLastLogin;

    /**
     * Čas předposledního přihlášení uživatele.
     */
    private Instant lastLoginAt;

    /**
     * Čas posledního (aktuálního) přihlášení uživatele.
     */
    private Instant currentLoginAt;

    public long getUnreadCountSinceLastLogin() {
        return unreadCountSinceLastLogin;
    }

    public void setUnreadCountSinceLastLogin(long unreadCountSinceLastLogin) {
        this.unreadCountSinceLastLogin = unreadCountSinceLastLogin;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getCurrentLoginAt() {
        return currentLoginAt;
    }

    public void setCurrentLoginAt(Instant currentLoginAt) {
        this.currentLoginAt = currentLoginAt;
    }
}