package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující hráče v systému.
 *
 * Hráč představuje sportovní identitu používanou při registracích
 * na zápasy, notifikacích a vyhodnocování účasti. Hráč může,
 * ale nemusí mít přiřazen aplikační uživatelský účet.
 *
 * Entita obsahuje základní identifikační údaje, stav hráče
 * v systému a vazbu na uživatele a nastavení hráče.
 */
@Entity
@Table(name = "player_entity")
public class PlayerEntity {

    /**
     * Primární klíč hráče.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Křestní jméno hráče.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Příjmení hráče.
     * Při nastavení je převáděno na velká písmena.
     */
    @Column(nullable = false)
    private String surname;

    /**
     * Volitelná přezdívka hráče.
     */
    private String nickname;

    /**
     * Typ hráče v systému.
     *
     * Určuje například cenový nebo organizační režim hráče.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType type;

    /**
     * Celé jméno hráče odvozené z křestního jména a příjmení.
     *
     * Hodnota se aktualizuje automaticky při změně jména nebo příjmení.
     */
    private String fullName;

    /**
     * Telefonní číslo hráče.
     *
     * Používá se zejména pro zasílání SMS notifikací.
     */
    private String phoneNumber;

    /**
     * Tým, ke kterému je hráč přiřazen.
     */
    @Enumerated(EnumType.STRING)
    private Team team;

    /**
     * Aktuální stav hráče v systému.
     *
     * Stav určuje například, zda je hráč čekající na schválení,
     * schválený nebo zamítnutý.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus playerStatus = PlayerStatus.PENDING;

    /**
     * Uživatelský účet, ke kterému hráč patří.
     *
     * Vazba je volitelná, protože hráč může existovat
     * i bez přímé vazby na uživatelský účet.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    /**
     * Nastavení hráče.
     *
     * Obsahuje například kontaktní údaje a notifikační preference.
     * Jeden hráč má právě jedno PlayerSettingsEntity.
     */
    @OneToOne(mappedBy = "player",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private PlayerSettingsEntity settings;

    /**
     * Časové razítko hráče.
     *
     * Uchovává datum a čas vytvoření hráče.
     * Hodnota se nastavuje při prvním uložení a dále se nemění.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Metoda volaná před prvním uložením entity.
     *
     * Zajišťuje inicializaci časového razítka v případě,
     * že nebylo nastaveno.
     */
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    /**
     * Bezparametrický konstruktor požadovaný JPA.
     *
     * Výchozí typ hráče je nastaven na BASIC.
     */
    public PlayerEntity() {
        this.type = PlayerType.BASIC;
    }

    /**
     * Konstruktor pro vytvoření hráče s inicializačními hodnotami.
     *
     * @param name         křestní jméno hráče
     * @param surname      příjmení hráče
     * @param nickname     přezdívka hráče
     * @param type         typ hráče
     * @param phoneNumber  telefonní číslo hráče
     * @param team         tým hráče
     * @param playerStatus aktuální stav hráče
     */
    public PlayerEntity(String name,
                        String surname,
                        String nickname,
                        PlayerType type,
                        String phoneNumber,
                        Team team,
                        PlayerStatus playerStatus) {

        this.name = name;
        this.surname = surname;
        this.nickname = nickname;
        this.type = type;
        this.phoneNumber = phoneNumber;
        this.team = team;
        this.playerStatus = playerStatus;
        updateFullName();
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
        updateFullName();
    }

    public String getSurname() { return surname; }

    public void setSurname(String surname) {
        this.surname = surname.toUpperCase();
        updateFullName();
    }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getFullName() { return fullName; }

    public PlayerType getType() { return type; }

    public void setType(PlayerType type) { this.type = type; }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Team getTeam() { return team; }

    public void setTeam(Team team) { this.team = team; }

    public PlayerStatus getPlayerStatus() { return playerStatus; }

    public void setPlayerStatus(PlayerStatus playerStatus) { this.playerStatus = playerStatus; }

    public AppUserEntity getUser() { return user; }

    public void setUser(AppUserEntity user) { this.user = user; }

    public PlayerSettingsEntity getSettings() { return settings; }

    /**
     * Nastavuje nastavení hráče a zároveň zajišťuje obousměrnou vazbu.
     *
     * @param settings instance nastavení hráče
     */
    public void setSettings(PlayerSettingsEntity settings) {
        this.settings = settings;
        if (settings != null) {
            settings.setPlayer(this);
        }
    }

    /**
     * Aktualizuje celé jméno hráče podle aktuálního jména a příjmení.
     *
     * Metoda je volána při změně jména nebo příjmení.
     */
    private void updateFullName() {
        this.fullName = name + " " + surname;
    }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
