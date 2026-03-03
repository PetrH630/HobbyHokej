package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;

/**
 * Servisní rozhraní pro přepočet kapacity zápasu.
 *
 * Odpovědností této vrstvy je reagovat na změnu parametru maxPlayers
 * u zápasu a spustit odpovídající doménovou logiku.
 *
 * Při snížení kapacity se provádí globální přepočet stavů registrací.
 * Při navýšení kapacity se nová místa rozdělí mezi týmy
 * a vhodní kandidáti ze stavu RESERVED se povýší do stavu REGISTERED.
 */
public interface MatchCapacityService {

    /**
     * Zpracovává změnu kapacity zápasu.
     *
     * Metoda se volá po uložení změn zápasu. Porovnává původní hodnotu
     * maxPlayers s novou hodnotou v entitě zápasu a podle rozdílu:
     *
     * - při snížení kapacity spouští přepočet stavů registrací,
     * - při navýšení kapacity povyšuje hráče ze stavu RESERVED do stavu REGISTERED
     *   a rozděluje nová místa mezi týmy.
     *
     * Pokud nedošlo ke změně kapacity nebo je některá z hodnot null,
     * neprovádí se žádná akce.
     *
     * @param match entita zápasu po uložení nových hodnot
     * @param oldMaxPlayers původní hodnota maxPlayers před změnou
     */
    void handleCapacityChange(MatchEntity match, Integer oldMaxPlayers);
}