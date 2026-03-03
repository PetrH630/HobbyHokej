package cz.phsoft.hokej.registration.dto;

import java.time.LocalDateTime;

/**
 * DTO pro náhled připomínek hráčům ve stavu bez reakce.
 *
 * Slouží pro administrativní nebo testovací endpoint,
 * který umožňuje zobrazit, kterým hráčům by byla odeslána
 * připomínka k registraci, aniž by došlo k reálnému odeslání
 * notifikací.
 *
 * Objekt obsahuje základní identifikační údaje o zápase
 * a hráči potřebné pro zobrazení přehledu plánovaných
 * připomínek.
 */
public class NoResponseReminderPreviewDTO {

    /**
     * Identifikátor zápasu, ke kterému se připomínka vztahuje.
     */
    private Long matchId;

    /**
     * Datum a čas konání zápasu.
     */
    private LocalDateTime matchDateTime;

    /**
     * Identifikátor hráče, kterému by byla připomínka odeslána.
     */
    private Long playerId;

    /**
     * Celé jméno hráče pro prezentační účely.
     */
    private String playerFullName;

    /**
     * Telefonní číslo hráče, na které by byla připomínka odeslána.
     */
    private String playerPhoneNumber;

    /**
     * Vytváří prázdnou instanci DTO.
     */
    public NoResponseReminderPreviewDTO() {
    }

    /**
     * Vytváří instanci DTO s kompletními údaji pro náhled připomínky.
     *
     * @param matchId           identifikátor zápasu
     * @param matchDateTime     datum a čas zápasu
     * @param playerId          identifikátor hráče
     * @param playerFullName    celé jméno hráče
     * @param playerPhoneNumber telefonní číslo hráče
     */
    public NoResponseReminderPreviewDTO(Long matchId,
                                        LocalDateTime matchDateTime,
                                        Long playerId,
                                        String playerFullName,
                                        String playerPhoneNumber) {
        this.matchId = matchId;
        this.matchDateTime = matchDateTime;
        this.playerId = playerId;
        this.playerFullName = playerFullName;
        this.playerPhoneNumber = playerPhoneNumber;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public void setMatchDateTime(LocalDateTime matchDateTime) {
        this.matchDateTime = matchDateTime;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerFullName() {
        return playerFullName;
    }

    public void setPlayerFullName(String playerFullName) {
        this.playerFullName = playerFullName;
    }

    public String getPlayerPhoneNumber() {
        return playerPhoneNumber;
    }

    public void setPlayerPhoneNumber(String playerPhoneNumber) {
        this.playerPhoneNumber = playerPhoneNumber;
    }
}