package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO reprezentující hráče v systému.
 *
 * Slouží k přenosu dat o hráči mezi backendem a klientem
 * (registrace hráče, správa profilu, přehledy a registrace na zápasy).
 *
 * DTO neobsahuje žádnou perzistentní logiku
 * a je nezávislé na databázové vrstvě.
 */
public class PlayerDTO {

    /**
     * ID hráče.
     *
     * Při vytváření nového hráče může být {@code null},
     * hodnota je generována serverem.
     */
    private Long id;

    @NotBlank(message = "Křestní jméno je povinné.")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "Příjmení je povinné.")
    @Size(min = 2, max = 50)
    private String surname;

    private String nickName;

    /**
     * Celé jméno hráče.
     *
     * Odvozená hodnota složená ze jména a příjmení.
     */
    private String fullName;

    private String phoneNumber;

    /**
     * Typ hráče (např. BASIC, STANDARD, VIP).
     *
     * Pokud není explicitně nastaven,
     * použije se výchozí hodnota {@link PlayerType#BASIC}.
     */
    private PlayerType type;

    private Team team;

    /**
     * Stav hráče v systému (např. PENDING, APPROVED).
     *
     * Pokud není nastaven, použije se výchozí stav {@link PlayerStatus#PENDING}.
     */
    private PlayerStatus playerStatus;

    /**
     * Nastavení notifikací hráče.
     */
    private boolean notifyByEmail;
    private boolean notifyBySms;

    public PlayerDTO() {
        this.type = PlayerType.BASIC;
    }

    public PlayerDTO(Long id,
                     String name,
                     String surname,
                     String nickName,
                     PlayerType type,
                     Team team,
                     PlayerStatus playerStatus,
                     boolean notifyByEmail,
                     boolean notifyBySms) {

        this.id = id;
        this.name = name;
        this.surname = surname;
        this.nickName = nickName;
        this.type = type != null ? type : PlayerType.BASIC;
        this.team = team;
        this.playerStatus = playerStatus != null ? playerStatus : PlayerStatus.PENDING;
        this.notifyByEmail = notifyByEmail;
        this.notifyBySms = notifyBySms;
        updateFullName();
    }

    // gettery / settery

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; updateFullName(); }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; updateFullName(); }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getFullName() { return fullName; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) {
        this.type = type != null ? type : PlayerType.BASIC;
    }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public PlayerStatus getPlayerStatus() { return playerStatus; }
    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus != null ? playerStatus : PlayerStatus.PENDING;
    }

    public boolean isNotifyByEmail() { return notifyByEmail; }
    public void setNotifyByEmail(boolean notifyByEmail) { this.notifyByEmail = notifyByEmail; }

    public boolean isNotifyBySms() { return notifyBySms; }
    public void setNotifyBySms(boolean notifyBySms) { this.notifyBySms = notifyBySms; }

    // ==================================================
    // INTERNÍ LOGIKA
    // ==================================================

    /**
     * Aktualizuje odvozené pole {@link #fullName}
     * při změně jména nebo příjmení.
     */
    private void updateFullName() {
        this.fullName = name + " " + surname;
    }
}
