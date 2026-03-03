package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;

import java.util.List;

/**
 * Service vrstva pro čtecí operace nad registracemi hráčů.
 *
 * Odpovídá za poskytování přehledových dat o registracích hráčů
 * na zápasy bez provádění změn stavu v databázi.
 *
 * Používá se zejména z controller vrstvy pro získání seznamů registrací
 * podle konkrétního zápasu, více zápasů, konkrétního hráče
 * nebo pro identifikaci hráčů bez reakce na daný zápas.
 *
 * Implementace této služby typicky deleguje dotazy na repository vrstvu
 * a provádí mapování entit na DTO objekty.
 */
public interface MatchRegistrationQueryService {

    /**
     * Vrátí seznam registrací pro konkrétní zápas.
     *
     * Registrace jsou načteny na základě identifikátoru zápasu
     * a následně mapovány do přenosových objektů.
     *
     * @param matchId identifikátor zápasu, pro který mají být registrace načteny
     * @return seznam registrací hráčů ve formě DTO pro daný zápas
     */
    List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId);

    /**
     * Vrátí seznam registrací pro více zápasů najednou.
     *
     * Používá se například při načítání přehledových seznamů zápasů,
     * kde je potřeba agregovat registrace pro více identifikátorů.
     *
     * @param matchIds seznam identifikátorů zápasů
     * @return seznam registrací odpovídajících zadaným zápasům
     */
    List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds);

    /**
     * Vrátí všechny registrace uložené v systému.
     *
     * Metoda se používá zejména pro administrátorské přehledy
     * nebo exportní operace.
     *
     * @return seznam všech registrací ve formě DTO
     */
    List<MatchRegistrationDTO> getAllRegistrations();

    /**
     * Vrátí všechny registrace konkrétního hráče.
     *
     * Slouží pro zobrazení historie účasti hráče na zápasech
     * nebo pro výpočet statistik.
     *
     * @param playerId identifikátor hráče
     * @return seznam registrací daného hráče
     */
    List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId);

    /**
     * Vrátí seznam hráčů, kteří na daný zápas dosud nereagovali.
     *
     * Výsledkem jsou hráči bez vytvořené registrace
     * nebo se stavem odpovídajícím absenci reakce.
     *
     * @param matchId identifikátor zápasu
     * @return seznam hráčů bez reakce ve formě DTO
     */
    List<PlayerDTO> getNoResponsePlayers(Long matchId);
}