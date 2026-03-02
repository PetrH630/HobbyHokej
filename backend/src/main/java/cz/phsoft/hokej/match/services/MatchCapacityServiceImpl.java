package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementace service vrstvy pro přepočet kapacity zápasu.
 *
 * Třída slouží jako tenká obálka nad {@link MatchAllocationEngine}
 * a zajišťuje, aby se při změně parametru maxPlayers spustil
 * odpovídající přepočet:
 * - při snížení kapacity se provede globální přepočet
 *   stavů registrací,
 * - při navýšení kapacity se nová místa rozdělí mezi týmy
 *   a vhodní kandidáti ze stavu RESERVED se povýší
 *   do stavu REGISTERED.
 */
@Service
public class MatchCapacityServiceImpl implements MatchCapacityService {

    private static final Logger log =
            LoggerFactory.getLogger(MatchCapacityServiceImpl.class);

    private final MatchAllocationEngine matchAllocationEngine;

    public MatchCapacityServiceImpl(MatchAllocationEngine matchAllocationEngine) {
        this.matchAllocationEngine = matchAllocationEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void handleCapacityChange(MatchEntity match, Integer oldMaxPlayers) {
        if (match == null) {
            return;
        }

        Integer newMaxPlayers = match.getMaxPlayers();
        // Pokud nemáme jedno z čísel, neděláme nic – typicky nový zápas
        if (newMaxPlayers == null || oldMaxPlayers == null) {
            return;
        }

        int diffMaxPlayers = newMaxPlayers - oldMaxPlayers;
        if (diffMaxPlayers == 0) {
            return;
        }

        log.debug("handleCapacityChange: matchId={}, oldMax={}, newMax={}, diff={}",
                match.getId(), oldMaxPlayers, newMaxPlayers, diffMaxPlayers);

        if (diffMaxPlayers < 0) {
            // SNÍŽENÍ kapacity – přebyteční hráči se přesunou do RESERVED
            // a následně se přepočtou pozice podle kapacity postů.
            matchAllocationEngine.recomputeForMatch(match.getId());
            return;
        }

        // ZVÝŠENÍ kapacity – nová místa se rozdělí mezi týmy
        // a vhodní kandidáti ze stavu RESERVED se povýší na REGISTERED.
        matchAllocationEngine.handleCapacityIncrease(match, diffMaxPlayers);
    }
}