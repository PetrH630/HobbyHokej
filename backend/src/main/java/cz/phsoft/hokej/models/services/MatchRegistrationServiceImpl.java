package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.*;
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
 * Implementace service vrstvy používané pro správu registrací hráčů na zápasy.
 *
 * V této třídě se vytvářejí a aktualizují registrace, vyhodnocují se stavové přechody
 * podle kapacity zápasu a poskytují se přehledy registrací pro zápas, hráče nebo sadu zápasů.
 *
 * Součástí odpovědnosti je také spouštění notifikací hráčům podle typu změny registrace
 * a v případě potřeby odesílání SMS zpráv registrovaným hráčům.
 *
 * Třída obsahuje business logiku registrací a souvisejících pravidel. Neřeší prezentaci,
 * UI logiku ani výběr aktuálního hráče, které náleží jiným vrstvám aplikace.
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
     * Vytváří nebo aktualizuje registraci hráče na zápas.
     *
     * Nejprve se načte zápas a hráč a ověří se, že zápas patří do aktivní sezóny.
     * Následně se vyhodnotí, zda aktuálně přihlášený uživatel může registraci upravovat
     * s ohledem na roli a čas zápasu.
     *
     * Podle obsahu požadavku se zvolí větev pro odhlášení, omluvu nebo registraci
     * a určí se cílový stav registrace. Společné údaje se převezmou z požadavku,
     * registrace se uloží a při odhlášení ze stavu REGISTERED se pokusí povýšit
     * nejvhodnější hráč ze stavu RESERVED do stavu REGISTERED.
     *
     * Po uložení se podle výsledného stavu odešlou notifikace.
     *
     * @param playerId Identifikátor hráče, jehož registrace se upravuje.
     * @param request  Požadavek obsahující parametry změny registrace.
     * @return DTO uložené registrace se stavem odpovídajícím vyhodnoceným pravidlům.
     */
    @Transactional
    @Override
    public MatchRegistrationDTO upsertRegistration(Long playerId, MatchRegistrationRequest request) {

        MatchEntity match = getMatchOrThrow(request.getMatchId());
        PlayerEntity player = getPlayerOrThrow(playerId);

        // Ověření, že zápas patří do aktuálně aktivní sezóny.
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

        // Po odhlášení ze stavu REGISTERED se pokusí najít a povýšit
        // nejlepšího kandidáta ze stavu RESERVED.
        if (request.isUnregister() && originalStatus == PlayerMatchStatus.REGISTERED) {
            System.out.println("spouštím promoteReservedCandidatAferUnregister");
            promoteReservedCandidateAfterUnregister(match, registration);
        }

        NotificationType notificationType = resolveNotificationType(newStatus);
        if (notificationType != null) {
            notifyPlayer(player, notificationType, registration);
        }

        return matchRegistrationMapper.toDTO(registration);
    }

    /**
     * Vyhodnocuje registraci typu REGISTERED, RESERVED nebo SUBSTITUTE
     * a vrací cílový stav registrace.
     *
     * Opakovaná registrace hráče, který je již ve stavu REGISTERED, se neumožňuje.
     * Při volbě SUBSTITUTE se nastaví stav náhradníka, který neblokuje kapacitu zápasu.
     * Při standardní registraci se podle kapacity zápasu určí stav REGISTERED nebo RESERVED.
     *
     * Případná omluva uložená na registraci se před nastavením cílového stavu odstraní.
     *
     * @param request      Požadavek obsahující parametry změny registrace.
     * @param match        Zápas, ke kterému se registrace vztahuje.
     * @param player       Hráč, jehož registrace se upravuje.
     * @param registration Existující registrace, nebo null v případě prvního zápisu.
     * @return Cílový stav registrace odpovídající vyhodnoceným pravidlům.
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
     * Zpracovává odhlášení hráče ze zápasu a vrací cílový stav UNREGISTERED.
     *
     * Odhlášení se umožňuje pouze v případě, že registrace existuje
     * a nachází se ve stavu REGISTERED nebo RESERVED. V opačném případě
     * se vyhodí výjimka signalizující neexistující nebo nepovolenou
     * registraci pro odhlášení.
     *
     * Při úspěšném odhlášení se uloží informace o omluvě z požadavku.
     *
     * @param request      Požadavek obsahující parametry odhlášení.
     * @param playerId     Identifikátor hráče, jehož registrace se odhlašuje.
     * @param registration Existující registrace hráče na zápas.
     * @return Stav UNREGISTERED, který se má na registraci uložit.
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
     * Zpracovává omluvu hráče z účasti na zápase a vrací cílový stav EXCUSED.
     *
     * Omluva se umožňuje pouze v případě, že hráč dosud nereagoval na zápas,
     * je veden jako náhradník nebo je ve stavu NO_EXCUSED.
     * Při porušení pravidel se vyhodí výjimka signalizující nepovolený přechod.
     *
     * Při úspěšné omluvě se uloží důvod a poznámka omluvy z požadavku.
     *
     * @param request      Požadavek obsahující parametry omluvy.
     * @param match        Zápas, ke kterému se omluva vztahuje.
     * @param player       Hráč, jehož omluva se zpracovává.
     * @param registration Existující registrace, nebo null v případě prvního zápisu.
     * @return Stav EXCUSED, který se má na registraci uložit.
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
     * Nastavuje registraci do stavu NO_EXCUSED na základě rozhodnutí administrátora.
     *
     * Zápas musí být již odehraný a předchozí stav registrace musí být REGISTERED.
     * Případná omluva se odstraní a uloží se poznámka administrátora.
     * Po změně se odešle notifikace odpovídající výslednému stavu.
     *
     * @param matchId   Identifikátor zápasu, pro který se stav upravuje.
     * @param playerId  Identifikátor hráče, jehož registrace se upravuje.
     * @param adminNote Poznámka administrátora, která se uloží k registraci.
     * @return DTO aktualizované registrace po změně stavu.
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
     * Nastavuje registraci do stavu EXCUSED na základě rozhodnutí administrátora
     * nebo manažera po předchozím NO_EXCUSED.
     *
     * Zápas musí být již odehraný a předchozí stav registrace musí být NO_EXCUSED.
     * Administrátorská poznámka se odstraní a uloží se důvod a poznámka omluvy.
     * V této operaci se notifikace neodesílají.
     *
     * @param matchId      Identifikátor zápasu, pro který se stav upravuje.
     * @param playerId     Identifikátor hráče, jehož registrace se upravuje.
     * @param excuseReason Důvod omluvy, který se uloží k registraci.
     * @param excuseNote   Poznámka omluvy, která se uloží k registraci.
     * @return DTO aktualizované registrace po změně stavu.
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

    /**
     * Mění tým hráče u existující registrace na zápas.
     *
     * Metoda se používá pro přepnutí hráče mezi týmem LIGHT a DARK.
     * Nejprve se ověří existence zápasu a hráče. Následně se zkontroluje,
     * že běžný hráč (ROLE_PLAYER) nemění tým u již proběhlého zápasu.
     * Administrátor nebo manažer mohou tuto operaci provádět i zpětně.
     *
     * Pokud jsou splněny validační podmínky, je provedena aktualizace registrace.
     * Stav registrace zůstává REGISTERED, ale je přepnut tým na opačný.
     *
     * Po úspěšné změně je vyhodnocen typ notifikace a v případě potřeby
     * je hráči odeslána odpovídající notifikace.
     *
     * @param playerId Identifikátor hráče, jehož registrace má být upravena.
     * @param matchId  Identifikátor zápasu, u kterého má být tým změněn.
     * @return Aktualizovaná registrace převedená do DTO.
     */
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
     * Mění pozici hráče u existující registrace na zápas.
     *
     * Metoda se používá pro úpravu pozice v rámci zápasu. U běžného hráče
     * (ROLE_PLAYER) se změna povoluje pouze pro budoucí zápasy. Administrátor
     * nebo manažer mohou měnit pozici i zpětně.
     *
     * Změna je povolena pouze u aktivních registrací, tedy pro stavy
     * REGISTERED, RESERVED nebo SUBSTITUTE.
     *
     * @param playerId       Identifikátor hráče, jehož registrace má být upravena.
     * @param matchId        Identifikátor zápasu, u kterého má být pozice změněna.
     * @param positionInMatch Cílová pozice hráče v daném zápase.
     * @return Aktualizovaná registrace převedená do DTO.
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
     * Aplikuje společné údaje z požadavku na registraci.
     *
     * V rámci zápisu se upravuje tým, administrátorská poznámka,
     * informace o omluvě a pozice v zápase, pokud jsou v požadavku uvedeny.
     *
     * @param registration Registrace, která se má aktualizovat.
     * @param request      Požadavek obsahující hodnoty, které se mají zapsat do registrace.
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
     * Odstraňuje případnou dříve uloženou omluvu na registraci.
     *
     * Metoda se používá ve chvílích, kdy se hráč znovu registruje nebo mění
     * typ účasti a omluva již není relevantní k novému stavu.
     *
     * @param registration Registrace, u které se má omluva vymazat.
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
     * Přepočítává stavy REGISTERED a RESERVED pro daný zápas podle kapacity zápasu.
     *
     * Registrace se seřadí podle času vytvoření a prvním hráčům do výše kapacity zápasu
     * se nastaví stav REGISTERED. Ostatním hráčům se nastaví stav RESERVED.
     * Registrace ve stavu SUBSTITUTE se do přepočtu nezahrnují.
     *
     * Metoda se používá zejména při změně kapacity zápasu v MatchService.
     *
     * @param matchId Identifikátor zápasu, pro který se stavy přepočítávají.
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
     * Odesílá SMS všem hráčům registrovaným na daný zápas ve stavu REGISTERED,
     * kteří mají povolené SMS notifikace.
     *
     * Z registrací se načtou hráči a vyhodnotí se jejich nastavení notifikací.
     * Pokud je SMS notifikace povolena, sestaví se text zprávy a provede se
     * odeslání přes SmsService.
     *
     * @param matchId Identifikátor zápasu, pro který se SMS zprávy odesílají.
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

    /**
     * Provádí administrátorskou změnu stavu registrace.
     *
     * Před změnou se ověří existence zápasu, hráče i registrace.
     * Stav NO_EXCUSED se touto metodou nenastavuje, protože pro tento stav
     * se používá samostatná operace s vlastní business logikou.
     *
     * @param matchId  Identifikátor zápasu, pro který se stav upravuje.
     * @param playerId Identifikátor hráče, jehož registrace se upravuje.
     * @param status   Cílový stav registrace, který se má nastavit.
     * @return DTO aktualizované registrace po změně stavu.
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
    // LOGIKA POVÝŠENÍ KANDIDÁTA Z RESERVED → REGISTERED
    // ====================================================

    /**
     * Pokouší se po odhlášení hráče najít nejlepšího kandidáta ze stavu RESERVED
     * a povýšit jej do stavu REGISTERED.
     *
     * Registrace ve stavu RESERVED se seřadí podle času registrace a postupně se
     * vyhodnocují podle nastavení hráče v PlayerSettings. Při výběru kandidáta
     * se zohledňuje možnost změny týmu a pozice v zápase a respektuje se
     * zvláštní pravidlo pro pozici brankáře.
     *
     * Metoda aktualizuje maximálně jednu registraci a používá se pouze v kontextu
     * odhlášení ze stavu REGISTERED.
     *
     * @param match               Zápas, ve kterém došlo k uvolnění místa.
     * @param canceledRegistration Registrace hráče, který se odhlásil.
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
                System.out.println("match: " + match.getId() + "candidate: " + candidate.getId()
                        + "freedTeam: " + freedTeam + "freedPosition: " + freedPosition);
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
     * Pokouší se povýšit danou rezervovanou registraci na obsazené místo
     * podle uvolněného týmu a pozice v zápase.
     *
     * Ověřuje se nastavení hráče pro změnu týmu a pozice v PlayerSettings
     * a respektuje se zvláštní pravidlo pro pozici brankáře.
     *
     * Pro uvolněnou pozici GOALIE se připouští pouze kandidát, který je
     * v daném zápase veden na pozici GOALIE (positionInMatch), případně
     * má tuto pozici odvozenu jako primární při chybějící pozici v registraci.
     *
     * Pro ostatní pozice se vyhodnocuje kompatibilita mezi pozicí v registraci
     * a uvolněnou pozicí:
     * - v rámci obrany (DEFENSE, DEFENSE_LEFT, DEFENSE_RIGHT) lze pozice měnit
     *   bez omezení,
     * - v rámci útoku (FORWARD, CENTER, WING_LEFT, WING_RIGHT) lze pozice měnit
     *   bez omezení,
     * - přechod mezi obranou a útokem je povolen pouze pokud má hráč v nastavení
     *   povoleno possibleChangePlayerPosition,
     * - pozice ANY znamená „nezáleží“ a umožňuje obsadit libovolnou ne-brankářskou
     *   pozici.
     *
     * Pokud hráč splní podmínky, je mu nastaven cílový tým a pozice a jeho stav
     * se změní na REGISTERED. Metoda zároveň odešle odpovídající notifikaci.
     *
     * @param candidate     Registrace hráče ve stavu RESERVED.
     * @param freedTeam     Tým, ve kterém se uvolnilo místo.
     * @param freedPosition Pozice, která se uvolnila v zápase.
     * @return True, pokud byl kandidát úspěšně povýšen, jinak false.
     */
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

        // Efektivní pozice kandidáta – primárně z registrace, sekundárně z primární pozice hráče.
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
            // Kandidát nemůže danou pozici obsadit podle pravidel.
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

    // ====================================================
    // PRIVÁTNÍ HELPERY – NAČÍTÁNÍ ENTIT A ZÁKLADNÍ LOGIKA
    // ====================================================
    /**
     * Určuje cílovou pozici kandidáta pro obsazení uvolněného místa.
     *
     * Pravidla:
     * - Pokud je uvolněná pozice null, ponechá se aktuální pozice kandidáta.
     * - Pro pozici GOALIE je nutné, aby efektivní aktuální pozice byla GOALIE.
     *   Na brankáře se kandidát nepřevádí z jiné pozice.
     * - Pro ostatní pozice:
     *   - pokud má kandidát pozici ANY, může obsadit libovolnou ne-brankářskou pozici,
     *   - pokud je cílová pozice stejná jako aktuální, je povolena vždy,
     *   - pokud jsou aktuální a cílová pozice obě v obraně nebo obě v útoku,
     *     je změna povolena bez ohledu na possibleChangePlayerPosition,
     *   - přechod mezi obranou a útokem je povolen pouze pokud je
     *     possibleChangePlayerPosition = true.
     *
     * Pokud není možné cílovou pozici korektně určit, vrací se null.
     *
     * @param currentPosition    Efektivní aktuální pozice kandidáta.
     * @param freedPosition      Uvolněná pozice v zápase.
     * @param canChangePosition  Příznak, zda může kandidát přecházet mezi obranou a útokem.
     * @return Cílová pozice kandidáta nebo null, pokud ji nelze nastavit.
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
            // Změna v rámci obrany nebo v rámci útoku je povolena vždy.
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
     * Určuje, zda daná pozice patří do obrany.
     *
     * Obranné pozice: DEFENSE, DEFENSE_LEFT, DEFENSE_RIGHT.
     *
     * @param position Pozice, která se vyhodnocuje.
     * @return True, pokud jde o obrannou pozici, jinak false.
     */
    private boolean isDefensePosition(PlayerPosition position) {
        if (position == null) {
            return false;
        }
        return position == PlayerPosition.DEFENSE
                || position == PlayerPosition.DEFENSE_LEFT
                || position == PlayerPosition.DEFENSE_RIGHT;
    }

    /**
     * Určuje, zda daná pozice patří do útoku.
     *
     * Útočné pozice: FORWARD, CENTER, WING_LEFT, WING_RIGHT.
     *
     * @param position Pozice, která se vyhodnocuje.
     * @return True, pokud jde o útočnou pozici, jinak false.
     */
    private boolean isForwardPosition(PlayerPosition position) {
        if (position == null) {
            return false;
        }
        return position == PlayerPosition.FORWARD
                || position == PlayerPosition.CENTER
                || position == PlayerPosition.WING_LEFT
                || position == PlayerPosition.WING_RIGHT;
    }
    /**
     * Deleguje odeslání notifikace hráči do NotificationService.
     *
     * Metoda se používá pro centralizaci volání notifikační služby
     * a předání kontextu, který se použije při sestavení obsahu notifikace.
     *
     * @param player  Hráč, kterému se notifikace odesílá.
     * @param type    Typ notifikace určující šablonu a význam zprávy.
     * @param context Kontext notifikace, který se předává do notifikační vrstvy.
     */
    private void notifyPlayer(PlayerEntity player, NotificationType type, Object context) {
        notificationService.notifyPlayer(player, type, context);
    }

    /**
     * Vrací množinu identifikátorů hráčů, kteří mají k zápasu uloženou
     * registraci v jakémkoliv stavu.
     *
     * @param matchId Identifikátor zápasu, pro který se ID hráčů načítají.
     * @return Množina identifikátorů hráčů, kteří na zápas reagovali
     * vytvořením registrace.
     */
    private Set<Long> getRespondedPlayerIds(Long matchId) {
        return registrationRepository.findByMatchId(matchId).stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Načítá zápas podle identifikátoru nebo vyhazuje výjimku při neexistenci.
     *
     * @param matchId Identifikátor zápasu.
     * @return Načtená entita zápasu.
     */
    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Načítá hráče podle identifikátoru nebo vyhazuje výjimku při neexistenci.
     *
     * @param playerId Identifikátor hráče.
     * @return Načtená entita hráče.
     */
    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Načítá registraci hráče na zápas, pokud existuje.
     *
     * @param playerId Identifikátor hráče.
     * @param matchId  Identifikátor zápasu.
     * @return Entita registrace, nebo null pokud registrace neexistuje.
     */
    private MatchRegistrationEntity getRegistrationOrNull(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);
    }

    /**
     * Načítá registraci hráče na zápas nebo vyhazuje výjimku při neexistenci.
     *
     * @param playerId Identifikátor hráče.
     * @param matchId  Identifikátor zápasu.
     * @return Načtená entita registrace.
     */
    private MatchRegistrationEntity getRegistrationOrThrow(Long playerId, Long matchId) {
        return registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RegistrationNotFoundException(matchId, playerId));
    }

    /**
     * Vyhodnocuje, zda je v zápase dostupné místo pro registraci ve stavu REGISTERED.
     *
     * @param match Zápas, pro který se dostupnost místa vyhodnocuje.
     * @return True, pokud počet registrovaných hráčů nedosahuje kapacity zápasu,
     * jinak false.
     */
    private boolean isSlotAvailable(MatchEntity match) {
        long registeredCount = registrationRepository
                .countByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);
        return registeredCount < match.getMaxPlayers();
    }

    /**
     * Odesílá SMS zprávu hráči navázanému na registraci, pokud je k dispozici
     * telefonní číslo.
     *
     * Pokud registrace nebo hráč neexistují, metoda se ukončí bez provedení.
     * Telefonní číslo se vyhodnocuje primárně z nastavení hráče a sekundárně
     * z hodnoty uložené na hráči. Případné chyby při odesílání se pouze zalogují.
     *
     * @param registration Registrace, pro kterou se zpráva odesílá.
     * @param message      Text SMS zprávy.
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
     * Aktualizuje stav registrace a ukládá změnu do databáze.
     *
     * V rámci změny se nastaví nový stav, identifikace původce změny
     * a volitelně se aktualizuje čas změny. Zápis se provádí přes repository
     * vrstvu s okamžitým flush pro zajištění konzistence následných operací.
     *
     * @param registration    Registrace, která se má aktualizovat.
     * @param status          Cílový stav registrace.
     * @param updatedBy       Identifikace původce změny.
     * @param updateTimestamp Příznak určující, zda se má nastavit aktuální čas změny.
     * @return Uložená entita registrace po změně stavu.
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
     * Mapuje stav registrace na typ notifikace.
     *
     * Pokud se pro daný stav notifikace neodesílá, vrací se null.
     *
     * @param newStatus Stav registrace, pro který se typ notifikace vyhodnocuje.
     * @return Typ notifikace odpovídající stavu, nebo null, pokud se notifikace
     * neposílá.
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
     * Vrací aktuální čas.
     *
     * Metoda se používá pro sjednocení přístupu k času a pro usnadnění testování.
     *
     * @return Aktuální čas jako LocalDateTime.
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Ověřuje, že zápas patří do aktivní sezóny.
     *
     * Kontrola se používá před zápisem registrace, aby se zabránilo změnám
     * v neaktivních sezónách.
     *
     * @param match Zápas, který se má ověřit.
     */
    private void assertMatchInActiveSeason(MatchEntity match) {
        if (match.getSeason() == null || !match.getSeason().isActive()) {
            throw new InvalidSeasonStateException(
                    "BE - Registrace lze měnit pouze u zápasů v aktivní sezóně."
            );
        }
    }

    /**
     * Ověřuje, zda aktuálně přihlášený uživatel může měnit registraci na daný zápas.
     *
     * Uživatel s rolí ADMIN nebo MANAGER není časově omezen. Uživatel s rolí PLAYER
     * může registraci měnit pouze v definovaném časovém okně po začátku zápasu.
     * Při porušení pravidel se vyhazuje výjimka signalizující nepovolenou změnu.
     *
     * @param match Zápas, pro který se oprávnění vyhodnocuje.
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
     * Vyhodnocuje, zda je zápas v časovém okně, ve kterém může hráč měnit registraci.
     *
     * Zápas se považuje za editovatelný pro hráče do třiceti minut po začátku zápasu.
     *
     * @param match Zápas, ke kterému se editovatelnost vyhodnocuje.
     * @return True, pokud je změna registrace ještě povolena, jinak false.
     */
    private boolean isMatchEditableForPlayer(MatchEntity match) {
        LocalDateTime editLimit = match.getDateTime().plusMinutes(30);
        return now().isBefore(editLimit);
    }

    /**
     * Vyhodnocuje, zda má aktuálně přihlášený uživatel roli ROLE_PLAYER.
     *
     * Metoda se používá pro aplikaci časového omezení, které se vztahuje pouze
     * na hráče.
     *
     * @return True, pokud má uživatel roli ROLE_PLAYER, jinak false.
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
     * Vyhodnocuje, zda zápas patří do aktuálně vybrané sezóny.
     *
     * Porovnává se identifikátor sezóny zápasu s identifikátorem sezóny
     * vráceným metodou getCurrentSeasonIdOrActive.
     *
     * @param match Zápas, který se má vyhodnotit.
     * @return True, pokud zápas patří do aktuální sezóny, jinak false.
     */
    private boolean isMatchInCurrentSeason(MatchEntity match) {
        if (match == null || match.getSeason() == null) {
            return false;
        }
        Long seasonId = getCurrentSeasonIdOrActive();
        return seasonId.equals(match.getSeason().getId());
    }

    /**
     * Vyhodnocuje, zda registrace patří k zápasu v aktuálně vybrané sezóně.
     *
     * @param registration Registrace, která se má vyhodnotit.
     * @return True, pokud registrace patří do aktuální sezóny, jinak false.
     */
    private boolean isRegistrationInCurrentSeason(MatchRegistrationEntity registration) {
        if (registration == null) {
            return false;
        }
        return isMatchInCurrentSeason(registration.getMatch());
    }

    /**
     * Vrací identifikátor sezóny používané pro filtrování registrací.
     *
     * Primárně se používá sezóna uložená v CurrentSeasonService. Pokud není k dispozici,
     * použije se globálně aktivní sezóna získaná ze SeasonService.
     *
     * @return Identifikátor aktuální nebo aktivní sezóny.
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Vrací registrace pro daný zápas omezené na aktuálně vybranou sezónu.
     *
     * Pokud zápas nepatří do aktuálně vybrané sezóny, vrací se prázdný seznam.
     *
     * @param matchId Identifikátor zápasu, pro který se registrace načítají.
     * @return Seznam registrací převedených do DTO pro daný zápas v rámci
     * aktuální sezóny.
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
     * Vrací registrace pro zadanou sadu zápasů omezené na aktuálně vybranou sezónu.
     *
     * Pokud je seznam identifikátorů zápasů null nebo prázdný, vrací se prázdný seznam.
     *
     * @param matchIds Seznam identifikátorů zápasů, pro které se registrace načítají.
     * @return Seznam registrací převedených do DTO pro zadané zápasy
     * v rámci aktuální sezóny.
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
     * Vrací všechny registrace v systému omezené na aktuálně vybranou sezónu.
     *
     * @return Seznam všech registrací převedených do DTO v rámci aktuální sezóny.
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
     * Vrací registrace zadaného hráče omezené na aktuálně vybranou sezónu.
     *
     * @param playerId Identifikátor hráče, jehož registrace se načítají.
     * @return Seznam registrací hráče převedených do DTO v rámci aktuální sezóny.
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
     * Vrací hráče, kteří na daný zápas nijak nereagovali.
     *
     * Pokud zápas nepatří do aktuálně vybrané sezóny, vrací se prázdný seznam.
     * Při vyhodnocování se používá množina hráčů, kteří mají k zápasu uloženou
     * registraci v jakémkoliv stavu.
     *
     * @param matchId Identifikátor zápasu, pro který se hráči bez reakce vyhodnocují.
     * @return Seznam hráčů bez reakce převedených do DTO v rámci aktuální sezóny.
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

}