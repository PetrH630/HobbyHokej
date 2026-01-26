package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.SeasonDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CurrentSeasonServiceImpl implements CurrentSeasonService {

    private static final String CURRENT_SEASON_SESSION_ATTR = "CURRENT_SEASON_ID";
    private static final String CURRENT_SEASON_CUSTOM_ATTR  = "CURRENT_SEASON_CUSTOM";

    private final HttpSession session;
    private final SeasonService seasonService;

    public CurrentSeasonServiceImpl(HttpSession session,
                                    SeasonService seasonService) {
        this.session = session;
        this.seasonService = seasonService;
    }

    @Override
    public Long getCurrentSeasonIdOrDefault() {
        Object value = session.getAttribute(CURRENT_SEASON_SESSION_ATTR);
        Boolean custom = (Boolean) session.getAttribute(CURRENT_SEASON_CUSTOM_ATTR);
        // Pokud si uživatel sezónu VĚDOMĚ zvolil, respektujeme ji
        if (Boolean.TRUE.equals(custom) && value != null) {
            return toLong(value);
        }

        // Jinak vždy bereme AKTUÁLNÍ aktivní sezónu z DB (adminem nastavenou)
        SeasonDTO active = seasonService.getActiveSeasonOrNull();
        if (active != null) {
            Long id = active.getId();
            session.setAttribute(CURRENT_SEASON_SESSION_ATTR, id);
            session.setAttribute(CURRENT_SEASON_CUSTOM_ATTR, Boolean.FALSE);
            return id;
        }

        // fallback - žádná aktivní sezóna
        return null;
    }

    @Override
    public void setCurrentSeasonId(Long seasonId) {
        // Uživatel si sezónu explicitně vybral
        session.setAttribute(CURRENT_SEASON_SESSION_ATTR, seasonId);
        session.setAttribute(CURRENT_SEASON_CUSTOM_ATTR, Boolean.TRUE);
    }

    @Override
    public void clearCurrentSeason() {
        session.removeAttribute(CURRENT_SEASON_SESSION_ATTR);
        session.removeAttribute(CURRENT_SEASON_CUSTOM_ATTR);
    }

    // malá pojistka pro případ, že container vrátí Integer/String místo Long
    private Long toLong(Object value) {
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        return Long.valueOf(value.toString());
    }
}

