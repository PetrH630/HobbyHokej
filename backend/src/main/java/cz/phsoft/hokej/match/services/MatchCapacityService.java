package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.services.MatchRegistrationCommandService;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;

/**
 * Service vrstva pro přepočet kapacity zápasu.
 *
 * Odpovědností této třídy je reagovat na změnu parametru maxPlayers u zápasu.
 * Při snížení kapacity provádí přepočet stavů registrací (REGISTERED/RESERVED),
 * při navýšení kapacity povyšuje vhodné kandidáty ze stavu RESERVED do stavu
 * REGISTERED a rozděluje nová místa mezi týmy.
 *
 * Pro čtecí operace nad registracemi se používá {@link MatchRegistrationService},
 * pro změny stavů registrací se používá {@link MatchRegistrationCommandService}.
 */
public interface MatchCapacityService {

    /**
     * Zpracovává změnu kapacity zápasu.
     *
     * Metoda se používá po uložení změn zápasu. Porovnává původní hodnotu
     * maxPlayers s novou hodnotou v entitě zápasu a podle rozdílu:
     * - při snížení kapacity spouští přepočet stavů registrací,
     * - při navýšení kapacity povyšuje hráče ze stavu RESERVED do stavu
     *   REGISTERED a rozděluje nová místa mezi týmy.
     *
     * Pokud nedošlo ke změně kapacity nebo je některá z hodnot null,
     * neprovádí se žádná akce.
     *
     * @param match         Zápas po uložení nových hodnot.
     * @param oldMaxPlayers Původní hodnota maxPlayers před změnou.
     */
    void handleCapacityChange(MatchEntity match, Integer oldMaxPlayers);
}