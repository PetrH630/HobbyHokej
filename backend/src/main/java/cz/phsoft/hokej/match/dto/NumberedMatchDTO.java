package cz.phsoft.hokej.match.dto;

/**
 * Společné rozhraní pro DTO objekt reprezentující zápas
 * s přiřazeným pořadovým číslem v rámci sezóny.
 *
 * Rozhraní se používá u více typů DTO (například MatchDTO,
 * MatchDetailDTO, MatchOverviewDTO), aby bylo možné jednotně
 * nastavovat a číst pořadové číslo zápasu vypočítané na serveru.
 */
public interface NumberedMatchDTO {

    /**
     * Nastavuje pořadové číslo zápasu v sezóně.
     *
     * Metoda se typicky volá v servisní nebo mapovací vrstvě.
     *
     * @param matchNumber pořadové číslo v rozsahu 1..N
     */
    void setMatchNumber(Integer matchNumber);

    /**
     * Vrací pořadové číslo zápasu v sezóně.
     *
     * @return pořadové číslo nebo null, pokud není nastaveno
     */
    Integer getMatchNumber();
}
