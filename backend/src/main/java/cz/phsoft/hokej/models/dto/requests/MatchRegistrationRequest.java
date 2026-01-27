package cz.phsoft.hokej.models.dto.requests;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO pro změnu registrace hráče na zápas.
 *
 * ÚČEL:
 * -----
 * Slouží jako vstupní objekt pro operace:
 * <ul>
 *     <li>přihlášení hráče na zápas,</li>
 *     <li>odhlášení hráče ze zápasu,</li>
 *     <li>omluvení hráče (EXCUSED),</li>
 *     <li>administrátorské zásahy do registrace.</li>
 * </ul>
 *
 * Tento request je zpracováván metodou
 * {@code MatchRegistrationService#upsertRegistration(...)}.
 *
 * POZNÁMKA:
 * ---------
 * Ne všechna pole jsou povinná – jejich význam a validace
 * závisí na konkrétní operaci a business logice služby.
 */
public class MatchRegistrationRequest {

    /**
     * ID zápasu, ke kterému se registrace vztahuje.
     */
    @NotNull
    @Positive
    private Long matchId;

    /**
     * ID hráče.
     *
     * Typicky:
     * <ul>
     *     <li>u endpointů typu {@code /me} může být {@code null}
     *         a hráč je určen kontextem,</li>
     *     <li>u administrátorských operací je vyplněno explicitně.</li>
     * </ul>
     */
    private Long playerId;

    /**
     * Tým, do kterého je hráč přiřazen (např. DARK / LIGHT).
     */
    private Team team;

    /**
     * Důvod omluvy – používá se pouze při statusu
     * {@code EXCUSED}
     * {@code UNREGISTER}
     */
    private ExcuseReason excuseReason;

    /**
     * Volitelná poznámka k omluvě od hráče.
     */
    private String excuseNote;

    /**
     * Administrátorská poznámka k registraci
     * (např. no-show, interní komentář).
     */
    private String adminNote;

    /**
     * Příznak odhlášení ze zápasu.
     *
     * Pokud je {@code true}, request reprezentuje akci UNREGISTER.
     */
    private boolean unregister;

    // --- gettery ---

    public Long getPlayerId() { return playerId; }
    public Long getMatchId() { return matchId; }
    public Team getTeam() { return team; }
    public ExcuseReason getExcuseReason() { return excuseReason; }
    public String getExcuseNote() { return excuseNote; }
    public String getAdminNote() { return adminNote; }
    public boolean isUnregister() { return unregister; }
}
