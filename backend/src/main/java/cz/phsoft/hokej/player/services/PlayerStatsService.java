package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerStatsDTO;

/**
 * Servisní rozhraní pro poskytování statistik hráče.
 *
 * Slouží k zapouzdření business logiky výpočtu statistik hráče
 * v rámci aktuální sezóny. Implementace je odpovědná za načtení
 * relevantních zápasů, vyhodnocení aktivity hráče a agregaci
 * registračních statusů do souhrnné podoby.
 *
 * Rozhraní je určeno pro použití v aplikační vrstvě,
 * například z controlleru, a odděluje kontrakt od konkrétní
 * implementace výpočtu statistik.
 */
public interface PlayerStatsService {

    /**
     * Vrátí statistiku hráče za aktuální sezónu.
     *
     * Statistika zahrnuje celkový počet zápasů v sezóně
     * a počty zápasů rozdělené podle registračního statusu hráče.
     * Implementace je odpovědná za zajištění správného časového
     * vymezení aktuální sezóny a agregaci příslušných dat.
     *
     * @param playerId identifikátor hráče, pro kterého se statistika počítá
     * @return datový přenosový objekt obsahující souhrnné statistiky hráče
     */
    PlayerStatsDTO getPlayerStats(Long playerId);

}