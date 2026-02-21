package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.NotificationCategory;
import cz.phsoft.hokej.data.enums.NotificationType;

import java.time.Instant;

/**
 * DTO, které reprezentuje aplikační notifikaci pro uživatele.
 *
 * Slouží k přenosu notifikací z backendu do klientské aplikace.
 * Používá se pro zobrazení badge, přehledu posledních událostí
 * a seznamu notifikací od posledního přihlášení.
 *
 * DTO obsahuje zjednodušený text notifikace, základní metadata
 * (typ, kategorie, důležitost) a časová razítka vytvoření
 * a případného přečtení.
 *
 * Informace o hráči jsou předávány ve formě PlayerDTO,
 * pokud se daná notifikace vztahuje ke konkrétnímu hráči.
 */
public class NotificationDTO {

    /**
     * Identifikátor notifikace.
     */
    private Long id;

    /**
     * Typ notifikace.
     *
     * Odpovídá hodnotě v NotificationType a určuje
     * konkrétní událost v systému.
     */
    private NotificationType type;

    /**
     * Kategorie notifikace.
     *
     * Hodnota se obvykle odvozuje z NotificationType.
     * Používá se pro filtrování a nastavení preferencí.
     */
    private NotificationCategory category;

    /**
     * Příznak důležitosti notifikace.
     *
     * Hodnota se obvykle odvozuje z NotificationType.
     * Používá se například při filtrování podle
     * GlobalNotificationLevel.
     */
    private boolean important;

    /**
     * Stručný text notifikace.
     *
     * Používá se pro zobrazení v badge, přehledu
     * posledních událostí a v základním seznamu
     * notifikací.
     */
    private String messageShort;

    /**
     * Detailnější text notifikace.
     *
     * Je používán pro případné rozšířené zobrazení.
     * U jednodušších notifikací může být null.
     */
    private String messageFull;

    /**
     * Čas vytvoření notifikace.
     *
     * Hodnota je ukládána jako Instant v UTC a
     * používá se pro seřazení a filtrování
     * (například od posledního přihlášení).
     */
    private Instant createdAt;

    /**
     * Čas přečtení notifikace.
     *
     * Pokud je hodnota null, notifikace je považována
     * za nepřečtenou.
     */
    private Instant readAt;

    /**
     * Příznak, zda byla notifikace přečtena.
     *
     * Hodnota se obvykle odvozuje podle toho,
     * zda je readAt null. Pole je určeno pro
     * pohodlnější použití na klientské straně.
     */
    private boolean read;

    /**
     * Hráč, kterého se notifikace týká.
     *
     * U notifikací vázaných na konkrétního hráče
     * se používá PlayerDTO. U systémových
     * notifikací může být null.
     */
    private PlayerDTO player;

    // ==========================================
    // GETTERY A SETTERY
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public PlayerDTO getPlayer() {
        return player;
    }

    public void setPlayer(PlayerDTO player) {
        this.player = player;
    }
}