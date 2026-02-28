package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerStatsDTO;

/**
 * Service rozhraní pro poskytování statistik hráče.
 *
 * Slouží k zapouzdření business logiky výpočtu statistik hráče
 * v rámci aktuální sezóny. Implementace je odpovědná za načtení
 * relevantních zápasů, vyhodnocení aktivity hráče a agregaci
 * registračních statusů do souhrnné podoby.
 *
 * Toto rozhraní je určeno pro použití v aplikační vrstvě
 * (například z controlleru) a odděluje kontrakt od konkrétní
 * implementace výpočtu.
 */
public interface PlayerStatsService {

    /**
     * Vrací statistiku hráče za aktuální sezónu.
     *
     * Statistika zahrnuje celkový počet odehraných zápasů sezóny
     * a počty zápasů rozdělené podle registračního statusu hráče.
     *
     * @param playerId Identifikátor hráče, pro kterého se statistika počítá.
     * @return Datový přenosový objekt obsahující souhrnné statistiky hráče.
     */
    PlayerStatsDTO getPlayerStats(Long playerId);

}
