package cz.phsoft.hokej.match.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pro aktualizaci skóre zápasu.
 *
 * Slouží jako vstupní objekt při zadávání nebo úpravě výsledku zápasu.
 * Obsahuje počet vstřelených branek pro tým LIGHT a tým DARK.
 *
 * Validace vstupních hodnot je zajištěna pomocí Bean Validation.
 */
public class MatchScoreUpdateRequest {

    /**
     * Počet branek týmu LIGHT.
     */
    @NotNull(message = "Skóre týmu LIGHT je povinné.")
    @Min(value = 0, message = "Skóre nemůže být záporné.")
    private Integer scoreLight;

    /**
     * Počet branek týmu DARK.
     */
    @NotNull(message = "Skóre týmu DARK je povinné.")
    @Min(value = 0, message = "Skóre nemůže být záporné.")
    private Integer scoreDark;

    public Integer getScoreLight() {
        return scoreLight;
    }

    public void setScoreLight(Integer scoreLight) {
        this.scoreLight = scoreLight;
    }

    public Integer getScoreDark() {
        return scoreDark;
    }

    public void setScoreDark(Integer scoreDark) {
        this.scoreDark = scoreDark;
    }
}