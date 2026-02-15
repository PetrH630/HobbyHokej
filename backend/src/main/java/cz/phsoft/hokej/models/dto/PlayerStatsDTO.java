package cz.phsoft.hokej.models.dto;

/**
 * Datový přenosový objekt reprezentující statistiku hráče
 * za aktuální sezónu.
 *
 * Objekt slouží pro přenos agregovaných dat z service vrstvy
 * do controlleru a následně do klientské aplikace.
 * Obsahuje celkový počet zápasů v sezóně a počty zápasů
 * rozdělené podle jednotlivých registračních statusů.
 *
 * Instance tohoto objektu je vytvářena a naplňována
 * ve service vrstvě na základě vyhodnocení zápasů
 * a registrací hráče.
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

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getAllMatchesInSeason() {
        return allMatchesInSeason;
    }

    public void setAllMatchesInSeason(int allMatchesInSeason) {
        this.allMatchesInSeason = allMatchesInSeason;
    }

    public int getRegistered() {
        return registered;
    }

    public void setRegistered(int registered) {
        this.registered = registered;
    }

    public int getUnregistered() {
        return unregistered;
    }

    public void setUnregistered(int unregistered) {
        this.unregistered = unregistered;
    }

    public int getExcused() {
        return excused;
    }

    public void setExcused(int excused) {
        this.excused = excused;
    }

    public int getSubstituted() {
        return substituted;
    }

    public void setSubstituted(int substituted) {
        this.substituted = substituted;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public int getNoResponse() {
        return noResponse;
    }

    public void setNoResponse(int noResponse) {
        this.noResponse = noResponse;
    }

    public int getNoExcused() {
        return noExcused;
    }

    public void setNoExcused(int noExcused) {
        this.noExcused = noExcused;
    }
}
