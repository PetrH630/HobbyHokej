package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchMode;

/**
 * Služba pro centralizovaný přepočet rozložení hráčů v zápase.
 *
 * Engine představuje doménovou vrstvu odpovědnou za:
 * - přepočet kapacity REGISTERED a RESERVED hráčů,
 * - zpracování navýšení kapacity,
 * - úpravu rozložení pozic při změně herního módu.
 *
 * Implementace pracuje nad entitou MatchEntity
 * a entitami registrací bez přímé vazby na controller.
 */
public interface MatchAllocationEngine {

    /**
     * Provede kompletní přepočet registrací pro daný zápas.
     *
     * Používá se zejména:
     * - po snížení kapacity,
     * - při globální validaci konzistence rozložení hráčů.
     *
     * @param matchId identifikátor zápasu
     */
    void recomputeForMatch(Long matchId);

    /**
     * Zpracuje navýšení kapacity zápasu.
     *
     * Nová místa jsou rozdělena mezi týmy tak,
     * aby došlo k co nejvyrovnanějšímu počtu REGISTERED hráčů.
     *
     * @param match zápas, u kterého byla navýšena kapacita
     * @param totalNewSlots rozdíl mezi novou a původní kapacitou
     */
    void handleCapacityIncrease(MatchEntity match, int totalNewSlots);

    /**
     * Zpracuje změnu herního systému zápasu.
     *
     * Metoda:
     * - opraví neplatné pozice hráčů,
     * - provede rebalance pozic dle nového módu.
     *
     * Statusy registrací se nemění.
     *
     * @param match zápas po změně módu
     * @param oldMatchMode původní herní mód
     */
    void handleMatchModeChange(MatchEntity match, MatchMode oldMatchMode);
}