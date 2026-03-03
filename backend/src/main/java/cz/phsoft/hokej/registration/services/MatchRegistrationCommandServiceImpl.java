package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.exceptions.InvalidMatchDateTimeException;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.match.services.MatchAllocationEngine;
import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.notifications.services.NotificationService;
import cz.phsoft.hokej.notifications.sms.SmsMessageBuilder;
import cz.phsoft.hokej.notifications.sms.SmsService;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.exceptions.InvalidPlayerStatusException;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.dto.MatchRegistrationRequest;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.exceptions.DuplicateRegistrationException;
import cz.phsoft.hokej.registration.exceptions.RegistrationNotFoundException;
import cz.phsoft.hokej.registration.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.registration.util.PlayerPositionUtil;
import cz.phsoft.hokej.season.exceptions.InvalidSeasonStateException;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementace příkazové service vrstvy pro správu registrací hráčů na zápasy.
 *
 * Tato třída zajišťuje veškeré změny stavů registrací, změny týmů a pozic,
 * přepočet kapacity zápasu při změně počtu hráčů a hromadné odesílání SMS.
 *
 * Třída obsahuje business logiku registrací a souvisejících pravidel.
 * Neřeší čtecí operace nad registracemi, které zůstávají v MatchRegistrationService.
 */
@Service
public class MatchRegistrationCommandServiceImpl implements MatchRegistrationCommandService {

    private static final Logger log = LoggerFactory.getLogger(MatchRegistrationCommandServiceImpl.class);

    private final MatchRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchRegistrationMapper matchRegistrationMapper;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;
    private final NotificationService notificationService;
    private final MatchAllocationEngine matchAllocationEngine;

