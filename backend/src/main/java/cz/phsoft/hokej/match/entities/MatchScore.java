package cz.phsoft.hokej.match.entities;

import cz.phsoft.hokej.match.enums.MatchResult;
import cz.phsoft.hokej.player.enums.Team;
import jakarta.persistence.Embeddable;

/**
 * Hodnota reprezentující skóre zápasu.
 *
 * Uchovává počet vstřelených branek pro tým LIGHT a tým DARK.
 * Třída je používána jako vložený objekt (value object) v entitě MatchEntity.
 *
 * Obsahuje doménovou logiku pro práci se skóre a určení výsledku zápasu.
 */
@Embeddable
public class MatchScore {

    private Integer light;
    private Integer dark;

    public MatchScore() {
        this.light = 0;
        this.dark = 0;
    }

    public Integer getGoals(Team team) {
        if (team == null) {
            return null;
        }
        return team == Team.LIGHT ? light : dark;
    }

    public void setGoals(Team team, Integer value) {
        if (team == null || value == null) {
            return;
        }

        if (value < 0) {
            throw new IllegalArgumentException("Skóre nemůže být záporné.");
        }

        if (team == Team.LIGHT) {
            this.light = value;
        } else {
            this.dark = value;
        }
    }

    /**
     * Vrací výsledek zápasu na základě aktuálního skóre.
     *
     * @return {@link MatchResult} reprezentující výsledek zápasu.
     */
    public MatchResult getResult() {

        if (light == null || dark == null) {
            return MatchResult.NOT_PLAYED;
        }

        if (light > dark) {
            return MatchResult.LIGHT_WIN;
        }

        if (dark > light) {
            return MatchResult.DARK_WIN;
        }

        return MatchResult.DRAW;
    }

    /**
     * Vrací vítězný tým.
     *
     * @return Vítězný tým nebo null v případě remízy či nezadaného skóre.
     */
    public Team getWinner() {

        MatchResult result = getResult();

        return switch (result) {
            case LIGHT_WIN -> Team.LIGHT;
            case DARK_WIN -> Team.DARK;
            default -> null;
        };
    }

    public Integer getLight() {
        return light;
    }

    public void setLight(Integer light) {
        if (light != null && light < 0) {
            throw new IllegalArgumentException("Skóre nemůže být záporné.");
        }
        this.light = light;
    }

    public Integer getDark() {
        return dark;
    }

    public void setDark(Integer dark) {
        if (dark != null && dark < 0) {
            throw new IllegalArgumentException("Skóre nemůže být záporné.");
        }
        this.dark = dark;
    }
}