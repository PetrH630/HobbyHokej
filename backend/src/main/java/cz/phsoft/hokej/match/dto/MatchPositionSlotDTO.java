package cz.phsoft.hokej.match.dto;

import cz.phsoft.hokej.player.enums.PlayerPosition;

/**
 * DTO pro přehled kapacity konkrétní pozice v rámci zápasu.
 *
 * Obsahuje počet míst pro danou pozici na tým, počty obsazených míst
 * v týmu DARK a LIGHT a z toho odvozené volné sloty.
 *
 * Používá se v {@link MatchPositionOverviewDTO} pro zobrazení přehledu
 * pozic na ledě.
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