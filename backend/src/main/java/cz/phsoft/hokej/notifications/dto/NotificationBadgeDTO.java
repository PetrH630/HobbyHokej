package cz.phsoft.hokej.notifications.dto;

import java.time.Instant;

/**
 * DTO reprezentující badge s počtem nepřečtených notifikací.
 *
 * Používá se pro zobrazení indikátoru nových notifikací v uživatelském rozhraní.
 * Obsahuje počet nepřečtených notifikací od posledního přihlášení
 * a časové údaje o přihlášeních uživatele.
 *
 * Třída slouží výhradně jako datový přenosový objekt.
 */
public class NotificationBadgeDTO {

    private long unreadCountSinceLastLogin;
    private Instant lastLoginAt;
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