package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.NotificationCategory;
import cz.phsoft.hokej.data.enums.NotificationType;

import java.time.Instant;

public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private NotificationCategory category;
    private boolean important;
    private String messageShort;
    private String messageFull;
    private Instant createdAt;
    private Instant readAt;
    private boolean read;
    private PlayerDTO player;

    /**
     * ID zápasu, ke kterému se notifikace vztahuje.
     *
     * Pokud notifikace nesouvisí s konkrétním zápasem,
     * je hodnota null.
     */
    private Long matchId;

    /**
     * Emailová adresa (nebo více adres oddělených čárkou),
     * na kterou byl e-mail skutečně odesílán.
     *
     * Pokud e-mail neodcházel, je null.
     */
    private String emailTo;

    /**
     * Telefonní číslo, na které byla odeslána SMS.
     * Pokud SMS neodcházela, je null.
     */
    private String smsTo;

    // GETTERY / SETTERY

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationCategory getCategory() { return category; }
    public void setCategory(NotificationCategory category) { this.category = category; }

    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }

    public String getMessageShort() { return messageShort; }
    public void setMessageShort(String messageShort) { this.messageShort = messageShort; }

    public String getMessageFull() { return messageFull; }
    public void setMessageFull(String messageFull) { this.messageFull = messageFull; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public PlayerDTO getPlayer() { return player; }
    public void setPlayer(PlayerDTO player) { this.player = player; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getEmailTo() { return emailTo; }
    public void setEmailTo(String emailTo) { this.emailTo = emailTo; }

    public String getSmsTo() { return smsTo; }
    public void setSmsTo(String smsTo) { this.smsTo = smsTo; }
}