package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerPosition;

import java.util.Map;

/**
 * Datový přenosový objekt reprezentující statistiku hráče
 * za aktuální sezónu.
 *
 * Objekt slouží pro přenos agregovaných dat z service vrstvy
 * do controlleru a následně do klientské aplikace.
 */
public class PlayerStatsDTO {

    /**
     * Identifikátor hráče, pro kterého jsou statistiky počítány.
     */
    private Long playerId;

    /**
     * Celkový počet odehraných zápasů v aktuální sezóně.
     */
    private int allMatchesInSeason;

    /**
     * Celkový počet odehraných zápasů dostupných pro hráče v aktuální sezóně.
     */
    private int allMatchesInSeasonForPlayer;

    /**
     * Počet zápasů, na které byl hráč registrován.
     */
    private int registered;

    /**
     * Počet zápasů, na které se hráč odhlásil.
     */
    private int unregistered;

    /**
     * Počet zápasů, ze kterých byl hráč omluven.
     */
    private int excused;

    /**
     * Počet zápasů, ve kterých byl hráč jako možná budu.
     */
    private int substituted;

    /**
     * Počet zápasů, ve kterých byl hráč veden jako rezervní hráč.
     */
    private int reserved;

    /**
     * Počet zápasů, na které hráč nereagoval.
     */
    private int noResponse;

    /**
     * Počet zápasů, ve kterých nebyl hráč omluven a zároveň nenastoupil.
     */
    private int noExcused;

    /**
     * Domovský tým hráče uložený v databázi.
     */
    private Team homeTeam;

    /**
     * Primární pozice hráče.
     */
    private PlayerPosition primaryPosition;

    /**
     * Sekundární pozice hráče.
     */
    private PlayerPosition secondaryPosition;

    /**
     * Počty registrací hráče podle týmu pro status REGISTERED.
     */
    private Map<Team, Integer> registeredByTeam;

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public int getAllMatchesInSeason() { return allMatchesInSeason; }
    public void setAllMatchesInSeason(int allMatchesInSeason) { this.allMatchesInSeason = allMatchesInSeason; }

    public int getAllMatchesInSeasonForPlayer() { return allMatchesInSeasonForPlayer; }
    public void setAllMatchesInSeasonForPlayer(int allMatchesInSeasonForPlayer) {
        this.allMatchesInSeasonForPlayer = allMatchesInSeasonForPlayer;
    }

    public int getRegistered() { return registered; }
    public void setRegistered(int registered) { this.registered = registered; }

    public int getUnregistered() { return unregistered; }
    public void setUnregistered(int unregistered) { this.unregistered = unregistered; }

    public int getExcused() { return excused; }
    public void setExcused(int excused) { this.excused = excused; }

    public int getSubstituted() { return substituted; }
    public void setSubstituted(int substituted) { this.substituted = substituted; }

    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }

    public int getNoResponse() { return noResponse; }
    public void setNoResponse(int noResponse) { this.noResponse = noResponse; }

    public int getNoExcused() { return noExcused; }
    public void setNoExcused(int noExcused) { this.noExcused = noExcused; }

    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public PlayerPosition getPrimaryPosition() { return primaryPosition; }
    public void setPrimaryPosition(PlayerPosition primaryPosition) { this.primaryPosition = primaryPosition; }

    public PlayerPosition getSecondaryPosition() { return secondaryPosition; }
    public void setSecondaryPosition(PlayerPosition secondaryPosition) { this.secondaryPosition = secondaryPosition; }

    public Map<Team, Integer> getRegisteredByTeam() { return registeredByTeam; }
    public void setRegisteredByTeam(Map<Team, Integer> registeredByTeam) { this.registeredByTeam = registeredByTeam; }
}