package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.NotificationType;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entita reprezentující aplikační notifikaci.
 *
 * Notifikace vzniká při událostech definovaných v NotificationType.
 * Slouží pro:
 * - zobrazení badge (nepřečtené),
 * - přehled událostí,
 * - výpis notifikací od posledního přihlášení.
 *
 * Všechny časy jsou ukládány jako Instant (UTC).
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "user_id"),
                @Index(name = "idx_notification_created_at", columnList = "created_at"),
                @Index(name = "idx_notification_read_at", columnList = "read_at")
        }
)
public class NotificationEntity {

    /**
     * Primární klíč notifikace.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Uživatel, kterému je notifikace určena.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    /**
     * Volitelná vazba na hráče.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    /**
     * Typ notifikace.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private NotificationType type;

    /**
     * Stručný text notifikace pro zobrazení v dropdownu.
     */
    @Column(name = "message_short", nullable = false, length = 255)
    private String messageShort;

    /**
     * Detailní text (volitelné).
     */
    @Column(name = "message_full", length = 2000)
    private String messageFull;

    /**
     * Čas vytvoření notifikace.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Čas přečtení notifikace.
     * Pokud je null → notifikace je nepřečtená.
     */
    @Column(name = "read_at")
    private Instant readAt;

    /**
     * Uživatel, který akci způsobil (např. admin).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUserEntity createdBy;

    /**
     * Automatické nastavení createdAt při persist.
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // ==================================================
    // Business helper metody
    // ==================================================

    @Transient
    public boolean isRead() {
        return readAt != null;
    }

    // ==================================================
    // Gettery / Settery
    // ==================================================

    public Long getId() { return id; }

    public AppUserEntity getUser() { return user; }
    public void setUser(AppUserEntity user) { this.user = user; }

    public PlayerEntity getPlayer() { return player; }
    public void setPlayer(PlayerEntity player) { this.player = player; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getMessageShort() { return messageShort; }
    public void setMessageShort(String messageShort) { this.messageShort = messageShort; }

    public String getMessageFull() { return messageFull; }
    public void setMessageFull(String messageFull) { this.messageFull = messageFull; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public AppUserEntity getCreatedBy() { return createdBy; }
    public void setCreatedBy(AppUserEntity createdBy) { this.createdBy = createdBy; }
}