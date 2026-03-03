package cz.phsoft.hokej.notifications.entities;

import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entita reprezentující jednu aplikační notifikaci.
 *
 * Notifikace je vždy vázána na konkrétního uživatele.
 * Může být navázána také na hráče a konkrétní zápas.
 *
 * Entita uchovává:
 * - typ notifikace,
 * - krátký a plný text zprávy,
 * - informace o doručení e-mailem nebo SMS,
 * - čas vytvoření a přečtení.
 *
 * Kategorie a důležitost notifikace se neukládají samostatně,
 * ale odvozují se z NotificationType.
 */
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

    /**
     * Uživatel, kterému notifikace náleží.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    /**
     * Hráč, kterého se notifikace týká.
     *
     * Pro obecné systémové notifikace může být null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    /**
     * Zápas, ke kterému se notifikace vztahuje.
     *
     * Pro systémové notifikace může být hodnota null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private MatchEntity match;

    /**
     * Typ notifikace.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private NotificationType type;

    /**
     * Krátká verze textu notifikace.
     */
    @Column(name = "message_short", nullable = false, length = 255)
    private String messageShort;

    /**
     * Plná verze textu notifikace.
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
     *
     * Pokud je hodnota null, notifikace je nepřečtená.
     */
    @Column(name = "read_at")
    private Instant readAt;

    /**
     * Uživatel, který notifikaci vytvořil.
     *
     * Může se jednat například o administrátora.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUserEntity createdBy;

    /**
     * E-mailová adresa, na kterou byla notifikace skutečně odeslána.
     *
     * Pokud se e-mail neposílal, je hodnota null.
     */
    @Column(name = "email_to", length = 255)
    private String emailTo;

    /**
     * Telefonní číslo, na které byla notifikace skutečně odeslána formou SMS.
     *
     * Pokud se SMS neposílala, je hodnota null.
     */
    @Column(name = "sms_to", length = 50)
    private String smsTo;

    /**
     * Nastaví čas vytvoření při prvním uložení entity.
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    /**
     * Vrací informaci, zda je notifikace přečtená.
     *
     * @return true pokud je readAt nastaveno
     */
    @Transient
    public boolean isRead() {
        return readAt != null;
    }

    // Gettery a settery

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