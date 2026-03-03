package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementace servisní vrstvy pro přepočet kapacity zápasu.
 *
 * Třída představuje aplikační vrstvu nad MatchAllocationEngine
 * a zajišťuje vyhodnocení rozdílu mezi původní a novou kapacitou.
 *
 * Samotná doménová logika přepočtu je delegována do MatchAllocationEngine.
 */
@Service
public class MatchCapacityServiceImpl implements MatchCapacityService {

    private static final Logger log =
            LoggerFactory.getLogger(MatchCapacityServiceImpl.class);

    private final MatchAllocationEngine matchAllocationEngine;

    /**
     * Vytváří službu pro přepočet kapacity zápasu.
     *
     * @param matchAllocationEngine doménová služba pro přepočet rozložení hráčů
     */
    public MatchCapacityServiceImpl(MatchAllocationEngine matchAllocationEngine) {
        this.matchAllocationEngine = matchAllocationEngine;
    }

    /**
     * Vyhodnocuje změnu kapacity zápasu a deleguje odpovídající logiku.
     *
     * Při snížení kapacity se spustí globální přepočet registrací.
     * Při navýšení kapacity se rozdělí nová místa mezi týmy
     * a povýší se vhodní kandidáti ze stavu RESERVED.
     *
     * Operace je vedena jako transakční, protože může měnit stav registrací.
     *
     * @param match entita zápasu po uložení změny
     * @param oldMaxPlayers původní hodnota maxPlayers
     */
    @Override
    @Transactional
    public void handleCapacityChange(MatchEntity match, Integer oldMaxPlayers) {

        if (match == null) {
            return;
        }

        Integer newMaxPlayers = match.getMaxPlayers();

        // Pokud nemáme jednu z hodnot, typicky jde o nový zápas
        if (newMaxPlayers == null || oldMaxPlayers == null) {
            return;
        }

        int diffMaxPlayers = newMaxPlayers - oldMaxPlayers;

        if (diffMaxPlayers == 0) {
            return;
        }

        log.debug(
                "handleCapacityChange: matchId={}, oldMax={}, newMax={}, diff={}",
                match.getId(),
                oldMaxPlayers,
                newMaxPlayers,
                diffMaxPlayers
        );

        if (diffMaxPlayers < 0) {
            // Snížení kapacity – přebyteční hráči se přesunou do RESERVED
            matchAllocationEngine.recomputeForMatch(match.getId());
            return;
        }

        // Navýšení kapacity – rozdělí se nová místa mezi týmy
        matchAllocationEngine.handleCapacityIncrease(match, diffMaxPlayers);
    }
}