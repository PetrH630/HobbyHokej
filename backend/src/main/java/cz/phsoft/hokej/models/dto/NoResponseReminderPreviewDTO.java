package cz.phsoft.hokej.models.dto;

import java.time.LocalDateTime;

/**
 * DTO pro náhled připomínek NO_RESPONSE.
 *
 * Slouží pouze pro admin/test endpoint, aby bylo vidět,
 * kterým hráčům by se připomínka poslala, aniž by se reálně
 * odeslaly notifikace.
 */
public class NoResponseReminderPreviewDTO {

    private Long matchId;
    private LocalDateTime matchDateTime;

    private Long playerId;
    private String playerFullName;
    private String playerPhoneNumber;

    public NoResponseReminderPreviewDTO() {
    }

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