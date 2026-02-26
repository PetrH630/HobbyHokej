package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerPosition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO, které reprezentuje hráče v systému.
 *
 * Slouží k přenosu dat o hráči mezi backendem a klientem při registraci,
 * správě profilu, přehledech a registracích na zápasy. DTO je nezávislé
 * na databázové vrstvě a neobsahuje perzistentní logiku. Obsahuje však
 * jednoduchou odvozenou hodnotu celého jména.
 */
public class PlayerDTO {

    /**
     * ID hráče.
     *
     * Při vytváření nového hráče může být hodnota null.
     * Hodnota se generuje na serveru.
     */
    private Long id;

    @NotBlank(message = "Křestní jméno je povinné.")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "Příjmení je povinné.")
    @Size(min = 2, max = 50)
    private String surname;

    private String nickname;

    /**
     * Celé jméno hráče.
     *
     * Hodnota se odvozuje z křestního jména a příjmení.
     * Aktualizuje se při změně jména nebo příjmení.
     */
    private String fullName;

    @Pattern(
            regexp = "^\\+[1-9]\\d{1,14}$",
            message = "Telefon musí být v mezinárodním formátu, např. +420123456789"
    )
    private String phoneNumber;

    /**
     * Typ hráče, například BASIC, STANDARD nebo VIP.
     *
     * Pokud není explicitně nastaven, používá se výchozí hodnota BASIC.
     */
    private PlayerType type;

    private Team team;

    /**
     * Primární pozice hráče na ledě.
     *
     * Pokud není nastavena, používá se výchozí hodnota ANY.
     */
    private PlayerPosition primaryPosition;

    /**
     * Sekundární (alternativní) pozice hráče.
     *
     * Může být null, pokud hráč žádnou preferovanou sekundární pozici nemá.
     */
    private PlayerPosition secondaryPosition;

    /**
     * Stav hráče v systému, například PENDING nebo APPROVED.
     *
     * Pokud není explicitně nastaven, používá se výchozí stav PENDING.
     */
    private PlayerStatus playerStatus;

    /**
     * Časové razítko hráče.
     * Používá se pro určení data a času u vytvoření a změn uživatele.
     */
    private LocalDateTime timestamp;

    public PlayerDTO() {
        this.type = PlayerType.BASIC;
        this.primaryPosition = PlayerPosition.ANY;
    }

    public PlayerDTO(Long id,
                     String name,
                     String surname,
                     String nickname,
                     PlayerType type,
                     String phoneNumber,
                     Team team,
                     PlayerStatus playerStatus,
                     PlayerPosition primaryPosition,
                     PlayerPosition secondaryPosition
    ) {

        this.id = id;
        this.name = name;
        this.surname = surname;
        this.nickname = nickname;
        this.type = type != null ? type : PlayerType.BASIC;
        this.phoneNumber = phoneNumber;
        this.team = team;
        this.playerStatus = playerStatus != null ? playerStatus : PlayerStatus.PENDING;
        this.primaryPosition = primaryPosition != null ? primaryPosition : PlayerPosition.ANY;
        this.secondaryPosition = secondaryPosition;

        updateFullName();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; updateFullName(); }

    public String getSurname() { return surname.toUpperCase(); }
    public void setSurname(String surname) { this.surname = surname.toUpperCase(); updateFullName(); }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getFullName() { return fullName; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) {
        this.type = type != null ? type : PlayerType.BASIC;
    }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public PlayerPosition getPrimaryPosition() {
        return primaryPosition;
    }

    public void setPrimaryPosition(PlayerPosition primaryPosition) {
        this.primaryPosition = primaryPosition != null ? primaryPosition : PlayerPosition.ANY;
    }

    public PlayerPosition getSecondaryPosition() {
        return secondaryPosition;
    }

    public void setSecondaryPosition(PlayerPosition secondaryPosition) {
        this.secondaryPosition = secondaryPosition;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = (phoneNumber == null || phoneNumber.isBlank())
                ? null
                : phoneNumber;
    }

    public PlayerStatus getPlayerStatus() { return playerStatus; }
    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus != null ? playerStatus : PlayerStatus.PENDING;
    }

    /**
     * Aktualizuje odvozené pole fullName při změně jména nebo příjmení.
     */
    private void updateFullName() {
        this.fullName = name + " " + surname;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}