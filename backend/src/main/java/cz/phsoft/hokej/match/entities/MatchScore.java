package cz.phsoft.hokej.match.entities;

import cz.phsoft.hokej.match.enums.MatchResult;
import cz.phsoft.hokej.player.enums.Team;
import jakarta.persistence.Embeddable;

/**
 * Value object reprezentující skóre zápasu.
 *
 * Objekt je vložen do entit MatchEntity a MatchHistoryEntity.
 * Obsahuje doménovou logiku pro práci se skóre,
 * určení výsledku a vítěze zápasu.
 */
@Embeddable
public class MatchScore {

    private Integer light;
    private Integer dark;

    public MatchScore() {
        this.light = 0;
        this.dark = 0;
    }

    /**
     * Vrací počet branek pro zadaný tým.
     *
     * @param team tým, pro který se vrací skóre
     * @return počet branek nebo null, pokud tým není zadán
     */
    public Integer getGoals(Team team) {
        if (team == null) {
            return null;
        }
        return team == Team.LIGHT ? light : dark;
    }

    /**
     * Nastavuje počet branek pro zadaný tým.
     *
     * Hodnota nesmí být záporná.
     *
     * @param team tým, pro který se skóre nastavuje
     * @param value počet branek
     */
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
     * Určuje výsledek zápasu na základě aktuálního skóre.
     *
     * @return výsledek zápasu
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
     * Určuje vítězný tým na základě výsledku zápasu.
     *
     * @return vítězný tým nebo null v případě remízy nebo nezadaného skóre
     */
    public Team getWinner() {

        MatchResult result = getResult();

        return switch (result) {
            case LIGHT_WIN -> Team.LIGHT;
            case DARK_WIN -> Team.DARK;
            default -> null;
        };
    }

    public Integer getLight() { return light; }

    public void setLight(Integer light) {
        if (light != null && light < 0) {
            throw new IllegalArgumentException("Skóre nemůže být záporné.");
        }
        this.light = light;
    }

    public Integer getDark() { return dark; }

    public void setDark(Integer dark) {
        if (dark != null && dark < 0) {
            throw new IllegalArgumentException("Skóre nemůže být záporné.");
        }
        this.dark = dark;
    }
}