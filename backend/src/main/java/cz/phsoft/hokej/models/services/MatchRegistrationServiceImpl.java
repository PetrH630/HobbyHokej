package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.models.mappers.PlayerMapper;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.notification.NotificationService;
import cz.phsoft.hokej.models.services.sms.SmsMessageBuilder;
import cz.phsoft.hokej.models.services.sms.SmsService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementace service pro správu registrací hráčů na zápasy.
 * <p>
 * Třída zajišťuje vytvoření a změny registrací, přepočítávání stavů
 * podle kapacity zápasu a poskytování přehledů registrací.
 * Současně spouští notifikace hráčům podle typu změny registrace.
 * <p>
 * Třída řeší business logiku registrací a stavových přechodů,
 * ale neřeší UI, autentizaci ani výběr aktuálního hráče.
 * Tyto oblasti náleží jiným vrstvám aplikace.
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
    private final SeasonService seasonService;
    private final CurrentSeasonService currentSeasonService;

    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            PlayerMapper playerMapper,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            NotificationService notificationService,
            SeasonService seasonService,
            CurrentSeasonService currentSeasonService
    ) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.playerMapper = playerMapper;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.notificationService = notificationService;
        this.seasonService = seasonService;
        this.currentSeasonService = currentSeasonService;
    }

    // ==========================================
    // HLAVNÍ METODA – UPSERT REGISTRACE HRÁČE
    // ==========================================

    /**
     * Vytvoří nebo aktualizuje registraci hráče na zápas.
     * <p>
     * Metoda načte zápas a hráče, ověří, že zápas patří do aktivní sezóny
     * a že aktuální uživatel může registraci upravovat. Poté podle obsahu
     * požadavku zvolí větev pro odhlášení, omluvu nebo registraci
     * a nastaví odpovídající stav registrace. Společné údaje se převezmou
     * z požadavku a registrace se uloží. Po odhlášení se stav registrací
     * přepočítá a podle výsledného statusu se odešlou notifikace.
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

        // Ověření, že zápas patří do aktuálně aktivní sezóny.
        // Zápisy do neaktivní sezóny nejsou povoleny.
        assertMatchInActiveSeason(match);

        // Ověření, zda aktuální uživatel smí měnit registraci
        // s ohledem na svou roli a čas zápasu.
        assertPlayerCanModifyMatch(match);

        MatchRegistrationEntity registration =
                getRegistrationOrNull(playerId, request.getMatchId());

        if (registration == null && !request.isUnregister()) {
            registration = new MatchRegistrationEntity();
            registration.setMatch(match);
            registration.setPlayer(player);
        }

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

        if (request.isUnregister()) {
            recalcStatusesForMatch(request.getMatchId());
        }

        NotificationType notificationType = resolveNotificationType(newStatus);
        if (notificationType != null) {
            notifyPlayer(player, notificationType, registration);
        }

        return matchRegistrationMapper.toDTO(registration);
    }

    /**
     * Zpracuje větev pro registraci typu REGISTER, RESERVED nebo SUBSTITUTE.
     * <p>
     * Metoda nepovolí opakovanou registraci již registrovaného hráče.
     * Při volbě SUBSTITUTE se hráč označí jako náhradník, který
     * neblokuje kapacitu a může později přejít na jiný stav.
     * Při běžné registraci se podle kapacity zápasu rozhodne,
     * zda se použije stav REGISTERED nebo RESERVED. Případná
     * předchozí omluva se odstraní.
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

        PlayerMatchStatus newStatus =
                isSlotAvailable(match) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;

        clearExcuseIfNeeded(registration);
        return newStatus;
    }

    /**
     * Zpracuje odhlášení hráče ze zápasu (UNREGISTER).
     * <p>
     * Odhlášení je povoleno pouze v případě, že registrace existuje
     * a má stav REGISTERED nebo RESERVED. Při úspěšném odhlášení
     * se nastaví informace o omluvě a stav UNREGISTERED.
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
     * Zpracuje omluvu hráče z účasti na zápase (EXCUSED).
     * <p>
     * Omluva je povolena pouze v případě, že hráč dosud nereagoval
     * nebo byl veden jako náhradník, nebo byl veden jako neomluvený.
     * Při úspěšné omluvě se nastaví důvod a poznámka a registrace
     * se přepne do stavu EXCUSED.
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
     * Nastaví registraci do stavu NO_EXCUSED na základě rozhodnutí administrátora.
     * <p>
     * Zápas musí být již odehraný a předchozí stav registrace musí být REGISTERED.
     * Případná omluva se odstraní a uloží se poznámka administrátora. Po změně
     * se odešle notifikace podle nastaveného typu.
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

    // TODO
    /**
     * Nastaví registraci do stavu EXCUSED na základě rozhodnutí administrátora/manažera
     * poté co byl původně uložen se statutem NO_EXCUSED.
     *
     * Zápas musí být již odehraný a předchozí stav registrace musí být NO_EXCUSED.
     * Případná poznámka administrátora se odstraní. Notifikace se neodesílá.
     */
    @Override
    @Transactional
    public MatchRegistrationDTO cancelNoExcused(Long matchId,
                                              Long playerId,
                                              ExcuseReason excuseReason,
                                              String excuseNote) {

        MatchEntity match = getMatchOrThrow(matchId);
        PlayerEntity player = getPlayerOrThrow(playerId);

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

        registration.setExcuseReason(ExcuseReason.JINE);
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
    // KONEC TODO

    /**
     * Nastaví do registrace společné údaje z požadavku.
     * <p>
     * Upravuje tým, administrátorskou poznámku a případné informace
     * o omluvě, pokud jsou v požadavku uvedeny.
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

    /*
     Společný helper pro přechody, kde se nemá zachovat omluva.
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
     * Vrátí všechny registrace pro daný zápas omezené na aktuální sezónu.
     * <p>
     * Pokud zápas nepatří do aktuálně vybrané sezóny, vrátí se prázdný seznam.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId) {
        MatchEntity match = getMatchOrThrow(matchId);

        if (!isMatchInCurrentSeason(match)) {
            return List.of();
        }

        return matchRegistrationMapper.toDTOList(
                registrationRepository.findByMatchId(matchId)
        );
    }

    /**
     * Vrátí všechny registrace pro zadané zápasy omezené na aktuální sezónu.
     * <p>
     * Pokud je seznam ID prázdný nebo null, vrátí se prázdný seznam.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return List.of();
        }

        List<MatchRegistrationEntity> regsInSeason = registrationRepository
                .findByMatchIdIn(matchIds).stream()
                .filter(this::isRegistrationInCurrentSeason)
                .toList();

        return matchRegistrationMapper.toDTOList(regsInSeason);
    }

    /**
     * Vrátí všechny registrace v systému omezené na aktuální sezónu.
     */
    @Override
    public List<MatchRegistrationDTO> getAllRegistrations() {
        List<MatchRegistrationEntity> regsInSeason = registrationRepository
                .findAll().stream()
                .filter(this::isRegistrationInCurrentSeason)
                .toList();

        return matchRegistrationMapper.toDTOList(regsInSeason);
    }

    /**
     * Vrátí všechny registrace zadaného hráče omezené na aktuální sezónu.
     */
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId) {
        List<MatchRegistrationEntity> regsInSeason = registrationRepository
                .findByPlayerId(playerId).stream()
                .filter(this::isRegistrationInCurrentSeason)
                .toList();

        return matchRegistrationMapper.toDTOList(regsInSeason);
    }

    /**
     * Vrátí hráče, kteří na daný zápas nijak nereagovali.
     * <p>
     * Registrace se berou bez ohledu na konkrétní stav.
     * Pokud zápas nepatří do aktuálně vybrané sezóny,
     * vrátí se prázdný seznam.
     */
    @Override
    public List<PlayerDTO> getNoResponsePlayers(Long matchId) {
        MatchEntity match = getMatchOrThrow(matchId);

        if (!isMatchInCurrentSeason(match)) {
            return List.of();
        }

        Set<Long> respondedIds = getRespondedPlayerIds(matchId);

        List<PlayerEntity> noResponsePlayers = playerRepository.findAll().stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .toList();

        return noResponsePlayers.stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    /**
     * Přepočítá statusy REGISTERED a RESERVED pro daný zápas.
     * <p>
     * Registrace se seřadí podle času vytvoření a prvním hráčům
     * do výše kapacity zápasu se nastaví stav REGISTERED.
     * Ostatním se nastaví stav RESERVED. Náhradníci se do
     * tohoto přepočtu nezahrnují.
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
     * Odešle SMS všem hráčům, kteří jsou na daný zápas registrováni
     * se statusem REGISTERED a mají povolené SMS notifikace.
     */
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

    // poznámka: sendNoResponseSmsForMatch je aktuálně zakomentovaný blok,
    // takže dokumentace k němu se neřeší; pokud ho znovu aktivuješ,
    // můžeme mu dopsat Javadoc ve stejném stylu.

    /**
     * Obecná administrátorská operace pro změnu statusu registrace.
     * <p>
     * Metoda neumožňuje nastavit status NO_EXCUSED, protože ten má
     * vlastní samostatnou logiku. Před změnou se ověří existence
     * zápasu, hráče i samotné registrace.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @param status   cílový status registrace (mimo NO_EXCUSED)
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


    // ====================================================
    // PRIVÁTNÍ HELPERY – NAČÍTÁNÍ ENTIT A ZÁKLADNÍ LOGIKA
    // ====================================================

    // TODO - možná změnit Object context na MatchRegistrationEntity entity
    private void notifyPlayer(PlayerEntity player, NotificationType type, Object context) {
        notificationService.notifyPlayer(player, type, context);
    }

    /**
     * Vrátí množinu ID hráčů, kteří mají k zápasu nějakou registraci.
     */
    private Set<Long> getRespondedPlayerIds(Long matchId) {
        return registrationRepository.findByMatchId(matchId).stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Najde zápas podle ID nebo vyhodí MatchNotFoundException.
     */
    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Najde hráče podle ID nebo vyhodí PlayerNotFoundException.
     */
    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Vrátí registraci hráče na zápas, pokud existuje, jinak null.
     */
    private MatchRegistrationEntity getRegistrationOrNull(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);
    }

    /**
     * Vrátí registraci hráče na zápas nebo vyhodí RegistrationNotFoundException.
     */
    private MatchRegistrationEntity getRegistrationOrThrow(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RegistrationNotFoundException(matchId, playerId));
    }

    /**
     * Zjistí, zda je ve zápase ještě volné místo pro stav REGISTERED.
     */
    private boolean isSlotAvailable(MatchEntity match) {
        long registeredCount = registrationRepository
                .countByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);
        return registeredCount < match.getMaxPlayers();
    }

    // TODO - ŘEŠENO NOTIFIKACEMI - ASI SMAZAT

    /**
     * Bezpečně odešle SMS hráči z registrace.
     * <p>
     * Pokud je registrace nebo hráč null, metoda nic neprovede.
     * Chyby při odesílání jsou pouze zalogovány.
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
     * Společná metoda pro změnu statusu registrace.
     * <p>
     * Nastaví nový status, případně aktualizuje čas změny
     * a informaci o tom, kdo změnu provedl, a registraci uloží.
     *
     * @param registration    registrace ke změně
     * @param status          nový status
     * @param updatedBy       identifikace původce změny (user, admin, system)
     * @param updateTimestamp příznak, zda má být přepsán čas na aktuální
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
     * Převede status registrace na odpovídající typ notifikace.
     * <p>
     * Pokud se pro daný status notifikace neposílá, vrací se null.
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
     * Pomocná metoda pro získání aktuálního času.
     * <p>
     * Oddělení do samostatné metody usnadňuje testování.
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Ověří, že zápas patří do aktivní sezóny.
     * <p>
     * Metoda se používá před zápisem registrace, aby se zabránilo
     * změnám v neaktivních sezónách.
     */
    private void assertMatchInActiveSeason(MatchEntity match) {
        if (match.getSeason() == null || !match.getSeason().isActive()) {
            throw new InvalidSeasonStateException(
                    "BE - Registrace lze měnit pouze u zápasů v aktivní sezóně."
            );
        }
    }

    /**
     * Ověří, zda aktuálně přihlášený uživatel může měnit registraci na daný zápas.
     * <p>
     * Uživatel s rolí ADMIN nebo MANAGER není časově omezen.
     * Uživatel s rolí PLAYER může registraci měnit pouze
     * v definovaném časovém okně kolem začátku zápasu.
     */
    private void assertPlayerCanModifyMatch(MatchEntity match) {
        if (!isCurrentUserPlayer()) {
            return;
        }

        if (!isMatchEditableForPlayer(match)) {
            throw new InvalidPlayerStatusException(
                    "BE - Jako hráč můžeš měnit registraci pouze do 30 minut po začátku zápasu."
            );
        }
    }

    /**
     * Zjistí, zda je zápas ještě v časovém okně, ve kterém může hráč měnit registraci.
     * <p>
     * Zápas je považován za editovatelný pro hráče do třiceti minut
     * po začátku zápasu. Po uplynutí této doby už změna registrace není povolena.
     *
     * @param match zápas, ke kterému se vztahuje registrace
     * @return true, pokud je změna registrace ještě povolena
     */
    private boolean isMatchEditableForPlayer(MatchEntity match) {
        LocalDateTime editLimit = match.getDateTime().plusMinutes(30);
        return now().isBefore(editLimit);
    }

    /**
     * Zjistí, zda má aktuálně přihlášený uživatel roli ROLE_PLAYER.
     * <p>
     * Metoda se používá pro odlišení časového omezení
     * oproti administrátorům nebo manažerům.
     *
     * @return true, pokud má uživatel roli PLAYER, jinak false
     */
    private boolean isCurrentUserPlayer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PLAYER".equals(a.getAuthority()));
    }

    // ======================================
    // SEASON – POMOCNÉ METODY PRO CURRENT SEASON
    // ======================================

    /**
     * Zjistí, zda daný zápas patří do aktuálně vybrané sezóny.
     * <p>
     * Porovnává se identifikátor sezóny zápasu s identifikátorem,
     * který vrací metoda getCurrentSeasonIdOrActive.
     */
    private boolean isMatchInCurrentSeason(MatchEntity match) {
        if (match == null || match.getSeason() == null) {
            return false;
        }
        Long seasonId = getCurrentSeasonIdOrActive();
        return seasonId.equals(match.getSeason().getId());
    }

    /**
     * Zjistí, zda registrace patří k zápasu v aktuálně vybrané sezóně.
     */
    private boolean isRegistrationInCurrentSeason(MatchRegistrationEntity registration) {
        if (registration == null) {
            return false;
        }
        return isMatchInCurrentSeason(registration.getMatch());
    }

    /**
     * Vrátí identifikátor sezóny, která se má použít pro filtrování registrací.
     * <p>
     * Nejprve se použije sezóna uložená v CurrentSeasonService.
     * Pokud není k dispozici, použije se globálně aktivní sezóna
     * ze SeasonService.
     *
     * @return ID aktuální nebo aktivní sezóny
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

}
