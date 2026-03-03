package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.registration.dto.MatchRegistrationRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service vrstva pro orchestraci nad registracemi hráčů na zápasy.
 *
 * Příkazové (write) operace jsou delegovány do MatchRegistrationCommandService.
 * Čtecí operace jsou delegovány do MatchRegistrationQueryService.
 *
 * Slouží jako jednotné rozhraní pro kontrolery, které tak nemusí znát
 * rozdělení na command a query služby, a udržuje konzistentní vstupní bod
 * pro práci s registracemi na zápasy.
 */
@Service
public class MatchRegistrationServiceImpl implements MatchRegistrationService {

    /**
     * Služba zodpovědná za příkazové operace nad registracemi
     * (vytváření, změny stavů, přepočty, notifikace).
     */
    private final MatchRegistrationCommandService commandService;

    /**
     * Služba zodpovědná za čtecí operace nad registracemi
     * (přehledy, výpisy, statistiky).
     */
    private final MatchRegistrationQueryService matchRegistrationQueryService;

    /**
     * Vytváří instanci servisní vrstvy pro práci s registracemi.
     *
     * Konstruktor zajišťuje injektování samostatných služeb
     * pro command a query operace nad registracemi.
     *
     * @param commandService               služba pro příkazové operace nad registracemi
     * @param matchRegistrationQueryService služba pro čtecí operace nad registracemi
     */
    public MatchRegistrationServiceImpl(
            MatchRegistrationCommandService commandService,
            MatchRegistrationQueryService matchRegistrationQueryService
    ) {
        this.commandService = commandService;
        this.matchRegistrationQueryService = matchRegistrationQueryService;
    }

    // PŘÍKAZOVÉ OPERACE – DELEGACE DO COMMAND SERVICE

    /**
     * Vytváří nebo aktualizuje registraci hráče na zápas.
     *
     * Metoda pouze deleguje zpracování do MatchRegistrationCommandService
     * a obaluje celou operaci transakcí.
     *
     * @param playerId ID hráče
     * @param request  požadavek popisující změnu registrace
     * @return výsledná podoba registrace
     */
    @Override
    @Transactional
    public MatchRegistrationDTO upsertRegistration(Long playerId, MatchRegistrationRequest request) {
        return commandService.upsertRegistration(playerId, request);
    }

    /**
     * Označuje hráče jako neomluveného pro konkrétní zápas.
     *
     * Operace je delegována do MatchRegistrationCommandService
     * a probíhá v rámci transakce.
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote poznámka administrátora
     * @return aktualizovaná registrace
     */
    @Override
    @Transactional
    public MatchRegistrationDTO markNoExcused(Long matchId,
                                              Long playerId,
                                              String adminNote) {
        return commandService.markNoExcused(matchId, playerId, adminNote);
    }

    /**
     * Ruší stav neomluvené neúčasti a nastavuje omluvu hráče.
     *
     * Operace je delegována do MatchRegistrationCommandService
     * a probíhá v rámci transakce.
     *
     * @param matchId       ID zápasu
     * @param playerId      ID hráče
     * @param excuseReason  důvod omluvy
     * @param excuseNote    poznámka k omluvě
     * @return aktualizovaná registrace
     */
    @Override
    @Transactional
    public MatchRegistrationDTO cancelNoExcused(Long matchId,
                                                Long playerId,
                                                ExcuseReason excuseReason,
                                                String excuseNote) {
        return commandService.cancelNoExcused(matchId, playerId, excuseReason, excuseNote);
    }

    /**
     * Mění tým hráče v registraci na daný zápas.
     *
     * Implementace je delegována do MatchRegistrationCommandService
     * a probíhá v rámci transakce.
     *
     * @param playerId ID hráče
     * @param matchId  ID zápasu
     * @return aktualizovaná registrace
     */
    @Override
    @Transactional
    public MatchRegistrationDTO changeRegistrationTeam(Long playerId,
                                                       Long matchId) {
        return commandService.changeRegistrationTeam(playerId, matchId);
    }

