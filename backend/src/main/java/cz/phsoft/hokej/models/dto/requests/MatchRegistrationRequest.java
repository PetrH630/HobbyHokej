package cz.phsoft.hokej.models.dto.requests;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO pro změnu registrace hráče na zápas.
 *
 * Slouží jako vstupní objekt pro operace:
 * - přihlášení hráče na zápas,
 * - odhlášení hráče ze zápasu,
 * - omluvení hráče,
 * - administrátorské zásahy do registrace.
 *
 * Typ a kombinace vyplněných polí určují,
 * jaká operace se v servisní vrstvě provede.
 * Tento request je zpracováván metodou
 * MatchRegistrationService.upsertRegistration.
 *
 * Ne všechna pole jsou povinná. Jejich význam a validace
 * závisí na konkrétní operaci a business logice služby.
 */
public class MatchRegistrationRequest {

    /**
     * ID zápasu, ke kterému se registrace vztahuje.
     *
     * Hodnota je povinná a musí být kladná.
     * Slouží k jednoznačnému určení zápasu,
     * u kterého se registrace mění.
     */
    @NotNull
    @Positive
    private Long matchId;

    /**
     * ID hráče, kterého se operace týká.
     *
     * Typické použití:
     * - u endpointů typu "/me" může být null a hráč
     *   se určuje podle přihlášeného uživatele,
     * - u administrátorských operací se hodnota vyplňuje explicitně.
     */
    private Long playerId;

    /**
     * Tým, do kterého je hráč přiřazen (například DARK nebo LIGHT).
     *
     * Používá se při nastavování nebo změně rozdělení týmů.
     */
    private Team team;

    /**
     * Důvod omluvy hráče.
     *
     * Používá se u operací, které představují omluvu
     * nebo odhlášení se zdůvodněním.
     */
    private ExcuseReason excuseReason;

    /**
     * Volitelná textová poznámka k omluvě od hráče.
     *
     * Umožňuje doplnit detailnější vysvětlení
     * nad rámec strukturovaného ExcuseReason.
     */
    private String excuseNote;

    /**
     * Administrátorská poznámka k registraci.
     *
     * Používá se například pro označení no-show,
     * interních komentářů nebo dalších technických poznámek.
     */
    private String adminNote;

    /**
     * Příznak, že má dojít k odhlášení hráče ze zápasu.
     *
     * Pokud je true, request reprezentuje akci odhlášení
     * (UNREGISTER) bez ohledu na ostatní volitelná pole.
     */
    private boolean unregister;

    /**
     * Příznak, že jde o registraci náhradníka.
     *
     * Pokud je true, request reprezentuje akci SUBSTITUTE,
     * tedy stav, kdy hráč projevil zájem, ale jeho účast
     * není jistá (čeká se na uvolnění místa nebo potvrzení).
     */
    private boolean substitute;

    // --- gettery / settery ---

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

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }
}
