package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.demo.DemoModeOperationNotAllowedException;
import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.entities.MatchScore;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.match.exceptions.InvalidMatchDateTimeException;
import cz.phsoft.hokej.match.exceptions.InvalidMatchStatusException;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.mappers.MatchMapper;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.notifications.services.MatchTimeChangeContext;
import cz.phsoft.hokej.notifications.services.NotificationService;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.season.exceptions.InvalidSeasonPeriodDateException;
import cz.phsoft.hokej.season.services.CurrentSeasonService;
import cz.phsoft.hokej.season.services.SeasonService;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Implementace service vrstvy pro změnové operace nad zápasy.
 *
 * Třída zajišťuje vytváření, úpravu, mazání a změny stavu zápasu.
 * Při změnách se provádí doménové validace, kontrola vazby na sezónu
 * a spouští se navazující procesy, jako je přepočet kapacity, přepočet
 * rozložení hráčů při změně herního systému a odeslání notifikací hráčům.
 *
 * Služba je určena pro volání z controller vrstvy a používá repository
 * pro perzistenci a mapper pro převod mezi entitou a DTO.
 */
@Service
public class MatchCommandServiceImpl implements MatchCommandService {

    private static final Logger logger = LoggerFactory.getLogger(MatchCommandServiceImpl.class);

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final MatchMapper matchMapper;
    private final SeasonService seasonService;
    private final CurrentSeasonService currentSeasonService;
    private final NotificationService notificationService;
    private final AppUserRepository appUserRepository;
    private final MatchCapacityService matchCapacityService;
    private final Clock clock;
    private final MatchAllocationEngine matchAllocationEngine;

    /**
     * Vytváří instanci služby pro změnové operace nad zápasy.
     *
     * Závislosti se injektují konstruktorem, aby byla služba plně testovatelná
     * a aby byly vazby na repository, služby a doménové komponenty explicitní.
     *
     * @param matchRepository repozitář pro perzistenci zápasů
     * @param matchRegistrationRepository repozitář pro perzistenci registrací na zápasy
     * @param matchMapper mapper pro převod mezi entitou a DTO zápasu
     * @param seasonService služba pro práci se sezónami a aktivní sezónou
     * @param currentSeasonService služba pro práci s aktuální sezónou v kontextu uživatele
     * @param notificationService služba pro odesílání notifikací hráčům
     * @param appUserRepository repozitář pro načítání uživatelů podle identity
     * @param matchCapacityService služba pro přepočet kapacity zápasu při změně limitů
     * @param clock hodiny používané pro získání aktuálního času
     * @param matchAllocationEngine komponenta pro přepočet rozložení hráčů při změně herního systému
     */
    public MatchCommandServiceImpl(
            MatchRepository matchRepository,
            MatchRegistrationRepository matchRegistrationRepository,
            MatchMapper matchMapper,
            SeasonService seasonService,
            CurrentSeasonService currentSeasonService,
            NotificationService notificationService,
            AppUserRepository appUserRepository,
            MatchCapacityService matchCapacityService,
            Clock clock,
            MatchAllocationEngine matchAllocationEngine
    ) {
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.matchMapper = matchMapper;
        this.seasonService = seasonService;
        this.currentSeasonService = currentSeasonService;
        this.notificationService = notificationService;
        this.appUserRepository = appUserRepository;
        this.matchCapacityService = matchCapacityService;
        this.clock = clock;
        this.matchAllocationEngine = matchAllocationEngine;
    }

    // COMMANDS

    /**
     * Vytvoří nový zápas na základě dodaného DTO.
     *
     * Před uložením se validuje, že datum zápasu spadá do aktivní sezóny.
     * Následně se nastaví aktivní sezóna a auditní informace o uživateli,
     * který zápas vytvořil.
     *
     * @param dto vstupní data zápasu
     * @return uložený zápas převedený do DTO
     */
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        validateMatchDateInActiveSeason(entity.getDateTime());

        entity.setSeason(seasonService.getActiveSeason());

        Long currentUserId = getCurrentUserIdOrNull();
        entity.setCreatedByUserId(currentUserId);
        entity.setLastModifiedByUserId(currentUserId);

