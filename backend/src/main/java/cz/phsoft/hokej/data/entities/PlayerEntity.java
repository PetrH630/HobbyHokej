package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerType;
import jakarta.persistence.*;

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

    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType type; // VIP, STANDARD, BASIC

    private String fullName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus playerStatus = PlayerStatus.PENDING;

    // Many-to-One: každý hráč patří jednomu uživateli
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    @Embedded
    private NotificationSettings notificationSettings = new NotificationSettings();

    // ----------------- Konstruktor -----------------
    public PlayerEntity() {
        this.type = PlayerType.BASIC;
    }

    public PlayerEntity(String name, String surname, String nickName, PlayerType type, String phoneNumber, Team team, PlayerStatus playerStatus) {
        this.name = name;
        this.surname = surname;
        this.nickName = nickName;
        this.type = type;
        this.fullName = name + " " + surname;
        this.phoneNumber = phoneNumber;
        this.team = team;
        this.playerStatus = playerStatus;

    }

    // ----------------- Gettery a Settery -----------------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateFullName();
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
        updateFullName();
    }

    public String getNickname() { return nickName;}
    public void setNickname(String nickName) { this.nickName = nickName; }

    public String getFullName() {
        return fullName;
    }

    public PlayerType getType() {
        return type;
    }
    public void setType(PlayerType type) {
        this.type = type;
    }

    private void updateFullName() {
        this.fullName = name + " " + surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Team getTeam() {
        return team;
    }
    public void setTeam(Team team) {
        this.team = team;
    }

    public AppUserEntity getUser() {
        return user;
    }
    public void setUser(AppUserEntity user) {
        this.user = user;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }
    public boolean isNotifyByEmail() {
        return notificationSettings != null && notificationSettings.isEmailEnabled();
    }

    public void setNotifyByEmail(boolean notifyByEmail) {
        if (notificationSettings == null) {
            notificationSettings = new NotificationSettings();
        }
        notificationSettings.setEmailEnabled(notifyByEmail);
    }

    public boolean isNotifyBySms() {
        return notificationSettings != null && notificationSettings.isSmsEnabled();
    }

    public void setNotifyBySms(boolean notifyBySms) {
        if (notificationSettings == null) {
            notificationSettings = new NotificationSettings();
        }
        notificationSettings.setSmsEnabled(notifyBySms);
    }

}
