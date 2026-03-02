package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchMode;

/**
 * Služba pro centralizovaný přepočet rozložení hráčů v zápase.
 */
public interface MatchAllocationEngine {

    /**
     * Provede přepočet rozložení hráčů pro daný zápas.
     *
     * Používá se zejména:
     * - po SNÍŽENÍ kapacity (maxPlayers),
     * - případně pro další scénáře, kde potřebujeme
     *   přepočítat REGISTERED/RESERVED a pozice.
     *
     * @param matchId Identifikátor zápasu.
     */
    void recomputeForMatch(Long matchId);

    /**
     * Zpracuje NAVÝŠENÍ kapacity zápasu.
     *
     * Nová místa se rozdělí mezi týmy DARK a LIGHT tak,
     * aby se co nejvíce vyrovnal počet registrovaných hráčů
     * v obou týmech. Při lichém počtu nových míst dostává
     * extra slot tým, který má aktuálně méně hráčů ve stavu
     * REGISTERED.
     *
     * Povyšování kandidátů ze stavu RESERVED probíhá
     * v rámci této metody.
     *
     * @param match         Zápas, u kterého byla navýšena kapacita.
     * @param totalNewSlots Celkový počet nových míst
     *                      (rozdíl mezi novou a původní kapacitou).
     */
    void handleCapacityIncrease(MatchEntity match, int totalNewSlots);

    /**
     * Zpracuje změnu herního systému zápasu (MatchMode).
     *
     * Metoda:
     * - opraví neplatné pozice hráčů podle nového módu,
     * - následně vybalancuje pozice v rámci týmů tak,
     *   aby odpovídaly kapacitě postů pro nový MatchMode.
     *
     * Statusy registrací (REGISTERED/RESERVED) se touto
     * metodou nemění – řeší je kapacitní logika.
     *
     * @param match       Zápas po změně módu.
     * @param oldMatchMode Původní herní mód (před změnou).
     */
    void handleMatchModeChange(MatchEntity match, MatchMode oldMatchMode);
}