package cz.phsoft.hokej.match.dto;

import cz.phsoft.hokej.player.enums.PlayerPosition;

/**
 * DTO reprezentující kapacitní stav jedné herní pozice v zápase.
 *
 * Obsahuje maximální počet míst pro danou pozici na tým,
 * počet obsazených míst v týmu DARK a LIGHT
 * a z toho odvozený počet volných slotů.
 *
 * Objekt se používá jako součást MatchPositionOverviewDTO.
 */
public class MatchPositionSlotDTO {

    private PlayerPosition position;
    private int capacityPerTeam;
    private int occupiedDark;
    private int occupiedLight;
    private int freeDark;
    private int freeLight;

    public PlayerPosition getPosition() {
        return position;
    }

    public void setPosition(PlayerPosition position) {
        this.position = position;
    }

    public int getCapacityPerTeam() {
        return capacityPerTeam;
    }

    public void setCapacityPerTeam(int capacityPerTeam) {
        this.capacityPerTeam = capacityPerTeam;
    }

    public int getOccupiedDark() {
        return occupiedDark;
    }

    public void setOccupiedDark(int occupiedDark) {
        this.occupiedDark = occupiedDark;
    }

    public int getOccupiedLight() {
        return occupiedLight;
    }

    public void setOccupiedLight(int occupiedLight) {
        this.occupiedLight = occupiedLight;
    }

    public int getFreeDark() {
        return freeDark;
    }

    public void setFreeDark(int freeDark) {
        this.freeDark = freeDark;
    }

    public int getFreeLight() {
        return freeLight;
    }

    public void setFreeLight(int freeLight) {
        this.freeLight = freeLight;
    }
}