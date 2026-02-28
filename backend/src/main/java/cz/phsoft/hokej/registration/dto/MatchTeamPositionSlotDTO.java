package cz.phsoft.hokej.registration.dto;

import cz.phsoft.hokej.player.enums.PlayerPosition;

/**
 * DTO pro přehled kapacity konkrétní pozice v rámci zápasu
 * pro jeden tým.
 *
 * Obsahuje celkový počet slotů pro danou pozici na tým,
 * počet obsazených míst a počet volných slotů.
 */
public class MatchTeamPositionSlotDTO {

    private PlayerPosition position;
    private int capacity;
    private int occupied;
    private int free;

    public PlayerPosition getPosition() {
        return position;
    }

    public void setPosition(PlayerPosition position) {
        this.position = position;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupied() {
        return occupied;
    }

    public void setOccupied(int occupied) {
        this.occupied = occupied;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }
}