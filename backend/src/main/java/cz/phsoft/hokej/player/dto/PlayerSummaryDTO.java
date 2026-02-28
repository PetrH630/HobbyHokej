package cz.phsoft.hokej.player.dto;

import cz.phsoft.hokej.player.enums.PlayerType;
import cz.phsoft.hokej.player.enums.Team;

/**
 * Zjednodušené DTO reprezentující hráče.
 *
 * Slouží pro přehledové a seznamové pohledy, kde není potřeba kompletní
 * detail hráče ani jeho stavové a notifikační informace. Typicky se používá
 * v seznamech hráčů nebo pro rychlé výběry.
 */
public class PlayerSummaryDTO {

    private Long id;
    private String name;
    private String surname;
    private String fullName;
    private String nickname;
    private PlayerType type;
    private Team team;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) { this.type = type; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
