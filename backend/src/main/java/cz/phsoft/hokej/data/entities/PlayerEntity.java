package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.JerseyColor;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType type; // VIP, STANDARD, BASIC

    private String fullName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private JerseyColor jerseyColor;

    // Many-to-One: každý hráč patří jednomu uživateli
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    // ----------------- Konstruktor -----------------
    public PlayerEntity() {
        this.type = PlayerType.BASIC;
    }

    public PlayerEntity(String name, String surname, PlayerType type, String phoneNumber, JerseyColor jerseyColor) {
        this.name = name;
        this.surname = surname;
        this.type = type;
        this.fullName = name + " " + surname;
        this.phoneNumber = phoneNumber;
        this.jerseyColor = jerseyColor;
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

    public JerseyColor getJerseyColor() {
        return jerseyColor;
    }

    public void setJerseyColor(JerseyColor jerseyColor) {
        this.jerseyColor = jerseyColor;
    }

    public AppUserEntity getUser() {
        return user;
    }

    public void setUser(AppUserEntity user) {
        this.user = user;
    }
}
