package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.NotificationType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "user_id"),
                @Index(name = "idx_notification_created_at", columnList = "created_at"),
                @Index(name = "idx_notification_read_at", columnList = "read_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_user_match_type",
                        columnNames = {"user_id", "match_id", "type"}
                )
        }
)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    /**
     * Zápas, ke kterému se notifikace vztahuje.
     *
     * Pro obecné systémové notifikace může být hodnota null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private MatchEntity match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private NotificationType type;

    @Column(name = "message_short", nullable = false, length = 255)
    private String messageShort;

    @Column(name = "message_full", length = 2000)
    private String messageFull;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUserEntity createdBy;

    /**
     * Emailová adresa (nebo více adres oddělených čárkou),
     * na kterou byla notifikace skutečně odesílána
     * e-mailem (podle nastavení a rozhodnutí NotificationDecision).
     *
     * Pokud se e-mail neposílal, je hodnota null.
     */
    @Column(name = "email_to", length = 255)
    private String emailTo;

    /**
     * Telefonní číslo, na které byla notifikace
     * skutečně odesílána formou SMS.
     *
     * Pokud se SMS neposílala, je hodnota null.
     */
    @Column(name = "sms_to", length = 50)
    private String smsTo;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    @Transient
    public boolean isRead() {
        return readAt != null;
    }

    // Gettery / settery

    public Long getId() {
        return id;
    }

    public AppUserEntity getUser() {
        return user;
    }

    public void setUser(AppUserEntity user) {
        this.user = user;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public void setMatch(MatchEntity match) {
        this.match = match;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessageShort() {
        return messageShort;
    }

    public void setMessageShort(String messageShort) {
        this.messageShort = messageShort;
    }

    public String getMessageFull() {
        return messageFull;
    }

    public void setMessageFull(String messageFull) {
        this.messageFull = messageFull;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public AppUserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getSmsTo() {
        return smsTo;
    }

    public void setSmsTo(String smsTo) {
        this.smsTo = smsTo;
    }
}