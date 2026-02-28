package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.match.exceptions.InvalidMatchDateTimeException;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.player.exceptions.InvalidPlayerStatusException;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.dto.MatchRegistrationRequest;
import cz.phsoft.hokej.registration.exceptions.DuplicateRegistrationException;
import cz.phsoft.hokej.registration.exceptions.RegistrationNotFoundException;
import cz.phsoft.hokej.registration.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.notifications.services.NotificationService;
import cz.phsoft.hokej.notifications.sms.SmsMessageBuilder;
import cz.phsoft.hokej.notifications.sms.SmsService;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.util.PlayerPositionUtil;
import cz.phsoft.hokej.season.exceptions.InvalidSeasonStateException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import java.util.Map;
import java.util.Objects;

/**
 * Implementace příkazové service vrstvy pro správu registrací hráčů na zápasy.
 *
 * Tato třída zajišťuje veškeré změny stavů registrací, změny týmů a pozic,
 * přepočet kapacity zápasu při změně počtu hráčů a hromadné odesílání SMS.
 *
 * Třída obsahuje business logiku registrací a souvisejících pravidel.
 * Neřeší čtecí operace nad registracemi, které zůstávají v {@link MatchRegistrationService}.
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

    public MatchRegistrationCommandServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            NotificationService notificationService
    ) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.notificationService = notificationService;
    }

    // ==========================================
    // HLAVNÍ METODA – UPSERT REGISTRACE HRÁČE
    // ==========================================

    /**
     * {@inheritDoc}
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

    // ==========================================
    // OMLUVY A NO_EXCUSED
    // ==========================================

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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

    // ==========================================
    // ZMĚNY TÝMU A POZICE
    // ==========================================

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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

    // ==========================================
    // PŘEPOČET KAPACITY
    // ==========================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {
        MatchEntity match = getMatchOrThrow(matchId);
        Integer maxPlayersObj = match.getMaxPlayers();

        if (maxPlayersObj == null || maxPlayersObj <= 0) {
            return;
        }

        int maxPlayers = maxPlayersObj;

        // Kolik slotů se rezervuje pro brankáře v rámci maxPlayers.
        int goalieSlots = getGoalieSlotsForMatch(match);
        if (goalieSlots < 0) {
            goalieSlots = 0;
        }
        if (goalieSlots > maxPlayers) {
            goalieSlots = maxPlayers;
        }

        int skaterCapacity = maxPlayers - goalieSlots;

        // Všichni aktuálně REGISTERED, seřazení podle času registrace.
        List<MatchRegistrationEntity> allRegistered = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        if (allRegistered.isEmpty()) {
            return;
        }

        // Rozdělení na brankáře a hráče v poli.
        List<MatchRegistrationEntity> goalies = allRegistered.stream()
                .filter(this::isGoalieRegistration)
                .toList();

        List<MatchRegistrationEntity> skaters = allRegistered.stream()
                .filter(r -> !isGoalieRegistration(r))
                .toList();

        int registeredGoalies = goalies.size();
        int registeredSkaters = skaters.size();

        //  PŮVODNÍ BRZDA – ODSTRANĚNO:
        // if (registeredGoalies + registeredSkaters <= maxPlayers
        //         && registeredSkaters <= skaterCapacity) {
        //     return;
        // }

        // 1) Brankáři – ponechají se REGISTERED pouze do výše goalieSlots,
        //    případní další brankáři se přesunou do RESERVED.
        int allowedGoalies = Math.min(goalieSlots, maxPlayers);
        int keptGoalies = 0;

        for (MatchRegistrationEntity goalieReg : goalies) {
            if (keptGoalies < allowedGoalies) {
                updateRegistrationStatus(goalieReg, PlayerMatchStatus.REGISTERED, "system", false);
                keptGoalies++;
            } else {
                updateRegistrationStatus(goalieReg, PlayerMatchStatus.RESERVED, "system", false);
            }
        }

        // 2) Hráči v poli – přepočet jen nad skaters s kapacitou skaterCapacity.
        //  PŮVODNÍ BRZDA – ODSTRANĚNO:
        // if (registeredSkaters <= skaterCapacity) {
        //     return;
        // }

        if (registeredSkaters <= 0) {
            return;
        }

        int desiredTotal = skaterCapacity;

        // Aktuální rozložení hráčů v poli – využívá se při rozhodování,
        // který tým dostane případné "liché" extra místo.
        long currentDark = skaters.stream()
                .filter(r -> r.getTeam() == Team.DARK)
                .count();
        long currentLight = skaters.stream()
                .filter(r -> r.getTeam() == Team.LIGHT)
                .count();

        int targetDark;
        int targetLight;

        if (desiredTotal % 2 == 0) {
            // Rovnoměrné rozdělení.
            targetDark = desiredTotal / 2;
            targetLight = desiredTotal / 2;
        } else {
            // Lichý počet – jeden tým má o 1 více, upřednostní se aktuálně větší tým.
            if (currentDark >= currentLight) {
                targetDark = desiredTotal / 2 + 1;
                targetLight = desiredTotal - targetDark;
            } else {
                targetLight = desiredTotal / 2 + 1;
                targetDark = desiredTotal - targetLight;
            }
        }

        int dark = 0;
        int light = 0;

        for (MatchRegistrationEntity reg : skaters) {
            Team team = reg.getTeam();
            boolean movable = canAutoMoveTeam(reg); // Využití PlayerSettings.isPossibleMoveToAnotherTeam().

            // Hráč bez přiřazeného týmu – přiřadí se tam, kde je volno.
            if (team == null) {
                if (dark < targetDark) {
                    reg.setTeam(Team.DARK);
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                    dark++;
                } else if (light < targetLight) {
                    reg.setTeam(Team.LIGHT);
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                    light++;
                } else {
                    updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED, "system", false);
                }
                continue;
            }

            if (team == Team.DARK) {
                if (!movable) {
                    if (dark < targetDark) {
                        updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                        dark++;
                    } else {
                        updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED, "system", false);
                    }
                } else {
                    if (dark < targetDark) {
                        updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                        dark++;
                    } else if (light < targetLight) {
                        reg.setTeam(Team.LIGHT);
                        updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                        light++;
                    } else {
                        updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED, "system", false);
                    }
                }
            } else if (team == Team.LIGHT) {
                if (!movable) {
                    if (light < targetLight) {
                        updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                        light++;
                    } else {
                        updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED, "system", false);
                    }
                } else {
                    if (light < targetLight) {
                        updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                        light++;
                    } else if (dark < targetDark) {
                        reg.setTeam(Team.DARK);
                        updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED, "system", false);
                        dark++;
                    } else {
                        updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED, "system", false);
                    }
                }
            } else {
                // Neznámý tým – konzervativně do RESERVED.
                updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED, "system", false);
            }
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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

        // Pokud je konkrétní pozice pro daný tým plná → uložíme registraci jako RESERVED.
        // Pozice se normálně uloží v applyRequestDetails(...) – chceme ji mít, aby
        // hráč "stál ve frontě" právě na tuto pozici.
        return positionAvailable ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;
    }

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

    private void clearExcuseIfNeeded(MatchRegistrationEntity registration) {
        if (registration == null) {
            return;
        }
        if (registration.getExcuseReason() != null || registration.getExcuseNote() != null) {
            registration.setExcuseReason(null);
            registration.setExcuseNote(null);
        }
    }

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

    private boolean tryPromoteCandidateToFreedSlot(MatchRegistrationEntity candidate,
                                                   Team freedTeam,
                                                   PlayerPosition freedPosition) {

        if (candidate == null || candidate.getPlayer() == null) {
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

    private boolean isGoalieRegistration(MatchRegistrationEntity registration) {
        if (registration == null || registration.getPlayer() == null) {
            return false;
        }

        PlayerPosition position = registration.getPositionInMatch();
        if (position == null) {
            position = registration.getPlayer().getPrimaryPosition();
        }

        return position == PlayerPosition.GOALIE;
    }

    private int getGoalieSlotsForMatch(MatchEntity match) {
        MatchMode mode = match.getMatchMode();
        if (mode == null) {
            return 0;
        }
        return switch (mode) {
            case THREE_ON_THREE_WITH_GOALIE -> 2;
            case FOUR_ON_FOUR_WITH_GOALIE -> 2;
            case FIVE_ON_FIVE_WITH_GOALIE -> 2;
            default -> 0;
        };
    }

    private boolean isDefensePosition(PlayerPosition position) {
        return PlayerPositionUtil.isDefense(position);
    }

    private boolean isForwardPosition(PlayerPosition position) {
        return PlayerPositionUtil.isForward(position);
    }

    private boolean canAutoMoveTeam(MatchRegistrationEntity registration) {
        if (registration == null || registration.getPlayer() == null) {
            return false;
        }

        PlayerEntity player = registration.getPlayer();
        var settings = player.getSettings();
        if (settings == null) {
            return false;
        }

        return settings.isPossibleMoveToAnotherTeam();
    }

    private void notifyPlayer(PlayerEntity player, NotificationType type, Object context) {
        notificationService.notifyPlayer(player, type, context);
    }

    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    private MatchRegistrationEntity getRegistrationOrNull(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);
    }

    private MatchRegistrationEntity getRegistrationOrThrow(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RegistrationNotFoundException(matchId, playerId));
    }

    private boolean isSlotAvailable(MatchEntity match) {
        long registeredCount = registrationRepository
                .countByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);
        return registeredCount < match.getMaxPlayers();
    }

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

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private void assertMatchInActiveSeason(MatchEntity match) {
        if (match.getSeason() == null || !match.getSeason().isActive()) {
            throw new InvalidSeasonStateException(
                    "BE - Registrace lze měnit pouze u zápasů v aktivní sezóně."
            );
        }
    }

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

    private boolean isMatchEditableForPlayer(MatchEntity match) {
        LocalDateTime editLimit = match.getDateTime().plusMinutes(30);
        return now().isBefore(editLimit);
    }

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
     * podle MatchModeLayoutUtil a aktuálně REGISTERED hráčů.
     *
     * Nepracuje s žádnými dalšími službami, používá pouze:
     * - MatchEntity (matchMode, maxPlayers),
     * - MatchModeLayoutUtil,
     * - MatchRegistrationRepository.
     *
     * Kontroluje se:
     * - jen pro konkrétní pozice (null/ANY = bez omezení),
     * - jen pro stav REGISTERED (RESERVED pozice neblokují kapacitu).
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

        // Pro tuto pozici není definovaná kapacita – bereme jako neomezenou.
        if (positionCapacity == null || positionCapacity <= 0) {
            return true;
        }

        // Spočítáme obsazenost této pozice v daném týmu mezi REGISTERED hráči.
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