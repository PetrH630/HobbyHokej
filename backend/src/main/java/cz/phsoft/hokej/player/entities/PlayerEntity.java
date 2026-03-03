package cz.phsoft.hokej.player.entities;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.player.enums.PlayerStatus;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.enums.PlayerType;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující hráče v systému.
 *
 * Hráč představuje sportovní identitu používanou při registracích
 * na zápasy, notifikacích a vyhodnocování účasti. Entita je mapována
 * na databázovou tabulku player_entity a slouží jako hlavní zdroj
 * dat o hráči v perzistentní vrstvě.
 *
 * Obsahuje vazbu na uživatele aplikace a na nastavení hráče.
 */
@Entity
@Table(name = "player_entity")
public class PlayerEntity {

    /**
     * Jedinečný identifikátor hráče.
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
     */
    private String fullName;

    /**
     * Telefonní číslo hráče.
     */
    private String phoneNumber;

    /**
     * Tým, ke kterému je hráč přiřazen.
     */
    @Enumerated(EnumType.STRING)
    private Team team;

    /**
     * Primární pozice hráče.
     *
     * Výchozí hodnota je ANY.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_position", nullable = false)
    private PlayerPosition primaryPosition;

    /**
     * Sekundární pozice hráče.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_position")
    private PlayerPosition secondaryPosition;

    /**
     * Aktuální stav hráče v systému.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus playerStatus = PlayerStatus.PENDING;

    /**
     * Vazba na uživatele aplikace, ke kterému je hráč přiřazen.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    /**
     * Nastavení hráče.
     *
     * Vazba je obousměrná a životní cyklus nastavení je
     * navázán na životní cyklus hráče.
     */
    @OneToOne(mappedBy = "player",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private PlayerSettingsEntity settings;

    /**
     * Časové razítko vytvoření hráče.
     *
     * Hodnota je nastavena při vytvoření entity a dále se nemění.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Inicializační metoda volaná před persistencí entity.
     *
     * Zajišťuje nastavení výchozích hodnot časového razítka
     * a primární pozice hráče, pokud nebyly explicitně nastaveny.
     */
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        if (this.primaryPosition == null) {
            this.primaryPosition = PlayerPosition.ANY;
        }
    }

    /**
     * Vytváří prázdnou instanci hráče s výchozími hodnotami.
     *
     * Nastavuje výchozí typ hráče a primární pozici.
     */
    public PlayerEntity() {
        this.type = PlayerType.BASIC;
        this.primaryPosition = PlayerPosition.ANY;
    }

    /**
     * Vytváří instanci hráče se zadanými hodnotami.
     *
     * Po nastavení základních údajů se aktualizuje odvozené pole fullName.
     *
     * @param name         křestní jméno hráče
     * @param surname      příjmení hráče
     * @param nickname     přezdívka hráče
     * @param type         typ hráče
     * @param phoneNumber  telefonní číslo hráče
     * @param team         tým hráče
     * @param playerStatus stav hráče v systému
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
        this.primaryPosition = PlayerPosition.ANY;
        updateFullName();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    /**
     * Nastavuje křestní jméno hráče a aktualizuje celé jméno.
     */
    public void setName(String name) {
        this.name = name;
        updateFullName();
    }

    public String getSurname() { return surname; }

    /**
     * Nastavuje příjmení hráče ve formátu velkých písmen
     * a aktualizuje celé jméno.
     */
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

    public PlayerPosition getPrimaryPosition() { return primaryPosition; }

    /**
     * Nastavuje primární pozici hráče.
     *
     * Pokud je hodnota null, nastavuje se výchozí pozice ANY.
     */
    public void setPrimaryPosition(PlayerPosition primaryPosition) {
        this.primaryPosition = primaryPosition != null ? primaryPosition : PlayerPosition.ANY;
    }

    public PlayerPosition getSecondaryPosition() { return secondaryPosition; }

    public void setSecondaryPosition(PlayerPosition secondaryPosition) {
        this.secondaryPosition = secondaryPosition;
    }

    public PlayerStatus getPlayerStatus() { return playerStatus; }
    public void setPlayerStatus(PlayerStatus playerStatus) { this.playerStatus = playerStatus; }

    public AppUserEntity getUser() { return user; }
    public void setUser(AppUserEntity user) { this.user = user; }

    public PlayerSettingsEntity getSettings() { return settings; }

    /**
     * Nastavuje nastavení hráče a synchronizuje obousměrnou vazbu.
     *
     * Pokud je nastavení neprázdné, nastaví se zpětná reference
     * na tuto entitu hráče.
     */
    public void setSettings(PlayerSettingsEntity settings) {
        this.settings = settings;
        if (settings != null) {
            settings.setPlayer(this);
        }
    }

    /**
     * Aktualizuje odvozené pole fullName na základě jména a příjmení.
     */
    private void updateFullName() {
        this.fullName = name + " " + surname;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}