    public MatchRegistrationCommandServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            NotificationService notificationService,
            MatchAllocationEngine matchAllocationEngine
    ) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.notificationService = notificationService;
        this.matchAllocationEngine = matchAllocationEngine;
    }

    // HLAVNÍ METODA – UPSERT REGISTRACE HRÁČE

    /**
     * Vytváří nebo aktualizuje registraci hráče na zápas.
     *
     * Zápas a hráč se načtou z repository vrstev. Ověří se, že zápas patří
     * do aktivní sezóny a že hráč může v daném čase měnit registraci.
     * Podle typu požadavku se provede:
     * - odhlášení hráče ze zápasu,
     * - zpracování omluvy,
     * - registrace, rezervace nebo označení jako náhradník.
     *
     * Případně existující registrace se aktualizuje, nebo se vytvoří nová.
     * Po změně stavu se může provést povýšení kandidáta ze stavu RESERVED
     * a odeslání notifikace hráči. Výsledek se vrací jako DTO.
     *
     * @param playerId identifikátor hráče
     * @param request požadavek na změnu nebo vytvoření registrace
     * @return uložená nebo aktualizovaná registrace převedená do DTO
     */
    @Override
    @Transactional
    public MatchRegistrationDTO upsertRegistration(Long playerId, MatchRegistrationRequest request) {

        MatchEntity match = getMatchOrThrow(request.getMatchId());
        PlayerEntity player = getPlayerOrThrow(playerId);

        // Ověření, že zápas patří do aktivní sezóny.
        assertMatchInActiveSeason(match);

        // Ověření oprávnění hráče měnit registraci v čase.
        assertPlayerCanModifyMatch(match);

        MatchRegistrationEntity registration =
                getRegistrationOrNull(playerId, request.getMatchId());

        if (registration == null && !request.isUnregister()) {
            registration = new MatchRegistrationEntity();
            registration.setMatch(match);
            registration.setPlayer(player);
        }

        PlayerMatchStatus originalStatus =
                (registration != null) ? registration.getStatus() : null;

        PlayerMatchStatus newStatus;

        if (request.isUnregister()) {
            newStatus = handleUnregister(request, playerId, registration);
        } else if (request.getExcuseReason() != null) {
            newStatus = handleExcuse(request, match, player, registration);
        } else {
            newStatus = handleRegisterOrReserveOrSubstitute(request, match, player, registration);
        }

        applyRequestDetails(registration, request);

        registration.setStatus(newStatus);
        registration.setTimestamp(now());
        registration.setCreatedBy("user");

        registration = registrationRepository.save(registration);

        // Po odhlášení ze stavu REGISTERED se provede pokus o povýšení
        // nejvhodnějšího kandidáta ze stavu RESERVED.
        if (request.isUnregister() && originalStatus == PlayerMatchStatus.REGISTERED) {
            promoteReservedCandidateAfterUnregister(match, registration);
        }

        NotificationType notificationType = resolveNotificationType(newStatus);
        if (notificationType != null) {
            notifyPlayer(player, notificationType, registration);
        }

        return matchRegistrationMapper.toDTO(registration);
    }

    // OMLUVY A NO_EXCUSED

    /**
     * Nastavuje registraci hráče do stavu NO_EXCUSED.
     *
     * Metoda se používá pro označení hráče, který se nedostavil na již proběhlý
     * zápas bez omluvy. Zápas musí být v minulosti a registrace musí být ve stavu
     * REGISTERED. Případné původní omluvy se odstraní a uloží se administrátorská
     * poznámka. Výsledek se uloží a hráč může být informován notifikací.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @param adminNote administrátorská poznámka k označení bez omluvy
     * @return aktualizovaná registrace převedená do DTO
     */
    @Override
    @Transactional
    public MatchRegistrationDTO markNoExcused(Long matchId,
                                              Long playerId,
                                              String adminNote) {

        MatchEntity match = getMatchOrThrow(matchId);
        PlayerEntity player = getPlayerOrThrow(playerId);

        if (match.getDateTime().isAfter(now())) {
            throw new InvalidPlayerStatusException(
                    "BE - Status NO_EXCUSED lze nastavit pouze u již proběhlého zápasu."
            );
        }

        MatchRegistrationEntity registration = getRegistrationOrThrow(playerId, matchId);

        if (registration.getStatus() != PlayerMatchStatus.REGISTERED) {
            throw new InvalidPlayerStatusException(
                    "BE - Status NO_EXCUSED lze nastavit pouze z registrace REGISTERED."
            );
        }

        registration.setExcuseReason(null);
        registration.setExcuseNote(null);

        if (adminNote == null || adminNote.isBlank()) {
            registration.setAdminNote("Nedostavil se bez omluvy");
        } else {
            registration.setAdminNote(adminNote);
        }

        MatchRegistrationEntity updated =
                updateRegistrationStatus(
                        registration,
                        PlayerMatchStatus.NO_EXCUSED,
                        "admin",
                        true
                );

        PlayerMatchStatus newStatus = PlayerMatchStatus.NO_EXCUSED;

        NotificationType notificationType = resolveNotificationType(newStatus);
        if (notificationType != null) {
            notifyPlayer(player, notificationType, updated);
        }

        return matchRegistrationMapper.toDTO(updated);
    }

    /**
     * Ruší stav NO_EXCUSED a nastavuje registraci do stavu EXCUSED.
     *
     * Metoda se používá v situaci, kdy se zpětně uzná omluva hráče
     * u již proběhlého zápasu. Zápas musí být v minulosti a registrace
     * musí být ve stavu NO_EXCUSED. Nastaví se důvod omluvy, poznámka
     * omluvy a registrace se uloží ve stavu EXCUSED.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @param excuseReason důvod omluvy, případně null
     * @param excuseNote text omluvy, případně null
     * @return aktualizovaná registrace převedená do DTO
     */
    @Override
    @Transactional
    public MatchRegistrationDTO cancelNoExcused(Long matchId,
                                                Long playerId,
                                                ExcuseReason excuseReason,
                                                String excuseNote) {

        MatchEntity match = getMatchOrThrow(matchId);
        getPlayerOrThrow(playerId);

        if (match.getDateTime().isAfter(now())) {
            throw new InvalidPlayerStatusException(
                    "BE - Status EXCUSED po NO-EXCUSED lze nastavit pouze u již proběhlého zápasu."
            );
        }

        MatchRegistrationEntity registration = getRegistrationOrThrow(playerId, matchId);

        if (registration.getStatus() != PlayerMatchStatus.NO_EXCUSED) {
            throw new InvalidPlayerStatusException(
                    "BE - Status EXCUSED (zrušení neomluvení) lze nastavit pouze u hráče se statutem NO_EXCUSED."
            );
        }

        registration.setExcuseReason(excuseReason != null ? excuseReason : ExcuseReason.JINE);
        registration.setAdminNote(null);
        if (excuseNote == null || excuseNote.isBlank()) {
            registration.setExcuseNote("Opravdu nemohl");
        } else {
            registration.setExcuseNote(excuseNote);
        }

        MatchRegistrationEntity updated =
                updateRegistrationStatus(
                        registration,
                        PlayerMatchStatus.EXCUSED,
                        "manager",
                        true
                );

        return matchRegistrationMapper.toDTO(updated);
    }

    // ZMĚNY TÝMU A POZICE

    /**
     * Mění tým hráče v rámci registrace na zápas.
     *
     * Metoda se používá zejména při vyrovnávání týmů. Pro hráče je změna
     * povolena pouze u budoucích zápasů, administrátor může měnit tým i jinak.
     * Registrace musí být ve stavu REGISTERED. Po změně týmu se registrace uloží
     * a hráči může být odeslána notifikace.
     *
     * @param playerId identifikátor hráče
     * @param matchId identifikátor zápasu
     * @return aktualizovaná registrace převedená do DTO
     */
    @Override
    @Transactional
    public MatchRegistrationDTO changeRegistrationTeam(Long playerId,
                                                       Long matchId) {

        MatchEntity match = getMatchOrThrow(matchId);
        PlayerEntity player = getPlayerOrThrow(playerId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.debug("changeRegistrationTeam – current user: {}", auth.getName());
            log.debug("changeRegistrationTeam – authorities: {}", auth.getAuthorities());
        } else {
            log.debug("changeRegistrationTeam – no authenticated user");
        }

        if (match.getDateTime().isBefore(now()) && isCurrentUserPlayer()) {
            throw new InvalidMatchDateTimeException(
                    "BE - Team lze změnit pouze u zápasů, které teprve budou."
            );
        }

        MatchRegistrationEntity registration = getRegistrationOrThrow(playerId, matchId);
        Team oldTeam = registration.getTeam();

        if (registration.getStatus() != PlayerMatchStatus.REGISTERED) {
            throw new InvalidPlayerStatusException(
                    "BE - Team lze změnit pouze z registrace REGISTERED."
            );
        }

        PlayerMatchStatus newStatus = PlayerMatchStatus.REGISTERED;
        Team newTeam = oldTeam.opposite();

        registration.setTeam(newTeam);

        registration = registrationRepository.save(registration);
        NotificationType notificationType = resolveNotificationType(newStatus);
        if (notificationType != null) {
            notifyPlayer(player, notificationType, registration);
        }
        return matchRegistrationMapper.toDTO(registration);
    }

    /**
     * Mění pozici hráče v rámci konkrétního zápasu.
     *
     * Metoda se používá pro přesun hráče mezi pozicemi v zápase.
     * Pro běžného hráče je změna povolena pouze u budoucích zápasů.
     * Pozice lze měnit jen u aktivních registrací se stavy REGISTERED,
     * RESERVED nebo SUBSTITUTE. Po změně se registrace uloží a vrátí se jako DTO.
     *
     * @param playerId identifikátor hráče
     * @param matchId identifikátor zápasu
     * @param positionInMatch cílová pozice hráče v daném zápase
     * @return aktualizovaná registrace převedená do DTO
     */
    @Override
    @Transactional
    public MatchRegistrationDTO changeRegistrationPosition(Long playerId,
                                                           Long matchId,
                                                           PlayerPosition positionInMatch) {

        MatchEntity match = getMatchOrThrow(matchId);
        PlayerEntity player = getPlayerOrThrow(playerId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.debug("changeRegistrationPosition – current user: {}", auth.getName());
            log.debug("changeRegistrationPosition – authorities: {}", auth.getAuthorities());
        } else {
            log.debug("changeRegistrationPosition – no authenticated user");
        }

        if (match.getDateTime().isBefore(now()) && isCurrentUserPlayer()) {
            throw new InvalidMatchDateTimeException(
                    "BE - Pozici lze změnit pouze u zápasů, které teprve budou."
            );
        }

        MatchRegistrationEntity registration = getRegistrationOrThrow(playerId, matchId);

        if (registration.getStatus() == PlayerMatchStatus.UNREGISTERED
                || registration.getStatus() == PlayerMatchStatus.NO_EXCUSED
                || registration.getStatus() == PlayerMatchStatus.EXCUSED) {
            throw new InvalidPlayerStatusException(
                    "BE - Pozici lze měnit pouze u aktivních registrací (REGISTERED, RESERVED, SUBSTITUTE)."
            );
        }

        registration.setPositionInMatch(positionInMatch);

        registration = registrationRepository.save(registration);

        return matchRegistrationMapper.toDTO(registration);
    }

    /**
     * Provádí administrátorskou změnu stavu registrace.
     *
     * Metoda umožňuje ruční nastavení stavu registrace, s výjimkou stavu
     * NO_EXCUSED, který musí být nastaven specializovaným postupem.
     * Registrace se načte, aktualizuje se stav a změna se uloží.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @param status cílový stav registrace
     * @return aktualizovaná registrace převedená do DTO
     */
    @Override
    @Transactional
    public MatchRegistrationDTO updateStatus(Long matchId,
                                             Long playerId,
                                             PlayerMatchStatus status) {

        getMatchOrThrow(matchId);
        getPlayerOrThrow(playerId);

        if (status == PlayerMatchStatus.NO_EXCUSED) {
            throw new InvalidPlayerStatusException(
                    "BE - Status NO_EXCUSED musí být nastaven přes speciální endpoint / logiku."
            );
        }

        MatchRegistrationEntity registration = getRegistrationOrThrow(playerId, matchId);

        MatchRegistrationEntity updated =
                updateRegistrationStatus(registration, status, "admin", true);

        return matchRegistrationMapper.toDTO(updated);
    }

    // PŘEPOČET KAPACITY

    /**
     * Přepočítává stavy registrací pro daný zápas.
     *
     * Přepočet je centralizován v MatchAllocationEngine. Tato metoda
     * zajišťuje kompatibilitu rozhraní a deleguje výpočet na engine.
     *
     * @param matchId identifikátor zápasu, pro který se mají stavy přepočítat
     */
    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {
        matchAllocationEngine.recomputeForMatch(matchId);
    }

    /**
     * Povyšuje kandidáty ze stavu RESERVED do stavu REGISTERED
     * při navýšení kapacity zápasu.
     *
     * Metoda se používá při uvolnění míst v zápase nebo při zvýšení
     * celkové kapacity. Nejprve se ověří, kolik míst lze reálně obsadit.
     * Poté se kandidáti ve stavu RESERVED zpracovávají v pořadí podle času
     * registrace a pro každého se zkusí povýšení do volného slotu
     * s ohledem na tým a pozici.
     *
     * @param matchId identifikátor zápasu
     * @param freedTeam tým, ve kterém se místo uvolnilo, nebo preferovaný tým
     * @param freedPosition pozice, která se uvolnila, nebo null
     * @param slotsCount počet nových míst, která se mají obsadit
     */
    @Override
    @Transactional
    public void promoteReservedCandidatesForCapacityIncrease(Long matchId,
                                                             Team freedTeam,
                                                             PlayerPosition freedPosition,
                                                             int slotsCount) {

        if (slotsCount <= 0) {
            return;
        }

        MatchEntity match = getMatchOrThrow(matchId);

        long registeredCount = registrationRepository
                .countByMatchIdAndStatus(matchId, PlayerMatchStatus.REGISTERED);

        int maxPlayers = match.getMaxPlayers();

        int remainingSlotsToFill = Math.min(slotsCount, maxPlayers - (int) registeredCount);
        if (remainingSlotsToFill <= 0) {
            return;
        }

        List<MatchRegistrationEntity> reserved = registrationRepository
                .findByMatchIdAndStatus(matchId, PlayerMatchStatus.RESERVED)
                .stream()
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        for (MatchRegistrationEntity candidate : reserved) {
            if (remainingSlotsToFill <= 0) {
                break;
            }

            boolean promoted = tryPromoteCandidateToFreedSlot(
                    candidate,
                    freedTeam,
                    freedPosition
            );

            if (promoted) {
                remainingSlotsToFill--;
            }
        }
    }

    // SMS – HROMADNÉ ODESÍLÁNÍ

    /**
     * Odesílá SMS zprávu všem hráčům ve stavu REGISTERED,
     * kteří mají povolené SMS notifikace.
     *
     * Metoda pro daný zápas načte registrace ve stavu REGISTERED,
     * pro každého hráče ověří nastavení SMS a existenci telefonního čísla
     * a následně se pokusí odeslat SMS prostřednictvím SmsService.
     *
     * @param matchId identifikátor zápasu
     */
    @Override
    @Transactional
    public void sendSmsToRegisteredPlayers(Long matchId) {
        registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .forEach(r -> {
                    PlayerEntity player = r.getPlayer();
                    if (player == null) {
                        return;
                    }

                    var settings = player.getSettings();
                    if (settings == null || !settings.isSmsEnabled()) {
                        return;
                    }

                    sendSms(r, smsMessageBuilder.buildMessageFinal(r));
                });
    }

    // PRIVÁTNÍ METODY

    /**
     * Zpracovává registraci, rezervaci nebo označení hráče jako náhradníka.
     *
     * Metoda rozhoduje o cílovém stavu registrace na základě požadavku, stavu
     * zápasu a hráče. Zohledňuje celkovou kapacitu zápasu a kapacitu pozic
     * podle týmů. V případě náhradníka se neblokuje kapacita zápasu.
     *
     * @param request požadavek na změnu registrace
     * @param match zápas, ke kterému se registrace vztahuje
     * @param player hráč, pro kterého se registrace vytváří nebo aktualizuje
     * @param registration existující registrace nebo null
     * @return cílový stav registrace, který má být uložen
     */
    private PlayerMatchStatus handleRegisterOrReserveOrSubstitute(
            MatchRegistrationRequest request,
            MatchEntity match,
            PlayerEntity player,
            MatchRegistrationEntity registration
    ) {
        PlayerMatchStatus currentStatus =
                (registration != null) ? registration.getStatus() : null;

        boolean isAlreadyRegistered = currentStatus == PlayerMatchStatus.REGISTERED;

        if (isAlreadyRegistered) {
            throw new DuplicateRegistrationException(request.getMatchId(), player.getId());
        }

        // Registrace jako náhradník (SUBSTITUTE) – hráč je označen jako „možná“
        // a neblokuje kapacitu ani pořadí.
        if (request.isSubstitute()) {
            if (currentStatus == PlayerMatchStatus.SUBSTITUTE) {
                throw new DuplicateRegistrationException(
                        request.getMatchId(),
                        player.getId(),
                        "Hráč již má zaregistrováno - možná"
                );
            }

            clearExcuseIfNeeded(registration);
            return PlayerMatchStatus.SUBSTITUTE;
        }

        // Nejdřív globální kapacita zápasu (maxPlayers)
        PlayerMatchStatus baseStatus =
                isSlotAvailable(match) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;

        // Pokud už teď víme, že se hráč nevejde do celkové kapacity,
        // vrátíme rovnou RESERVED – konkrétní pozice v tu chvíli slot neblokuje.
        if (baseStatus == PlayerMatchStatus.RESERVED) {
            clearExcuseIfNeeded(registration);
            return PlayerMatchStatus.RESERVED;
        }

        // Tady víme, že v zápase je globálně volné místo → kandidát na REGISTERED.
        // Teď musíme ověřit kapacitu konkrétní pozice v rámci týmu.

        // Tým – priorita: existující registrace -> request
        Team targetTeam = (registration != null && registration.getTeam() != null)
                ? registration.getTeam()
                : request.getTeam();

        // Pozice – priorita: request -> registrace -> primaryPosition hráče
        PlayerPosition targetPosition = request.getPositionInMatch();
        if (targetPosition == null && registration != null) {
            targetPosition = registration.getPositionInMatch();
        }
        if (targetPosition == null) {
            targetPosition = player.getPrimaryPosition();
        }

        boolean positionAvailable =
                isPositionSlotAvailableForTeam(match, targetTeam, targetPosition);

        clearExcuseIfNeeded(registration);

        // Pokud je konkrétní pozice pro daný tým plná → uloží se registrace jako RESERVED.
        // Pozice se normálně uloží v applyRequestDetails(...) – má být použita
        // pro případné povýšení na danou pozici.
        return positionAvailable ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;
    }

    /**
     * Zpracovává odhlášení hráče ze zápasu.
     *
     * Metoda ověřuje, zda existuje registrace ve stavu REGISTERED nebo RESERVED.
     * V případě úspěchu se na registraci nastaví údaje o omluvě z požadavku
     * a vrátí se stav UNREGISTERED, který se následně uloží.
     *
     * @param request požadavek na změnu registrace
     * @param playerId identifikátor hráče
     * @param registration existující registrace, která se má zrušit
     * @return cílový stav registrace UNREGISTERED
     */
    private PlayerMatchStatus handleUnregister(
            MatchRegistrationRequest request,
            Long playerId,
            MatchRegistrationEntity registration
    ) {
        boolean isAllowedUnregisterStatus =
                registration != null
                        && (registration.getStatus() == PlayerMatchStatus.REGISTERED
                        || registration.getStatus() == PlayerMatchStatus.RESERVED);

        if (!isAllowedUnregisterStatus) {
            throw new RegistrationNotFoundException(request.getMatchId(), playerId);
        }

        registration.setExcuseReason(request.getExcuseReason());
        registration.setExcuseNote(request.getExcuseNote());

        return PlayerMatchStatus.UNREGISTERED;
    }

    /**
     * Zpracovává omluvu hráče pro daný zápas.
     *
     * Metoda umožňuje nastavit stav EXCUSED v situaci, kdy hráč dosud
     * nereagoval na zápas, byl veden jako náhradník nebo byl označen
     * jako NO_EXCUSED. Na registraci se uloží důvod a poznámka omluvy.
     *
     * @param request požadavek na omluvu
     * @param match zápas, ke kterému se omluva vztahuje
     * @param player omlouvající se hráč
     * @param registration existující registrace nebo nově vytvářená registrace
     * @return cílový stav registrace EXCUSED
     */
    private PlayerMatchStatus handleExcuse(
            MatchRegistrationRequest request,
            MatchEntity match,
            PlayerEntity player,
            MatchRegistrationEntity registration
    ) {
        boolean isNoResponseOrSubstitute =
                (registration == null
                        || registration.getStatus() == null
                        || registration.getStatus() == PlayerMatchStatus.SUBSTITUTE
                        || registration.getStatus() == PlayerMatchStatus.NO_EXCUSED);

        if (!isNoResponseOrSubstitute) {
            throw new DuplicateRegistrationException(
                    request.getMatchId(),
                    player.getId(),
                    "BE - Omluva je možná pouze pokud hráč dosud nereagoval na zápas, nebo byl náhradník."
            );
        }

        registration.setExcuseReason(request.getExcuseReason());
        registration.setExcuseNote(request.getExcuseNote());

        return PlayerMatchStatus.EXCUSED;
    }

    /**
     * Aplikuje podrobnosti z požadavku na registraci.
     *
     * Metoda nastavuje tým, administrátorskou poznámku, důvod omluvy,
     * text omluvy a pozici v zápase, pokud jsou v požadavku uvedeny.
     *
     * @param registration registrace, která se má aktualizovat
     * @param request požadavek obsahující změny
     */
    private void applyRequestDetails(MatchRegistrationEntity registration,
                                     MatchRegistrationRequest request) {

        if (request.getTeam() != null) {
            registration.setTeam(request.getTeam());
        }

        if (request.getAdminNote() != null) {
            registration.setAdminNote(request.getAdminNote());
        }

        if (request.getExcuseReason() != null) {
            registration.setExcuseReason(request.getExcuseReason());
        }

        if (request.getExcuseNote() != null) {
            registration.setExcuseNote(request.getExcuseNote());
        }

        if (request.getPositionInMatch() != null) {
            registration.setPositionInMatch(request.getPositionInMatch());
        }
    }

    /**
     * Ruší případné údaje o omluvě na registraci.
     *
     * Metoda nastavuje důvod omluvy a text omluvy na null, pokud jsou vyplněny.
     * Používá se při změně stavu, kdy již omluva nemá být evidována.
     *
     * @param registration registrace, u které se mají údaje o omluvě vyčistit
     */
    private void clearExcuseIfNeeded(MatchRegistrationEntity registration) {
        if (registration == null) {
            return;
        }
        if (registration.getExcuseReason() != null || registration.getExcuseNote() != null) {
            registration.setExcuseReason(null);
            registration.setExcuseNote(null);
        }
    }

    /**
     * Pokouší se povýšit jednoho kandidáta ze stavu RESERVED po odhlášení hráče.
     *
     * Metoda zjistí uvolněný tým a pozici z původní registrace a najde kandidáty
     * ve stavu RESERVED, seřazené podle času registrace. První kandidát, který
     * úspěšně obsadí uvolněný slot, je povýšen do stavu REGISTERED.
     *
     * @param match zápas, ve kterém došlo k odhlášení
     * @param canceledRegistration původní registrace, která byla zrušena
     */
    private void promoteReservedCandidateAfterUnregister(MatchEntity match,
                                                         MatchRegistrationEntity canceledRegistration) {

        if (match == null || canceledRegistration == null) {
            return;
        }

        Team freedTeam = canceledRegistration.getTeam();
        PlayerPosition freedPosition = canceledRegistration.getPositionInMatch();

        List<MatchRegistrationEntity> reserved = registrationRepository
                .findByMatchIdAndStatus(match.getId(), PlayerMatchStatus.RESERVED)
                .stream()
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        for (MatchRegistrationEntity candidate : reserved) {
            if (tryPromoteCandidateToFreedSlot(candidate, freedTeam, freedPosition)) {
                log.debug(
                        "promoteReservedCandidateAfterUnregister: matchId={}, candidateId={}, freedTeam={}, freedPosition={}",
                        match.getId(),
                        candidate.getId(),
                        freedTeam,
                        freedPosition
                );
                break;
            }
        }
    }

    /**
     * Pokouší se povýšit jednoho kandidáta ze stavu RESERVED do stavu REGISTERED
     * podle uvolněného týmu a pozice.
     *
     * Metoda ověřuje možnosti kandidáta měnit tým a pozici podle jeho nastavení
     * a podle kapacity pozic v zápase. V případě úspěchu se kandidát převede
     * do stavu REGISTERED, aktualizuje se tým a pozice a odešle se notifikace.
     *
     * @param candidate registrace kandidáta ve stavu RESERVED
     * @param freedTeam tým, ve kterém se místo uvolnilo
     * @param freedPosition pozice, která se uvolnila
     * @return true, pokud byl kandidát povýšen, jinak false
     */
    private boolean tryPromoteCandidateToFreedSlot(MatchRegistrationEntity candidate,
                                                   Team freedTeam,
                                                   PlayerPosition freedPosition) {

        if (candidate == null || candidate.getPlayer() == null) {
            return false;
        }

        MatchEntity match = candidate.getMatch();
        if (match == null) {
            return false;
        }

        PlayerEntity player = candidate.getPlayer();
        var settings = player.getSettings();

        boolean canMoveTeam =
                settings != null && settings.isPossibleMoveToAnotherTeam();
        boolean canChangePosition =
                settings != null && settings.isPossibleChangePlayerPosition();

        Team currentTeam = candidate.getTeam();
        PlayerPosition currentPositionInMatch = candidate.getPositionInMatch();
        PlayerPosition primaryPosition = player.getPrimaryPosition();

        PlayerPosition effectiveCurrentPosition =
                (currentPositionInMatch != null) ? currentPositionInMatch : primaryPosition;

        // 1) Vyhodnocení cílového týmu.
        Team targetTeam;
        if (freedTeam == null || currentTeam == freedTeam) {
            targetTeam = currentTeam;
        } else {
            if (!canMoveTeam) {
                return false;
            }
            targetTeam = freedTeam;
        }

        // 2) Vyhodnocení cílové pozice s ohledem na GOALIE a změnu řady.
        PlayerPosition targetPosition = resolveTargetPosition(
                effectiveCurrentPosition,
                freedPosition,
                canChangePosition
        );

        if (targetPosition == null) {
            return false;
        }

        // Kontrola kapacity pozice – pokud není volno, kandidát se NEpovýší.
        if (!isPositionSlotAvailableForTeam(match, targetTeam, targetPosition)) {
            return false;
        }

        // 3) Provést změnu týmu/pozice a statusu na REGISTERED
        candidate.setTeam(targetTeam);
        candidate.setPositionInMatch(targetPosition);

        MatchRegistrationEntity updated =
                updateRegistrationStatus(candidate, PlayerMatchStatus.REGISTERED, "system", false);

        NotificationType type = resolveNotificationType(PlayerMatchStatus.REGISTERED);
        if (type != null) {
            notifyPlayer(player, type, updated);
        }

        return true;
    }

    /**
     * Určuje cílovou pozici pro kandidáta při povýšení podle uvolněné pozice
     * a nastavení hráče.
     *
     * Metoda respektuje speciální pravidla pro brankáře, umožňuje přesuny
     * v rámci obrany nebo útoku a případně i mezi obranou a útokem,
     * pokud to hráč povolil ve svém nastavení.
     *
     * @param currentPosition aktuální pozice hráče v zápase nebo jeho primární pozice
     * @param freedPosition uvolněná pozice, která se má obsadit
     * @param canChangePosition příznak, zda hráč povolil změnu pozice mezi řadami
     * @return cílová pozice, pokud je změna možná, jinak null
     */
    private PlayerPosition resolveTargetPosition(PlayerPosition currentPosition,
                                                 PlayerPosition freedPosition,
                                                 boolean canChangePosition) {

        if (freedPosition == null) {
            return currentPosition;
        }

        // GOALIE – speciální pravidlo: pouze kandidát, který je již veden jako GOALIE.
        if (freedPosition == PlayerPosition.GOALIE) {
            if (currentPosition == PlayerPosition.GOALIE) {
                return PlayerPosition.GOALIE;
            }
            return null;
        }

        // Kandidát s ANY (nezáleží) – může obsadit libovolnou ne-brankářskou pozici.
        if (currentPosition == null || currentPosition == PlayerPosition.ANY) {
            return freedPosition;
        }

        // Stejná pozice = vždy povoleno.
        if (currentPosition == freedPosition) {
            return currentPosition;
        }

        boolean currentIsDefense = isDefensePosition(currentPosition);
        boolean freedIsDefense = isDefensePosition(freedPosition);
        boolean currentIsForward = isForwardPosition(currentPosition);
        boolean freedIsForward = isForwardPosition(freedPosition);

        boolean sameLine =
                (currentIsDefense && freedIsDefense) ||
                        (currentIsForward && freedIsForward);

        if (sameLine) {
            // Změna v rámci obrany nebo útoku je povolena vždy.
            return freedPosition;
        }

        boolean crossLine =
                (currentIsDefense && freedIsForward) ||
                        (currentIsForward && freedIsDefense);

        if (crossLine) {
            // Přechod mezi obranou a útokem pouze pokud to hráč povolil v nastavení.
            if (!canChangePosition) {
                return null;
            }
            return freedPosition;
        }

        return null;
    }

    /**
     * Určuje, zda daná pozice patří mezi obranné pozice.
     *
     * Metoda deleguje rozhodnutí do utilitní třídy PlayerPositionUtil.
     *
     * @param position pozice hráče
     * @return true, pokud se jedná o obrannou pozici, jinak false
     */
    private boolean isDefensePosition(PlayerPosition position) {
        return PlayerPositionUtil.isDefense(position);
    }

    /**
     * Určuje, zda daná pozice patří mezi útočné pozice.
     *
     * Metoda deleguje rozhodnutí do utilitní třídy PlayerPositionUtil.
     *
     * @param position pozice hráče
     * @return true, pokud se jedná o útočnou pozici, jinak false
     */
    private boolean isForwardPosition(PlayerPosition position) {
        return PlayerPositionUtil.isForward(position);
    }

    /**
     * Odesílá notifikaci danému hráči.
     *
     * Notifikace se odesílá pomocí NotificationService
     * a obsahuje zadaný typ a kontext.
     *
     * @param player hráč, kterému se má notifikace odeslat
     * @param type typ notifikace
     * @param context kontext notifikace, typicky registrace nebo jiná doménová data
     */
    private void notifyPlayer(PlayerEntity player, NotificationType type, Object context) {
        notificationService.notifyPlayer(player, type, context);
    }

    /**
     * Načítá zápas podle identifikátoru nebo vyhazuje výjimku při neexistenci.
     *
     * Metoda slouží k centralizaci načítání entity zápasu. Při nenalezení
     * zápasu se vyhazuje MatchNotFoundException.
     *
     * @param matchId identifikátor zápasu
     * @return nalezená entita zápasu
     */
    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Načítá hráče podle identifikátoru nebo vyhazuje výjimku při neexistenci.
     *
     * Metoda slouží k centralizaci načítání entity hráče. Při nenalezení
     * hráče se vyhazuje PlayerNotFoundException.
     *
     * @param playerId identifikátor hráče
     * @return nalezená entita hráče
     */
    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Načítá registraci podle identifikátorů hráče a zápasu.
     *
     * Metoda vrací registraci, pokud existuje, jinak null. Používá se
     * v situacích, kdy absence registrace není chybovým stavem.
     *
     * @param playerId identifikátor hráče
     * @param matchId identifikátor zápasu
     * @return nalezená registrace nebo null
     */
    private MatchRegistrationEntity getRegistrationOrNull(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);
    }

    /**
     * Načítá registraci podle identifikátorů hráče a zápasu nebo vyhazuje výjimku.
     *
     * Metoda se používá v situacích, kdy se existence registrace očekává.
     * Při nenalezení registrace se vyhazuje RegistrationNotFoundException.
     *
     * @param playerId identifikátor hráče
     * @param matchId identifikátor zápasu
     * @return nalezená registrace
     */
    private MatchRegistrationEntity getRegistrationOrThrow(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RegistrationNotFoundException(matchId, playerId));
    }

    /**
     * Ověřuje, zda je v zápase ještě volné globální místo pro stav REGISTERED.
     *
     * Metoda porovnává aktuální počet registrací ve stavu REGISTERED
     * s maximálním počtem hráčů definovaným u zápasu.
     *
     * @param match zápas, pro který se kapacita ověřuje
     * @return true, pokud je v zápase volné místo, jinak false
     */
    private boolean isSlotAvailable(MatchEntity match) {
        long registeredCount = registrationRepository
                .countByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);
        return registeredCount < match.getMaxPlayers();
    }

    /**
     * Odesílá SMS hráči k dané registraci.
     *
     * Metoda dohledá telefonní číslo v nastavení hráče nebo v jeho profilu,
     * ověří, že číslo existuje a není prázdné, a pokusí se odeslat SMS
     * pomocí SmsService. Případné chyby se zalogují.
     *
     * @param registration registrace, ke které se zpráva vztahuje
     * @param message text SMS zprávy
     */
    private void sendSms(MatchRegistrationEntity registration, String message) {
        if (registration == null || registration.getPlayer() == null) {
            return;
        }

        PlayerEntity player = registration.getPlayer();
        var settings = player.getSettings();

        String phone = null;
        if (settings != null && settings.getContactPhone() != null && !settings.getContactPhone().isBlank()) {
            phone = settings.getContactPhone();
        } else if (player.getPhoneNumber() != null && !player.getPhoneNumber().isBlank()) {
            phone = player.getPhoneNumber();
        }

        if (phone == null || phone.isBlank()) {
            log.debug("sendSms: hráč {} nemá žádné telefonní číslo – SMS se nepošle", player.getId());
            return;
        }

        try {
            smsService.sendSms(phone, message);
        } catch (Exception e) {
            log.error(
                    "Chyba při odesílání SMS pro registraci {}: {}",
                    registration.getId(),
                    e.getMessage(),
                    e
            );
        }
    }

    /**
     * Aktualizuje stav registrace a volitelně čas změny.
     *
     * Metoda nastaví nový stav, informaci o uživateli, který změnu provedl,
     * a případně i čas změny. Registrace se uloží a flushne do databáze.
     *
     * @param registration registrace, která se má aktualizovat
     * @param status nový stav registrace
     * @param updatedBy identifikátor nebo role, která změnu provedla
     * @param updateTimestamp příznak, zda se má aktualizovat čas změny
     * @return uložená registrace
     */
    private MatchRegistrationEntity updateRegistrationStatus(
            MatchRegistrationEntity registration,
            PlayerMatchStatus status,
            String updatedBy,
            boolean updateTimestamp
    ) {
        registration.setStatus(status);
        registration.setCreatedBy(updatedBy);
        if (updateTimestamp) {
            registration.setTimestamp(LocalDateTime.now());
        }
        return registrationRepository.saveAndFlush(registration);
    }

    /**
     * Určuje typ notifikace podle nového stavu registrace.
     *
     * Metoda mapuje stavy registrace na typy notifikací používané
     * v notifikačním systému. Pro neznámé stavy vrací null.
     *
     * @param newStatus nový stav registrace
     * @return typ notifikace nebo null, pokud se notifikace nemá posílat
     */
    private NotificationType resolveNotificationType(PlayerMatchStatus newStatus) {
        return switch (newStatus) {
            case REGISTERED -> NotificationType.MATCH_REGISTRATION_CREATED;
            case UNREGISTERED -> NotificationType.MATCH_REGISTRATION_CANCELED;
            case EXCUSED -> NotificationType.PLAYER_EXCUSED;
            case RESERVED -> NotificationType.MATCH_REGISTRATION_RESERVED;
            case NO_RESPONSE -> NotificationType.MATCH_REGISTRATION_NO_RESPONSE;
            case SUBSTITUTE -> NotificationType.MATCH_REGISTRATION_SUBSTITUTE;
            case NO_EXCUSED -> NotificationType.PLAYER_NO_EXCUSED;
            default -> null;
        };
    }

    /**
     * Vrací aktuální datum a čas.
     *
     * Metoda je oddělena kvůli případnému testování a možnosti
     * pozdějšího nahrazení mockem času.
     *
     * @return aktuální datum a čas
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Ověřuje, zda zápas patří do aktivní sezóny.
     *
     * Metoda se používá pro zajištění, že se registrace mění pouze
     * u zápasů v aktivní sezóně. Pokud zápas do aktivní sezóny nepatří,
     * je vyhozena výjimka InvalidSeasonStateException.
     *
     * @param match zápas, který se má ověřit
     */
    private void assertMatchInActiveSeason(MatchEntity match) {
        if (match.getSeason() == null || !match.getSeason().isActive()) {
            throw new InvalidSeasonStateException(
                    "BE - Registrace lze měnit pouze u zápasů v aktivní sezóně."
            );
        }
    }

    /**
     * Ověřuje, zda aktuální hráč může měnit registraci pro daný zápas.
     *
     * Kontrola se provádí pouze v případě, že je aktuálně přihlášen hráč.
     * Pokud čas překročí limit (30 minut po začátku zápasu), je vyhozena
     * výjimka InvalidPlayerStatusException.
     *
     * @param match zápas, pro který se právo na změnu ověřuje
     */
    private void assertPlayerCanModifyMatch(MatchEntity match) {
        if (!isCurrentUserPlayer()) {
            return;
        }

        if (!isMatchEditableForPlayer(match)) {
            throw new InvalidPlayerStatusException(
                    "BE - Hráč může měnit registraci pouze do 30 minut po začátku zápasu."
            );
        }
    }

    /**
     * Ověřuje, zda je zápas pro hráče ještě editovatelný.
     *
     * Zápas je považován za editovatelný do 30 minut po plánovaném
     * začátku. Po uplynutí této lhůty se registrace hráčem měnit nesmí.
     *
     * @param match zápas, který se má ověřit
     * @return true, pokud je zápas editovatelný, jinak false
     */
    private boolean isMatchEditableForPlayer(MatchEntity match) {
        LocalDateTime editLimit = match.getDateTime().plusMinutes(30);
        return now().isBefore(editLimit);
    }

    /**
     * Zjišťuje, zda je aktuálně přihlášený uživatel hráč.
     *
     * Metoda kontroluje, zda je v autentizačním kontextu přítomna role ROLE_PLAYER.
     *
     * @return true, pokud je aktuální uživatel hráč, jinak false
     */
    private boolean isCurrentUserPlayer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PLAYER".equals(a.getAuthority()));
    }

    /**
     * Ověřuje, zda je pro daný zápas, tým a pozici ještě volný slot
     * podle MatchModeLayoutUtil a aktuálně registrovaných hráčů.
     *
     * Metoda pracuje s celkovou kapacitou zápasu, rozdělenou na oba týmy,
     * a s konfigurací kapacity podle pozic a módů zápasů. Pro konkrétní
     * pozici a tým ověřuje, zda počet hráčů ve stavu REGISTERED nepřekračuje
     * definovanou kapacitu.
     *
     * Pokud není pozice nebo tým určen, kapacita se neomezuje.
     *
     * @param match zápas, pro který se kapacita ověřuje
     * @param team tým, v jehož rámci se pozice kontroluje
     * @param positionInMatch pozice v zápase, která se má ověřit
     * @return true, pokud je pro zadanou pozici a tým volný slot, jinak false
     */
    private boolean isPositionSlotAvailableForTeam(MatchEntity match,
                                                   Team team,
                                                   PlayerPosition positionInMatch) {

        // Pokud nemáme konkrétní pozici nebo tým, kapacitu neomezujeme.
        if (positionInMatch == null || positionInMatch == PlayerPosition.ANY || team == null) {
            return true;
        }

        Integer maxPlayers = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        // Není definovaná celková kapacita nebo mód zápasu – neomezujeme.
        if (maxPlayers == null || maxPlayers <= 0 || mode == null) {
            return true;
        }

        // Stejná logika jako v MatchPositionServiceImpl – maxPlayers je pro oba týmy.
        int slotsPerTeam = maxPlayers / 2;

        Map<PlayerPosition, Integer> perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        Integer positionCapacity = perTeamCapacity.get(positionInMatch);

        // Pro tuto pozici není definovaná kapacita – bere se jako neomezená.
        if (positionCapacity == null || positionCapacity <= 0) {
            return true;
        }

        // Spočítá se obsazenost této pozice v daném týmu mezi REGISTERED hráči.
        List<MatchRegistrationEntity> registered = registrationRepository
                .findByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);

        long occupied = registered.stream()
                .filter(r -> r.getTeam() == team)
                .map(MatchRegistrationEntity::getPositionInMatch)
                .filter(Objects::nonNull)
                .filter(pos -> pos != PlayerPosition.ANY)
                .filter(pos -> pos == positionInMatch)
                .count();

        return occupied < positionCapacity;
    }
}