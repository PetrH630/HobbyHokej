package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

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

    private String phoneNumber;

    /**
     * Typ hráče, například BASIC, STANDARD nebo VIP.
     *
     * Pokud není explicitně nastaven, používá se výchozí hodnota BASIC.
     */
    private PlayerType type;

    private Team team;

    /**
     * Stav hráče v systému, například PENDING nebo APPROVED.
     *
     * Pokud není explicitně nastaven, používá se výchozí stav PENDING.
     */
    private PlayerStatus playerStatus;

    public PlayerDTO() {
        this.type = PlayerType.BASIC;
    }

    public PlayerDTO(Long id,
                     String name,
                     String surname,
                     String nickname,
                     PlayerType type,
                     String phoneNumber,
                     Team team,
                     PlayerStatus playerStatus
    ) {

        this.id = id;
        this.name = name;
        this.surname = surname;
        this.nickname = nickname;
        this.type = type != null ? type : PlayerType.BASIC;
        this.phoneNumber = phoneNumber;
        this.team = team;
        this.playerStatus = playerStatus != null ? playerStatus : PlayerStatus.PENDING;

        updateFullName();
    }

    // gettery / settery

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

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public PlayerStatus getPlayerStatus() { return playerStatus; }
    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus != null ? playerStatus : PlayerStatus.PENDING;
    }

    // interní logika

    /**
     * Aktualizuje odvozené pole fullName při změně jména nebo příjmení.
     */
    private void updateFullName() {
        this.fullName = name + " " + surname;
    }
}
