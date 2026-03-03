package cz.phsoft.hokej.match.services;

/**
 * Služba pro automatické přeskupení první lajny.
 *
 * Implementace pracuje pouze s již REGISTERED hráči
 * a nemění jejich tým. Úpravy se týkají pouze pozice
 * v rámci týmu podle kapacity a preferencí hráče.
 */
public interface MatchAutoLineupService {

    /**
     * Automaticky přeskupí pozice hráčů v zápase tak,
     * aby byly co nejlépe obsazeny všechny posty první lajny.
     *
     * @param matchId identifikátor zápasu
     */
    void autoArrangeStartingLineup(Long matchId);
}