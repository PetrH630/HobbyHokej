package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.sms.SmsMessageBuilder;
import cz.phsoft.hokej.models.services.sms.SmsService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service pro správu registrací hráčů na zápasy.
 * <p>
 * Odpovědnosti:
 * <ul>
 *     <li>vytváření a změna registrací (REGISTER, RESERVED, UNREGISTERED, EXCUSED, NO_EXCUSED),</li>
 *     <li>přepočet pořadí REGISTERED/RESERVED podle kapacity zápasu,</li>
 *     <li>získávání přehledů o registracích (pro zápas, hráče, NO_RESPONSE hráče),</li>
 *     <li>spouštění notifikací (email/SMS) podle změny statusu.</li>
 * </ul>
 * <p>
 * Tato service:
 * <ul>
 *     <li>řeší byznys logiku registrací a stavových přechodů,</li>
 *     <li>neřeší UI, security ani výběr aktuálního hráče (to řeší jiné vrstvy).</li>
 * </ul>
 */
@Service
public class MatchRegistrationServiceImpl implements MatchRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(MatchRegistrationServiceImpl.class);

    private final MatchRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchRegistrationMapper matchRegistrationMapper;
    private final PlayerMapper playerMapper;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;
    private final NotificationService notificationService;

    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            PlayerMapper playerMapper,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            NotificationService notificationService
    ) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.playerMapper = playerMapper;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.notificationService = notificationService;
    }

    // ==========================================
    // HLAVNÍ METODA – UPSERT REGISTRACE HRÁČE
    // ==========================================

    /**
     * Vytvoří nebo aktualizuje registraci hráče na zápas.
     * <p>
     * Postup:
     * <ol>
     *     <li>načte zápas a hráče,</li>
     *     <li>najde existující registraci (pokud existuje),</li>
     *     <li>podle obsahu {@link MatchRegistrationRequest} větví na:
     *         <ul>
     *             <li>UNREGISTER → {@link #handleUnregister},</li>
     *             <li>EXCUSE → {@link #handleExcuse},</li>
     *             <li>REGISTER/RESERVE → {@link #handleRegisterOrReserve},</li>
     *         </ul>
     *     </li>
     *     <li>aplikuje společné detaily z requestu ({@link #applyRequestDetails}),</li>
     *     <li>uloží registraci,</li>
     *     <li>po UNREGISTER přepočítá pořadí REGISTERED/RESERVED,</li>
     *     <li>podle výsledného statusu spustí notifikace.</li>
     * </ol>
     *
     * @param playerId ID hráče
     * @param request  požadavek na změnu registrace
     * @return DTO výsledné registrace
     */
    @Transactional
    @Override
    public MatchRegistrationDTO upsertRegistration(Long playerId, MatchRegistrationRequest request) {

        MatchEntity match = getMatchOrThrow(request.getMatchId());
        PlayerEntity player = getPlayerOrThrow(playerId);

        MatchRegistrationEntity registration =
                getRegistrationOrNull(playerId, request.getMatchId());

        if (registration == null && !request.isUnregister()) {
            registration = new MatchRegistrationEntity();
            registration.setMatch(match);
            registration.setPlayer(player);
        }


        PlayerMatchStatus newStatus;

        if (request.isUnregister()) {
            // UNREGISTER – pouze z REGISTERED/RESERVED
            newStatus = handleUnregister(request, playerId, registration);
        } else if (request.getExcuseReason() != null) {
            // EXCUSE – pouze pokud ještě nemá žádnou registraci
            newStatus = handleExcuse(request, match, player, registration);
        } else {
            // REGISTER / RESERVE
            newStatus = handleRegisterOrReserve(request, match, player, registration);
        }

        // společné nastavení detailů z requestu (team, admin poznámka, excuse...)
        applyRequestDetails(registration, request);

        // finální nastavení – status, timestamp, kdo vytvořil
        registration.setStatus(newStatus);
        registration.setTimestamp(now());
        registration.setCreatedBy("user");

        registration = registrationRepository.save(registration);

        // po UNREGISTER přepočítáme REGISTERED/RESERVED (náhradníky)
        if (request.isUnregister()) {
            recalcStatusesForMatch(request.getMatchId());
        }

        // notifikace (email/SMS) podle typu změny
        NotificationType notificationType = resolveNotificationType(newStatus);
        if (notificationType != null) {
            notificationService.notifyPlayer(player, notificationType, registration);
        }

        return matchRegistrationMapper.toDTO(registration);
    }

    /**
     * Větev pro UNREGISTER:
     * <ul>
     *     <li>povoleno pouze, pokud registrace/rezervace existuje a je REGISTERED/RESERVED,</li>
     *     <li>nastaví excuseReason a excuseNote,</li>
     *     <li>vrací nový status UNREGISTERED.</li>
     * </ul>
     */
    private PlayerMatchStatus handleUnregister(
            MatchRegistrationRequest request,
            Long playerId,
            MatchRegistrationEntity registration
    ) {
        boolean isAllowedUnregisterStatus =
                registration != null &&
                        (registration.getStatus() == PlayerMatchStatus.REGISTERED
                                || registration.getStatus() == PlayerMatchStatus.RESERVED);

        if (!isAllowedUnregisterStatus) {
            throw new RegistrationNotFoundException(request.getMatchId(), playerId);
        }

        registration.setExcuseReason(request.getExcuseReason());
        registration.setExcuseNote(request.getExcuseNote());

        return PlayerMatchStatus.UNREGISTERED;
    }

    /**
     * Větev pro EXCUSE (omluva z účasti):
     * <ul>
     *     <li>nelze, pokud má hráč aktuálně REGISTERED,</li>
     *     <li>pokud registrace neexistuje, vytvoří novou,</li>
     *     <li>nastaví excuseReason a excuseNote,</li>
     *     <li>vrací status EXCUSED.</li>
     * </ul>
     */
    private PlayerMatchStatus handleExcuse(
            MatchRegistrationRequest request,
            MatchEntity match,
            PlayerEntity player,
            MatchRegistrationEntity registration
    ) {
        // NO_RESPONSE = registrace bez statusu
        boolean isNoResponse = (registration == null || registration.getStatus() == null);

        if (!isNoResponse) {
            throw new DuplicateRegistrationException(
                    request.getMatchId(),
                    player.getId(),
                    "BE - Omluva je možná pouze pokud hráč dosud nereagoval na zápas."
            );
        }
        registration.setExcuseReason(request.getExcuseReason());
        registration.setExcuseNote(request.getExcuseNote());

        return PlayerMatchStatus.EXCUSED;
    }

    /**
     * Větev pro REGISTER / RESERVE:
     * <ul>
     *     <li>pokud je hráč už REGISTERED, další registrace není povolena,</li>
     *     <li>pokud je volné místo → REGISTERED, jinak RESERVED,</li>
     *     <li>při přechodu z EXCUSED smaže excuseReason / excuseNote.</li>
     * </ul>
     */
    private PlayerMatchStatus handleRegisterOrReserve(
            MatchRegistrationRequest request,
            MatchEntity match,
            PlayerEntity player,
            MatchRegistrationEntity registration
    ) {
        boolean isAlreadyRegistered =
                registration != null &&
                        registration.getStatus() == PlayerMatchStatus.REGISTERED;

        if (isAlreadyRegistered) {
            throw new DuplicateRegistrationException(request.getMatchId(), player.getId());
        }

        PlayerMatchStatus newStatus =
                isSlotAvailable(match) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;


        // Registrace je v této větvi vždy nenull (viz upsertRegistration).
        // Pokud přecházíme z EXCUSED, smažeme omluvu:
        if (registration.getExcuseReason() != null || registration.getExcuseNote() != null) {
            registration.setExcuseReason(null);
            registration.setExcuseNote(null);
        }


        return newStatus;
    }

    /**
     * Společné nastavení detailů registrace podle requestu:
     * <ul>
     *     <li>team (světlý/tmavý),</li>
     *     <li>adminNote,</li>
     *     <li>případná aktualizace excuseReason.</li>
     *     <li>případná aktualizace excuseReason.</li>
     * </ul>
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
    }

    // =========================
    // FETCH – ČTECÍ METODY
    // =========================

    /**
     * Vrátí všechny registrace pro daný zápas.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId) {
        return matchRegistrationMapper.toDTOList(
                registrationRepository.findByMatchId(matchId)
        );
    }

    /**
     * Vrátí všechny registrace pro zadané zápasy.
     * <p>
     * Pokud je seznam ID prázdný nebo null, vrací prázdný seznam.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return List.of();
        }

        return matchRegistrationMapper.toDTOList(
                registrationRepository.findByMatchIdIn(matchIds)
        );
    }

    /**
     * Vrátí všechny registrace v systému.
     */
    @Override
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationMapper.toDTOList(registrationRepository.findAll());
    }

    /**
     * Vrátí všechny registrace konkrétního hráče.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId) {
        return matchRegistrationMapper.toDTOList(
                registrationRepository.findByPlayerId(playerId)
        );
    }

    /**
     * Vrátí hráče, kteří na daný zápas nijak nereagovali
     * (nemají žádnou registraci bez ohledu na status).
     */
    @Override
    public List<PlayerDTO> getNoResponsePlayers(Long matchId) {
        Set<Long> respondedIds = getRespondedPlayerIds(matchId);

        List<PlayerEntity> noResponsePlayers = playerRepository.findAll().stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .toList();

        return noResponsePlayers.stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    /**
     * Vrátí množinu ID hráčů, kteří mají k zápasu nějakou registraci.
     */
    private Set<Long> getRespondedPlayerIds(Long matchId) {
        return registrationRepository.findByMatchId(matchId).stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());
    }

    // =========================
    // RECALC – PŘEPOČET POŘADÍ
    // =========================

    /**
     * Přepočítá statusy REGISTERED/RESERVED pro daný zápas.
     * <p>
     * Postup:
     * <ul>
     *     <li>vybere registrace se statusem REGISTERED/RESERVED,</li>
     *     <li>seřadí je podle timestampu,</li>
     *     <li>prvních maxPlayers nastaví na REGISTERED, ostatní na RESERVED.</li>
     * </ul>
     * <p>
     * Příklad:
     * <pre>
     * maxPlayers = 10
     * 15 hráčů má status REGISTERED/RESERVED
     * → prvních 10 = REGISTERED, zbylých 5 = RESERVED
     * </pre>
     */
    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {
        MatchEntity match = getMatchOrThrow(matchId);
        int maxPlayers = match.getMaxPlayers();

        List<MatchRegistrationEntity> regs = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        for (int i = 0; i < regs.size(); i++) {
            MatchRegistrationEntity reg = regs.get(i);
            PlayerMatchStatus newStatus =
                    (i < maxPlayers) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;

            if (reg.getStatus() != newStatus) {
                updateRegistrationStatus(reg, newStatus, "system", false);
            }
        }
    }

    // =========================
    // SMS – HROMADNÉ ODESÍLÁNÍ
    // =========================

    /**
     * Odešle SMS všem hráčům, kteří jsou REGISTERED na daný zápas
     * a mají povolené SMS notifikace.
     */
    @Transactional
    public void sendSmsToRegisteredPlayers(Long matchId) {
        registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .forEach(r -> {
                    var player = r.getPlayer();
                    var ns = player.getNotificationSettings();
                    if (ns != null && ns.isSmsEnabled()) {
                        sendSms(r, smsMessageBuilder.buildMessageFinal(r));
                    }
                });
    }

    /**
     * Odešle připomínkovou SMS všem hráčům, kteří na zápas nijak nereagovali
     * (NO_RESPONSE) a mají povolené SMS notifikace.
     */
    public void sendNoResponseSmsForMatch(Long matchId) {
        var match = getMatchOrThrow(matchId);

        getNoResponsePlayers(matchId).forEach(player -> {
            String smsMsg = smsMessageBuilder.buildMessageNoResponse(player, match);

            try {
                if (player.isNotifyBySms()) {
                    smsService.sendSms(player.getPhoneNumber(), smsMsg);
                }
            } catch (Exception e) {
                log.error(
                        "Chyba při odesílání SMS hráči {} ({}) pro zápas {}: {}",
                        player.getFullName(),
                        player.getPhoneNumber(),
                        matchId,
                        e.getMessage(),
                        e
                );
            }
        });
    }

    // ================================================
    // ADMIN – RUČNÍ ZMĚNA STATUSU / NO_EXCUSED LOGIKA
    // ================================================

    /**
     * Obecná admin operace pro změnu statusu registrace hráče na zápas.
     * <p>
     * Nepovoluje nastavení statusu NO_EXCUSED – ten má vlastní logiku.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @param status   cílový status (mimo NO_EXCUSED)
     * @return DTO aktualizované registrace
     */
    @Override
    @Transactional
    public MatchRegistrationDTO updateStatus(Long matchId, Long playerId, PlayerMatchStatus status) {

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

    /**
     * Speciální admin logika pro nastavení statusu NO_EXCUSED:
     * <ul>
     *     <li>zápas musí být v minulosti,</li>
     *     <li>původní status musí být REGISTERED,</li>
     *     <li>smaže se excuseReason a excuseNote,</li>
     *     <li>nastaví se adminNote (z parametru nebo defaultní).</li>
     * </ul>
     */
    @Override
    @Transactional
    public MatchRegistrationDTO markNoExcused(Long matchId,
                                              Long playerId,
                                              String adminNote) {

        MatchEntity match = getMatchOrThrow(matchId);
        getPlayerOrThrow(playerId);

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

        return matchRegistrationMapper.toDTO(updated);
    }

    // ====================================================
    // PRIVÁTNÍ HELPERY – NAČÍTÁNÍ ENTIT A ZÁKLADNÍ LOGIKA
    // ====================================================

    /**
     * Najde zápas podle ID nebo vyhodí {@link MatchNotFoundException}.
     */
    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Najde hráče podle ID nebo vyhodí {@link PlayerNotFoundException}.
     */
    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Vrátí registraci hráče na zápas, pokud existuje, jinak {@code null}.
     */
    private MatchRegistrationEntity getRegistrationOrNull(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);
    }

    /**
     * Vrátí registraci hráče na zápas, pokud existuje,
     * jinak vyhodí {@link RegistrationNotFoundException}.
     */
    private MatchRegistrationEntity getRegistrationOrThrow(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RegistrationNotFoundException(matchId, playerId));
    }

    /**
     * Zjistí, zda je ve zápase ještě volné místo pro status REGISTERED.
     */
    private boolean isSlotAvailable(MatchEntity match) {
        long registeredCount = registrationRepository
                .countByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);
        return registeredCount < match.getMaxPlayers();
    }

    /**
     * Bezpečně odešle SMS hráči z registrace.
     * <p>
     * Pokud je registrace nebo hráč null, nic neudělá.
     * Chyby při odesílání pouze zaloguje.
     */
    private void sendSms(MatchRegistrationEntity registration, String message) {
        if (registration == null || registration.getPlayer() == null) {
            return;
        }

        try {
            smsService.sendSms(registration.getPlayer().getPhoneNumber(), message);
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
     * Společná metoda pro změnu statusu registrace.
     *
     * @param registration    registrace ke změně
     * @param status          nový status
     * @param updatedBy       kdo změnu provedl (user/admin/system)
     * @param updateTimestamp zda přepsat timestamp na aktuální čas
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
     * Převede výsledný {@link PlayerMatchStatus} na typ notifikace.
     *
     * @return odpovídající {@link NotificationType}, nebo {@code null},
     * pokud se pro daný status notifikace neposílá
     */
    private NotificationType resolveNotificationType(PlayerMatchStatus newStatus) {
        return switch (newStatus) {
            case REGISTERED -> NotificationType.PLAYER_REGISTERED;
            case UNREGISTERED -> NotificationType.PLAYER_UNREGISTERED;
            case EXCUSED -> NotificationType.PLAYER_EXCUSED;
            case RESERVED -> NotificationType.PLAYER_RESERVED;
            default -> null;
        };
    }

    /**
     * Pomocná metoda pro získání aktuálního času.
     * <p>
     * Odděleno kvůli lepší testovatelnosti.
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}
