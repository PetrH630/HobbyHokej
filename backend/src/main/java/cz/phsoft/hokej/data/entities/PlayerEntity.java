package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující hráče v systému.
 *
 * Hráč představuje sportovní identitu v aplikaci a je používán
 * při registracích na zápasy, notifikacích a statistikách.
 * Hráč může, ale nemusí mít přiřazeného aplikačního uživatele.
 */
@Entity
@Table(name = "player_entity")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    /**
     * Volitelná přezdívka hráče.
     */
    private String nickname;

    /**
     * Typ hráče, například BASIC, STANDARD nebo VIP.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType type;

    /**
     * Celé jméno hráče odvozené z křestního jména a příjmení.
     */
    private String fullName;

    /**
     * Telefonní číslo hráče pro SMS notifikace.
     */
    private String phoneNumber;

    /**
     * Tým, ke kterému je hráč přiřazen.
     */
    @Enumerated(EnumType.STRING)
    private Team team;

    /**
     * Aktuální stav hráče v systému.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus playerStatus = PlayerStatus.PENDING;

    /**
     * Uživatelský účet, ke kterému hráč patří.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    /**
     * Nastavení hráče, zejména kontaktní údaje a notifikační preference.
     * Jeden hráč má právě jedno PlayerSettingsEntity.
     */
    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlayerSettingsEntity settings;

    /**
     * Časové razítko hráče.
     * Používá se pro určení data a času u vytvoření, a změn uživatele.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public PlayerEntity() {
        this.type = PlayerType.BASIC;
    }

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

    public void setSettings(PlayerSettingsEntity settings) {
        this.settings = settings;
        if (settings != null) {
            settings.setPlayer(this);
        }
    }

    /**
     * Aktualizuje celé jméno hráče podle jména a příjmení.
     */
    private void updateFullName() {
        this.fullName = name + " " + surname;
    }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
