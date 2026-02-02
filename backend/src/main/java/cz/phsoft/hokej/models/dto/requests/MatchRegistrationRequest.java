package cz.phsoft.hokej.models.dto.requests;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO používané pro změnu registrace hráče na zápas.
 *
 * Slouží jako vstupní objekt pro operace související se správou účasti
 * hráče na zápase, zejména pro přihlášení, odhlášení, omluvení hráče
 * nebo administrátorské zásahy do registrace.
 *
 * Tento request je zpracováván servisní metodou
 * MatchRegistrationService.upsertRegistration(...), která na základě
 * kombinace předaných polí a business pravidel rozhoduje o výsledném
 * stavu registrace.
 *
 * Ne všechna pole jsou povinná. Jejich význam a validace závisí
 * na konkrétní operaci, kontextu volání (uživatelský vs. administrátorský
 * endpoint) a rozhodovací logice servisní vrstvy.
 */
public class MatchRegistrationRequest {

    /**
     * ID zápasu, ke kterému se registrace vztahuje.
     *
     * Hodnota je povinná a jednoznačně identifikuje zápas,
     * pro který se má registrace vytvořit nebo aktualizovat.
     */
    @NotNull
    @Positive
    private Long matchId;

    /**
     * ID hráče, kterého se registrace týká.
     *
     * U uživatelských endpointů typu /me může být hodnota null,
     * protože hráč je určen kontextem aktuálního hráče.
     * U administrátorských operací se očekává explicitně vyplněná hodnota.
     */
    private Long playerId;

    /**
     * Tým, do kterého má být hráč v rámci zápasu přiřazen.
     *
     * Typicky se jedná o hodnoty DARK nebo LIGHT. Hodnota může být
     * nastavena uživatelem nebo administrátorem podle typu operace.
     */
    private Team team;

    /**
     * Důvod omluvy hráče.
     *
     * Používá se pouze v případě, že výsledný stav registrace
     * odpovídá omluvené účasti. Validace relevance hodnoty
     * se provádí v servisní vrstvě.
     */
    private ExcuseReason excuseReason;

    /**
     * Volitelná textová poznámka k omluvě od hráče.
     *
     * Hodnota se používá pouze při omluvení a slouží
     * pro doplnění důvodu neúčasti.
     */
    private String excuseNote;

    /**
     * Interní administrátorská poznámka k registraci.
     *
     * Používá se například pro označení neomluvené absence
     * nebo pro interní komentáře správců systému.
     */
    private String adminNote;

    /**
     * Příznak odhlášení ze zápasu.
     *
     * Pokud je hodnota nastavena na true, request reprezentuje
     * akci odhlášení hráče ze zápasu bez omluvy.
     */
    private boolean unregister;

    /**
     * Příznak náhradníka.
     *
     * Pokud je hodnota nastavena na true, request reprezentuje
     * stav, kdy hráč projevil zájem o účast, ale je veden
     * jako náhradník.
     */
    private boolean substitute;

    // gettery / settery

    public Long getMatchId() {
        return matchId;
    }

    public Long getPlayerId() {
        return playerId;
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
