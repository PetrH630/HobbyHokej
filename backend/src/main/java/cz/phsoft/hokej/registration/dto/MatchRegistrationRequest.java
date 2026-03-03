package cz.phsoft.hokej.registration.dto;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO pro změnu registrace hráče na zápas.
 *
 * Slouží jako vstupní objekt pro operace přihlášení hráče,
 * odhlášení hráče, omluvení hráče nebo administrátorské zásahy
 * do registrace. Typ a kombinace vyplněných polí určují,
 * jaká operace se v servisní vrstvě provede.
 *
 * Objekt je zpracováván metodou MatchRegistrationService.upsertRegistration.
 * Ne všechna pole jsou povinná. Jejich význam a validace závisí
 * na konkrétní operaci a business pravidlech implementovaných
 * v servisní vrstvě.
 *
 * Třída neobsahuje žádnou aplikační logiku a slouží pouze
 * jako přenosový objekt z klienta do backendu.
 */
public class MatchRegistrationRequest {

    /**
     * Identifikátor zápasu, ke kterému se požadavek vztahuje.
     */
    @NotNull
    @Positive
    private Long matchId;

    /**
     * Identifikátor hráče, pokud je požadavek prováděn administrátorem.
     * U uživatelských operací se identita hráče určuje z kontextu přihlášení.
     */
    private Long playerId;

    /**
     * Tým, do kterého má být hráč přiřazen.
     * Používá se při nastavování nebo změně rozdělení týmů.
     */
    private Team team;

    /**
     * Důvod omluvy hráče.
     * Používá se u operací, které představují omluvu
     * nebo odhlášení se zdůvodněním.
     */
    private ExcuseReason excuseReason;

    /**
     * Volitelná textová poznámka k omluvě od hráče.
     * Umožňuje doplnit detailnější vysvětlení
     * nad rámec strukturovaného důvodu omluvy.
     */
    private String excuseNote;

    /**
     * Administrátorská poznámka k registraci.
     * Používá se například pro označení neúčasti bez omluvy
     * nebo pro interní evidenční účely.
     */
    private String adminNote;

    /**
     * Příznak určující, že má dojít k odhlášení hráče ze zápasu.
     * Pokud je nastaven na true, požadavek reprezentuje akci
     * odhlášení bez ohledu na ostatní volitelná pole.
     */
    private boolean unregister;

    /**
     * Příznak určující, že jde o registraci náhradníka.
     * Pokud je nastaven na true, požadavek reprezentuje stav,
     * kdy hráč projevil zájem o účast, ale čeká na uvolnění místa.
     */
    private boolean substitute;

    /**
     * Pozice hráče v tomto konkrétním zápase.
     * Umožňuje specifikovat, na jaké pozici má hráč nastoupit,
     * pokud to pravidla zápasu a rozdělení týmů umožňují.
     */
    private PlayerPosition positionInMatch;

    public Long getPlayerId() {
        return playerId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Team getTeam() {
        return team;
    }

    public ExcuseReason getExcuseReason() {
        return excuseReason;
    }

    public String getExcuseNote() {
        return excuseNote;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public boolean isUnregister() {
        return unregister;
    }

    public boolean isSubstitute() {
        return substitute;
    }

    /**
     * Nastavuje příznak registrace náhradníka.
     *
     * @param substitute hodnota příznaku náhradníka
     */
    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }

    public PlayerPosition getPositionInMatch() {
        return positionInMatch;
    }

    /**
     * Nastavuje pozici hráče v konkrétním zápase.
     *
     * @param positionInMatch cílová pozice hráče
     */
    public void setPositionInMatch(PlayerPosition positionInMatch) {
        this.positionInMatch = positionInMatch;
    }
}