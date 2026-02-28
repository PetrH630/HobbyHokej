package cz.phsoft.hokej.season.services;

import cz.phsoft.hokej.season.dto.SeasonDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Implementace rozhraní CurrentSeasonService.
 *
 * Třída spravuje identifikátor aktuálně zvolené sezóny
 * v HTTP session přihlášeného uživatele.
 * Rozlišuje se, zda byla sezóna zvolena explicitně uživatelem,
 * nebo byla nastavena automaticky na základě globálně aktivní sezóny.
 *
 * Správa samotných sezón a jejich životního cyklu není
 * odpovědností této třídy a je řešena v SeasonService.
 */
@Service
public class CurrentSeasonServiceImpl implements CurrentSeasonService {

    /**
     * Název session atributu pro uložení ID aktuální sezóny.
     */
    private static final String CURRENT_SEASON_SESSION_ATTR = "CURRENT_SEASON_ID";

    /**
     * Session atribut určující, zda byla sezóna zvolena uživatelem.
     *
     * Hodnota true značí explicitní uživatelskou volbu.
     * Hodnota false nebo null značí automatické nastavení.
     */
    private static final String CURRENT_SEASON_CUSTOM_ATTR = "CURRENT_SEASON_CUSTOM";

    /**
     * HTTP session přihlášeného uživatele.
     */
    private final HttpSession session;

    /**
     * Service poskytující informace o sezónách.
     *
     * Používá se k získání globálně aktivní sezóny.
     */
    private final SeasonService seasonService;

    public CurrentSeasonServiceImpl(HttpSession session,
                                    SeasonService seasonService) {
        this.session = session;
        this.seasonService = seasonService;
    }

    /**
     * Vrátí identifikátor aktuální sezóny podle stanovené priority.
     *
     * Nejprve se zohlední sezóna explicitně zvolená uživatelem.
     * Pokud taková volba neexistuje, použije se globálně aktivní sezóna.
     * Při automatickém nastavení se identifikátor sezóny uloží do session.
     *
     * @return ID aktuální sezóny nebo null, pokud není k dispozici žádná aktivní sezóna
     */
    @Override
    public Long getCurrentSeasonIdOrDefault() {
        Object value = session.getAttribute(CURRENT_SEASON_SESSION_ATTR);
        Boolean custom = (Boolean) session.getAttribute(CURRENT_SEASON_CUSTOM_ATTR);

        if (Boolean.TRUE.equals(custom) && value != null) {
            return toLong(value);
        }

        SeasonDTO active = seasonService.getActiveSeasonOrNull();
        if (active != null) {
            Long id = active.getId();
            session.setAttribute(CURRENT_SEASON_SESSION_ATTR, id);
            session.setAttribute(CURRENT_SEASON_CUSTOM_ATTR, Boolean.FALSE);
            return id;
        }

        return null;
    }

    /**
     * Nastaví sezónu jako aktuální pro přihlášeného uživatele.
     *
     * Sezóna je uložena do session a označena jako
     * explicitní uživatelská volba.
     *
     * @param seasonId ID sezóny, která má být nastavena
     */
    @Override
    public void setCurrentSeasonId(Long seasonId) {
        session.setAttribute(CURRENT_SEASON_SESSION_ATTR, seasonId);
        session.setAttribute(CURRENT_SEASON_CUSTOM_ATTR, Boolean.TRUE);
    }

    /**
     * Odstraní informaci o aktuální sezóně z HTTP session.
     *
     * Metoda se používá při odhlášení uživatele
     * nebo při resetu uživatelského kontextu.
     */
    @Override
    public void clearCurrentSeason() {
        session.removeAttribute(CURRENT_SEASON_SESSION_ATTR);
        session.removeAttribute(CURRENT_SEASON_CUSTOM_ATTR);
    }

    /**
     * Zajistí bezpečný převod hodnoty ze session na typ Long.
     *
     * Metoda slouží jako ochrana proti rozdílným typům hodnot,
     * které může servlet container vrátit.
     *
     * @param value hodnota načtená ze session
     * @return hodnota převedená na Long
     */
    private Long toLong(Object value) {
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        return Long.valueOf(value.toString());
    }
}