        return matchMapper.toDTO(matchRepository.save(entity));
    }

    /**
     * Aktualizuje existující zápas podle identifikátoru.
     *
     * Nejprve se načte zápas z databáze. Uživatel bez role administrátora
     * nebo manažera smí upravovat pouze zápasy z aktivní sezóny. Dále se
     * při úpravě sledují změny vybraných polí, aby bylo možné vyhodnotit,
     * zda se má změnit stav zápasu a zda se mají spustit navazující akce.
     *
     * Při změně herního systému se deleguje přepočet rozložení hráčů do
     * MatchAllocationEngine. Při změně kapacity se deleguje přepočet do
     * MatchCapacityService. Při změně času se odesílají notifikace hráčům.
     *
     * @param id identifikátor upravovaného zápasu
     * @param dto nová data zápasu
     * @return aktualizovaný zápas převedený do DTO
     */
    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        MatchEntity entity = findMatchOrThrow(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrManager = hasAdminOrManagerRole(auth);

        if (!isAdminOrManager) {
            Long activeSeasonId = seasonService.getActiveSeason().getId();
            if (!entity.getSeason().getId().equals(activeSeasonId)) {
                throw new InvalidMatchStatusException(
                        id, " - Zápas nepatří do aktuální sezóny, nelze ho upravit."
                );
            }
        }

        Integer oldMaxPlayers = entity.getMaxPlayers();
        LocalDateTime oldDateTime = entity.getDateTime();
        String oldLocation = entity.getLocation();
        Integer oldPrice = entity.getPrice();
        MatchMode oldMatchMode = entity.getMatchMode();

        matchMapper.updateEntity(dto, entity);
        logger.info("UPDATE matchId={}, oldMode={}, newModeAfterMap={}",
                id, oldMatchMode, entity.getMatchMode());

        Long currentUserId = getCurrentUserIdOrNull();
        entity.setLastModifiedByUserId(currentUserId);

        if (!isAdminOrManager) {
            validateMatchDateInActiveSeason(entity.getDateTime());
        }

        if (entity.getDateTime() != null
                && entity.getDateTime().isBefore(now())) {
            throw new InvalidMatchDateTimeException("Zápas by již byl minulostí");
        }

        boolean maxPlayersChanged =
                !Objects.equals(entity.getMaxPlayers(), oldMaxPlayers);

        boolean dateTimeChanged =
                !Objects.equals(entity.getDateTime(), oldDateTime);

        boolean locationChanged =
                !Objects.equals(entity.getLocation(), oldLocation);

        boolean priceChanged =
                !Objects.equals(entity.getPrice(), oldPrice);

        boolean matchModeChanged =
                !Objects.equals(entity.getMatchMode(), oldMatchMode);

        if (maxPlayersChanged || dateTimeChanged || locationChanged || priceChanged || matchModeChanged) {
            entity.setMatchStatus(MatchStatus.UPDATED);
        }

        MatchEntity saved = matchRepository.save(entity);

        if (matchModeChanged) {
            matchAllocationEngine.handleMatchModeChange(saved, oldMatchMode);
        }

        if (maxPlayersChanged) {
            matchCapacityService.handleCapacityChange(saved, oldMaxPlayers);
        }

        if (dateTimeChanged) {
            MatchTimeChangeContext ctx = new MatchTimeChangeContext(saved, oldDateTime);
            notifyPlayersAboutMatchChanges(ctx, MatchStatus.UPDATED);
        }

        return matchMapper.toDTO(saved);
    }

    /**
     * Odstraní zápas podle identifikátoru.
     *
     * V demo režimu se mazání blokuje a vyhazuje se výjimka, aby nedošlo
     * ke změně dat v ukázkovém prostředí. V běžném režimu se zápas odstraní
     * z databáze a vrátí se standardní úspěšná odpověď.
     *
     * @param id identifikátor mazaného zápasu
     * @return DTO s informací o úspěšném provedení operace
     */
    @Override
    public SuccessResponseDTO deleteMatch(Long id) {
        MatchEntity match = findMatchOrThrow(id);

        if (isDemoMode) {
            throw new DemoModeOperationNotAllowedException(
                    "Zápas nebude odstraněn. Aplikace běží v DEMO režimu."
            );
        }

        matchRepository.delete(match);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně smazán",
                id,
                now().toString()
        );
    }

    /**
     * Zruší zápas a uloží důvod zrušení.
     *
     * Operace je transakční, aby se změna stavu a uložení důvodu provedly
     * atomicky. Po uložení se odešlou notifikace hráčům podle typu změny.
     *
     * @param matchId identifikátor rušeného zápasu
     * @param reason důvod zrušení zápasu
     * @return DTO s informací o úspěšném provedení operace
     */
    @Override
    @Transactional
    public SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason) {
        MatchEntity match = findMatchOrThrow(matchId);
        String message = " je již zrušen";

        if (match.getMatchStatus() == MatchStatus.CANCELED) {
            throw new InvalidMatchStatusException(matchId, message);
        }

        match.setMatchStatus(MatchStatus.CANCELED);
        match.setCancelReason(reason);

        Long currentUserId = getCurrentUserIdOrNull();
        match.setLastModifiedByUserId(currentUserId);

        MatchEntity saved = matchRepository.save(match);
        notifyPlayersAboutMatchChanges(saved, MatchStatus.CANCELED);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně zrušen",
                match.getId(),
                now().toString()
        );
    }

    /**
     * Obnoví dříve zrušený zápas.
     *
     * Operace je povolena pouze pro zápas ve stavu CANCELED. Po změně stavu
     * se odstraní důvod zrušení a odešlou se notifikace hráčům o obnovení.
     *
     * @param matchId identifikátor obnovovaného zápasu
     * @return DTO s informací o úspěšném provedení operace
     */
    @Override
    @Transactional
    public SuccessResponseDTO unCancelMatch(Long matchId) {
        MatchEntity match = findMatchOrThrow(matchId);
        String message = " ještě nebyl zrušen";

        if (match.getMatchStatus() != MatchStatus.CANCELED) {
            throw new InvalidMatchStatusException(matchId, message);
        }

        match.setMatchStatus(MatchStatus.UNCANCELED);
        match.setCancelReason(null);

        Long currentUserId = getCurrentUserIdOrNull();
        match.setLastModifiedByUserId(currentUserId);

        MatchEntity saved = matchRepository.save(match);
        notifyPlayersAboutMatchChanges(saved, MatchStatus.UNCANCELED);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně obnoven",
                match.getId(),
                now().toString()
        );
    }

    /**
     * Aktualizuje skóre zápasu.
     *
     * Vstupní hodnoty se validují proti null a záporným hodnotám. Skóre nelze
     * měnit u zrušeného zápasu. Pokud dosud skóre neexistuje, vytvoří se nový
     * objekt MatchScore a přiřadí se k zápasu. Změna skóre nastaví stav zápasu
     * na UPDATED, aby bylo zřejmé, že došlo k aktualizaci výsledku.
     *
     * @param matchId identifikátor zápasu
     * @param scoreLight počet gólů týmu LIGHT
     * @param scoreDark počet gólů týmu DARK
     * @return aktualizovaný zápas převedený do DTO
     */
    @Override
    @Transactional
    public MatchDTO updateMatchScore(Long matchId, Integer scoreLight, Integer scoreDark) {
        if (scoreLight == null || scoreDark == null) {
            throw new IllegalArgumentException("Skóre musí být vyplněno pro oba týmy.");
        }

        if (scoreLight < 0 || scoreDark < 0) {
            throw new IllegalArgumentException("Skóre nemůže být záporné.");
        }

        MatchEntity match = findMatchOrThrow(matchId);

        if (match.getMatchStatus() == MatchStatus.CANCELED) {
            throw new InvalidMatchStatusException(
                    matchId,
                    " - Nelze měnit skóre zrušeného zápasu."
            );
        }

        MatchScore score = match.getScore();
        if (score == null) {
            score = new MatchScore();
            match.setScore(score);
        }

        Integer oldLight = score.getLight();
        Integer oldDark = score.getDark();

        score.setGoals(Team.LIGHT, scoreLight);
        score.setGoals(Team.DARK, scoreDark);

        boolean scoreChanged =
                !Objects.equals(oldLight, scoreLight) ||
                        !Objects.equals(oldDark, scoreDark);

        if (scoreChanged) {
            match.setMatchStatus(MatchStatus.UPDATED);
        }

        Long currentUserId = getCurrentUserIdOrNull();
        match.setLastModifiedByUserId(currentUserId);

        MatchEntity saved = matchRepository.save(match);

        logger.info(
                "MATCH SCORE UPDATED: matchId={}, oldScore=({},{}) newScore=({},{})",
                matchId,
                oldLight, oldDark,
                scoreLight, scoreDark
        );

        return matchMapper.toDTO(saved);
    }

    // HELPERY – NOTIFIKACE, SEZÓNA, UŽIVATEL

    /**
     * Odesílá hráčům notifikace o změnách souvisejících se zápasem.
     *
     * Metoda sjednocuje odesílání notifikací pro různé typy změn. Z kontextu se
     * nejprve získá zápas a následně se načtou registrace k zápasu. Notifikace
     * se odesílají pouze hráčům ve stavech, které reprezentují aktivní účast
     * nebo čekání na účast.
     *
     * @param context kontext změny zápasu, který nese potřebná data pro notifikaci
     * @param matchStatus cílový stav zápasu, podle kterého se volí typ notifikace
     * @throws IllegalArgumentException pokud je předán nepodporovaný typ kontextu
     */
    private void notifyPlayersAboutMatchChanges(Object context, MatchStatus matchStatus) {
        MatchEntity match;
        if (context instanceof MatchTimeChangeContext mtc) {
            match = mtc.match();
        } else if (context instanceof MatchEntity m) {
            match = m;
        } else {
            throw new IllegalArgumentException("Nepodporovaný typ contextu: " + context);
        }

        List<MatchRegistrationEntity> registrations =
                matchRegistrationRepository.findByMatchId(match.getId());

        registrations.stream()
                .filter(reg -> reg.getStatus() == PlayerMatchStatus.REGISTERED
                        || reg.getStatus() == PlayerMatchStatus.RESERVED
                        || reg.getStatus() == PlayerMatchStatus.SUBSTITUTE)
                .forEach(reg -> {
                    PlayerEntity player = reg.getPlayer();

                    if (matchStatus == MatchStatus.UPDATED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_TIME_CHANGED,
                                context
                        );
                    }

                    if (matchStatus == MatchStatus.CANCELED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_CANCELED,
                                match
                        );
                        logger.info("CANCEL notify: matchId={}, regs={}",
                                match.getId(),
                                registrations.stream().map(r -> r.getStatus().name()).toList()
                        );
                    }

                    if (matchStatus == MatchStatus.UNCANCELED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_UNCANCELED,
                                match
                        );
                    }
                });
    }

    /**
     * Vrátí identifikátor aktuálně přihlášeného uživatele, pokud je k dispozici.
     *
     * Identita uživatele se čte z kontextu Spring Security a dohledá se v databázi.
     * Pokud není uživatel autentizován nebo není nalezen v databázi, vrátí se null.
     *
     * @return identifikátor přihlášeného uživatele nebo null
     */
    private Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String email = auth.getName();
        return appUserRepository.findByEmail(email)
                .map(AppUserEntity::getId)
                .orElse(null);
    }

    /**
     * Určí, zda má uživatel roli administrátora nebo manažera.
     *
     * Kontrolují se autority přihlášeného uživatele. Výsledek se používá pro
     * odlišení oprávnění při úpravách zápasu mimo aktivní sezónu.
     *
     * @param auth autentizační objekt aktuálního uživatele
     * @return true, pokud uživatel disponuje rolí administrátora nebo manažera, jinak false
     */
    private boolean hasAdminOrManagerRole(Authentication auth) {
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a ->
                        ROLE_ADMIN.equals(a.getAuthority()) ||
                                ROLE_MANAGER.equals(a.getAuthority())
                );
    }

    /**
     * Načte zápas podle identifikátoru nebo vyhodí výjimku.
     *
     * Metoda centralizuje načítání zápasu, aby byla chyba při neexistenci
     * zápasu řešena konzistentně napříč třídou.
     *
     * @param matchId identifikátor hledaného zápasu
     * @return nalezená entita zápasu
     * @throws MatchNotFoundException pokud zápas neexistuje
     */
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Validuje, že datum zápasu spadá do období aktivní sezóny.
     *
     * Kontroluje se, zda datum zápasu neleží před začátkem aktivní sezóny
     * nebo po jejím konci. Při porušení se vyhodí doménová výjimka sezóny.
     *
     * @param dateTime datum a čas zápasu
     * @throws InvalidSeasonPeriodDateException pokud datum nepatří do rozsahu aktivní sezóny
     */
    private void validateMatchDateInActiveSeason(LocalDateTime dateTime) {
        var activeSeason = seasonService.getActiveSeason();
        var date = dateTime.toLocalDate();

        if (date.isBefore(activeSeason.getStartDate()) ||
                date.isAfter(activeSeason.getEndDate())) {

            throw new InvalidSeasonPeriodDateException(
                    "BE - Datum zápasu musí být v rozmezí aktivní sezóny (" +
                            activeSeason.getStartDate() + " - " + activeSeason.getEndDate() + ")."
            );
        }
    }

    /**
     * Vrátí identifikátor aktuální sezóny z kontextu uživatele nebo identifikátor aktivní sezóny.
     *
     * Pokud je v kontextu aplikace zvolena konkrétní sezóna, použije se její identifikátor.
     * V opačném případě se použije identifikátor aktivní sezóny.
     *
     * @return identifikátor sezóny použitý pro daný kontext
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Vrátí aktuální datum a čas podle injektovaných hodin.
     *
     * Použití Clock umožňuje deterministické testování časově závislé logiky.
     *
     * @return aktuální datum a čas
     */
    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}