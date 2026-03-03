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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Implementace změnové service vrstvy pro zápasy.
 *
 * Zajišťuje vytváření, úpravu, mazání a změny stavu zápasu.
 * Při úpravách se provádějí kontrolní mechanismy nad sezónou
 * a stavem zápasu a zajišťují se side-effects:
 * přepočet kapacity přes MatchCapacityService,
 * úprava pozic hráčů při změně herního systému přes MatchAllocationEngine
 * a odesílání notifikací hráčům o změnách souvisejících se zápasem.
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
     * Při vytvoření se injektují závislosti na repository, mappery,
     * služby pro sezóny, notifikace, uživatele, kapacitu zápasu a
     * alokační engine pro herní systém.
     *
     * @param matchRepository             Repozitář pro perzistenci zápasů.
     * @param matchRegistrationRepository Repozitář pro perzistenci registrací na zápasy.
     * @param matchMapper                 Mapper pro převod mezi entitou a DTO zápasu.
     * @param seasonService               Služba pro práci se sezónami.
     * @param currentSeasonService        Služba pro držení aktuální sezóny v kontextu aplikace.
     * @param notificationService         Služba pro odesílání notifikací hráčům.
     * @param appUserRepository           Repozitář pro uživatele aplikace.
     * @param matchCapacityService        Služba pro přepočet kapacity zápasu.
     * @param clock                       Hodiny používané pro časové operace.
     * @param matchAllocationEngine       Engine pro přepočet rozložení hráčů při změně herního systému.
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
                !java.util.Objects.equals(entity.getMaxPlayers(), oldMaxPlayers);

        boolean dateTimeChanged =
                !java.util.Objects.equals(entity.getDateTime(), oldDateTime);

        boolean locationChanged =
                !java.util.Objects.equals(entity.getLocation(), oldLocation);

        boolean priceChanged =
                !java.util.Objects.equals(entity.getPrice(), oldPrice);

        boolean matchModeChanged =
                !java.util.Objects.equals(entity.getMatchMode(), oldMatchMode);

        if (maxPlayersChanged || dateTimeChanged || locationChanged || priceChanged || matchModeChanged) {
            entity.setMatchStatus(MatchStatus.UPDATED);
        }

        MatchEntity saved = matchRepository.save(entity);

        if (matchModeChanged) {
            // Nově: změna herního systému se řeší přes MatchAllocationEngine
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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

        // Nastavení nových hodnot skóre přes doménovou logiku MatchScore
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
     * Podle typu contextu se získá entita zápasu. Následně se vyhledají
     * registrace na daný zápas a pro hráče se statusem REGISTERED,
     * RESERVED nebo SUBSTITUTE se podle cílového stavu zápasu odešlou
     * příslušné notifikace (změna času, zrušení nebo obnovení zápasu).
     *
     * @param context     Kontext změny zápasu. Může se jednat o MatchTimeChangeContext
     *                    při změně času nebo přímo o MatchEntity pro ostatní změny.
     * @param matchStatus Cílový stav zápasu, podle kterého se volí typ notifikace.
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
     * Získává identifikátor aktuálně přihlášeného uživatele.
     *
     * Informace se čte z kontextu Spring Security. Pokud není
     * uživatel autentizován nebo se nepodaří dohledat záznam
     * v databázi, vrací se null.
     *
     * @return Identifikátor přihlášeného uživatele nebo null, pokud není k dispozici.
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
     * Zjišťuje, zda má uživatel roli administrátora nebo manažera.
     *
     * Prochází se seznam autorit přihlášeného uživatele a kontroluje se,
     * zda obsahuje roli ROLE_ADMIN nebo ROLE_MANAGER. Používá se pro
     * odlišení oprávnění při úpravách zápasu mimo aktivní sezónu.
     *
     * @param auth Autentizační objekt aktuálního uživatele.
     * @return True, pokud má uživatel roli administrátora nebo manažera, jinak false.
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
     * Vyhledává zápas podle identifikátoru nebo vyhazuje výjimku.
     *
     * Používá se všude tam, kde je potřeba mít jistotu, že zápas
     * existuje. Pokud není zápas nalezen, vyhazuje se MatchNotFoundException.
     *
     * @param matchId Identifikátor hledaného zápasu.
     * @return Entita nalezeného zápasu.
     */
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Validuje, že datum zápasu spadá do období aktivní sezóny.
     *
     * Kontroluje se, zda datum zápasu není dříve než začátek aktivní
     * sezóny a zároveň není později než konec aktivní sezóny. V opačném
     * případě se vyhodí InvalidSeasonPeriodDateException.
     *
     * @param dateTime Datum a čas zápasu, který se kontroluje.
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
     * Získává identifikátor aktuální sezóny z kontextu nebo z aktivní sezóny.
     *
     * Nejprve se používá CurrentSeasonService pro získání aktuálního
     * identifikátoru sezóny. Pokud není k dispozici, použije se identifikátor
     * aktivní sezóny ze SeasonService.
     *
     * @return Identifikátor sezóny, která se má považovat za aktivní v daném kontextu.
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Vrací aktuální datum a čas.
     *
     * Používá se Clock injektovaný do služby, aby bylo možné čas
     * případně mockovat v testech. Slouží jako centrální místo
     * pro získání aktuálního času v této třídě.
     *
     * @return Aktuální datum a čas podle konfigurovaných hodin.
     */
    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}