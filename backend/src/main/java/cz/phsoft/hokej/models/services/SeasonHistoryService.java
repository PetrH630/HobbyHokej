package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.SeasonHistoryDTO;

import java.util.List;

/**
 * Service rozhraní používané pro poskytování historických dat sezóny.
 *
 * Rozhraní definuje kontrakt pro načítání přehledových nebo statistických údajů
 * vztahujících se ke konkrétní sezóně. Implementace je odpovědná za sestavení
 * historických dat z příslušných entit a jejich převod do DTO vrstvy.
 *
 * Vrstva service zajišťuje business logiku a agregace dat. Controller vrstva
 * využívá toto rozhraní pro zpřístupnění historie sezóny klientovi aplikace.
 */
public interface SeasonHistoryService {

    /**
     * Vrací historická data pro zadanou sezónu.
     *
     * Implementace načítá potřebná data pro danou sezónu,
     * provádí agregace nebo výpočty podle business pravidel
     * a výsledek převádí do seznamu {@link SeasonHistoryDTO}.
     *
     * @param seasonId Identifikátor sezóny, pro kterou se historie načítá.
     * @return Seznam historických záznamů sezóny ve formě DTO.
     */
    List<SeasonHistoryDTO> getHistoryForSeason(Long seasonId);
}