    /**
     * Mění pozici hráče v registraci na daný zápas.
     *
     * Operace je delegována do MatchRegistrationCommandService
     * a probíhá v rámci transakce.
     *
     * @param playerId        ID hráče
     * @param matchId         ID zápasu
     * @param positionInMatch cílová pozice hráče v zápase
     * @return aktualizovaná registrace
     */
    @Override
    @Transactional
    public MatchRegistrationDTO changeRegistrationPosition(Long playerId,
                                                           Long matchId,
                                                           PlayerPosition positionInMatch) {
        return commandService.changeRegistrationPosition(playerId, matchId, positionInMatch);
    }

    /**
     * Aktualizuje stav registrace hráče na zápas.
     *
     * Operace je delegována do MatchRegistrationCommandService
     * a probíhá v rámci transakce.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @param status   nový stav registrace
     * @return aktualizovaná registrace
     */
    @Override
    @Transactional
    public MatchRegistrationDTO updateStatus(Long matchId,
                                             Long playerId,
                                             PlayerMatchStatus status) {
        return commandService.updateStatus(matchId, playerId, status);
    }

    /**
     * Přepočítává stavy registrací pro daný zápas.
     *
     * Operace je delegována do MatchRegistrationCommandService
     * a probíhá v rámci transakce.
     *
     * @param matchId ID zápasu
     */
    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {
        commandService.recalcStatusesForMatch(matchId);
    }

    /**
     * Povyšuje rezervní hráče na registrované po navýšení kapacity.
     *
     * Operace je delegována do MatchRegistrationCommandService
     * a probíhá v rámci transakce.
     *
     * @param matchId       ID zápasu
     * @param freedTeam     tým, ve kterém byla uvolněna kapacita
     * @param freedPosition pozice, na které bylo uvolněno místo
     * @param slotsCount    počet nově dostupných míst
     */
    @Override
    @Transactional
    public void promoteReservedCandidatesForCapacityIncrease(Long matchId,
                                                             Team freedTeam,
                                                             PlayerPosition freedPosition,
                                                             int slotsCount) {
        commandService.promoteReservedCandidatesForCapacityIncrease(
                matchId,
                freedTeam,
                freedPosition,
                slotsCount
        );
    }

    /**
     * Odesílá SMS zprávy registrovaným hráčům daného zápasu.
     *
     * Jedná se o příkazovou operaci delegovanou do MatchRegistrationCommandService.
     * Metoda není součástí rozhraní MatchRegistrationService, ale slouží
     * jako rozšiřující operace pro plánovače nebo administrační akce.
     *
     * @param matchId ID zápasu
     */
    @Transactional
    public void sendSmsToRegisteredPlayers(Long matchId) {
        commandService.sendSmsToRegisteredPlayers(matchId);
    }

    // ČTECÍ OPERACE – DELEGACE DO QUERY SERVICE

    /**
     * Vrací registrace pro daný zápas.
     *
     * Dotaz je delegován do MatchRegistrationQueryService.
     *
     * @param matchId ID zápasu
     * @return seznam registrací k zápasu
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId) {
        return matchRegistrationQueryService.getRegistrationsForMatch(matchId);
    }

    /**
     * Vrací registrace pro více zápasů.
     *
     * Dotaz je delegován do MatchRegistrationQueryService.
     *
     * @param matchIds seznam ID zápasů
     * @return seznam registrací pro zadané zápasy
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds) {
        return matchRegistrationQueryService.getRegistrationsForMatches(matchIds);
    }

    /**
     * Vrací všechny registrace v rámci relevantní sezóny.
     *
     * Dotaz je delegován do MatchRegistrationQueryService.
     *
     * @return seznam všech registrací
     */
    @Override
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationQueryService.getAllRegistrations();
    }

    /**
     * Vrací všechny registrace daného hráče.
     *
     * Dotaz je delegován do MatchRegistrationQueryService.
     *
     * @param playerId ID hráče
     * @return seznam registrací hráče
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId) {
        return matchRegistrationQueryService.getRegistrationsForPlayer(playerId);
    }

    /**
     * Vrací seznam hráčů, kteří dosud nereagovali na daný zápas.
     *
     * Dotaz je delegován do MatchRegistrationQueryService.
     *
     * @param matchId ID zápasu
     * @return seznam hráčů bez reakce
     */
    @Override
    public List<PlayerDTO> getNoResponsePlayers(Long matchId) {
        return matchRegistrationQueryService.getNoResponsePlayers(matchId);
    }
}