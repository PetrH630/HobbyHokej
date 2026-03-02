package cz.phsoft.hokej.match.dto;

import cz.phsoft.hokej.player.enums.PlayerPosition;

import java.util.List;

/**
 * DTO pro přehled kapacity konkrétní pozice v rámci zápasu
 * pro jeden tým.
 *
 * Obsahuje celkový počet slotů pro danou pozici na tým,
 * počet obsazených míst a počet volných slotů.
 */
public class MatchTeamPositionSlotDTO {

    private PlayerPosition position;
    private Integer capacity;
    private Integer occupied;
    private Integer free;

    // Hráči aktuálně na ledě (REGISTERED)
    private List<MatchTeamPositionPlayerDTO> registeredPlayers;

    // Náhradníci pro danou pozici (RESERVED)
    private List<MatchTeamPositionPlayerDTO> reservedPlayers;

    public PlayerPosition getPosition() {
        return position;
    }

    public void setPosition(PlayerPosition position) {
        this.position = position;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getOccupied() {
        return occupied;
    }

    public void setOccupied(Integer occupied) {
        this.occupied = occupied;
    }

    public Integer getFree() {
        return free;
    }

    public void setFree(Integer free) {
        this.free = free;
    }

    public List<MatchTeamPositionPlayerDTO> getRegisteredPlayers() {
        return registeredPlayers;
    }

    public void setRegisteredPlayers(List<MatchTeamPositionPlayerDTO> registeredPlayers) {
        this.registeredPlayers = registeredPlayers;
    }

    public List<MatchTeamPositionPlayerDTO> getReservedPlayers() {
        return reservedPlayers;
    }

    public void setReservedPlayers(List<MatchTeamPositionPlayerDTO> reservedPlayers) {
        this.reservedPlayers = reservedPlayers;
    }
}