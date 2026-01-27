package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.models.dto.PlayerDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service vrstva pro práci s hráči ({@link PlayerEntity}).
 * <p>
 * Zodpovídá za:
 * <ul>
 *     <li>CRUD operace nad hráči (create/update/delete),</li>
 *     <li>kontrolu duplicity (jméno + příjmení),</li>
 *     <li>vazbu hráče na uživatele ({@link AppUserEntity}),</li>
 *     <li>změnu statusu hráče (APPROVED / REJECTED) včetně notifikací,</li>
 *     <li>nastavení „aktuálního hráče“ v {@link CurrentPlayerService} pro přihlášeného uživatele.</li>
 * </ul>
 * <p>
 * Co tato service NEřeší:
 * <ul>
 *     <li>HTTP/session vrstvu – to obstarávají controllery + CurrentPlayerService,</li>
 *     <li>autentizaci/autorizaci – řeší Spring Security a controller vrstva,</li>
 *     <li>detailní logiku zápasů – řeší {@link MatchServiceImpl}.</li>
 * </ul>
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;
    private final CurrentPlayerService currentPlayerService;

    public PlayerServiceImpl(
            PlayerRepository playerRepository,
            PlayerMapper playerMapper,
            AppUserRepository appUserRepository,
            NotificationService notificationService,
            CurrentPlayerService currentPlayerService
    ) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.appUserRepository = appUserRepository;
        this.notificationService = notificationService;
        this.currentPlayerService = currentPlayerService;
    }

    // ======================
    // READ
    // ======================

    /**
     * Vrátí všechny hráče v systému namapované na {@link PlayerDTO}.
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    /**
     * Vrátí jednoho hráče podle ID.
     *
     * @param id ID hráče
     * @return {@link PlayerDTO} odpovídající hráči
     * @throws PlayerNotFoundException pokud hráč s daným ID neexistuje
     */
    @Override
    public PlayerDTO getPlayerById(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);
        return playerMapper.toDTO(player);
    }

    /**
     * Vrátí všechny hráče, kteří patří uživateli s daným emailem
     * ({@link AppUserEntity#getEmail()}), seřazené podle ID vzestupně.
     *
     * @param email email uživatele
     */
    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerRepository.findByUser_EmailOrderByIdAsc(email).stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    // ======================
    // CREATE / UPDATE / DELETE
    // ======================

    /**
     * Vytvoří nového hráče bez vazby na uživatele.
     * <p>
     * Kroky:
     * <ol>
     *     <li>zkontroluje duplicitu jména + příjmení,</li>
     *     <li>namapuje {@link PlayerDTO} → {@link PlayerEntity},</li>
     *     <li>uloží hráče,</li>
     *     <li>notifikace se aktuálně NEposílají (zachování původního chování).</li>
     * </ol>
     */
    @Override
    @Transactional
    public PlayerDTO createPlayer(PlayerDTO dto) {
        // kontrola, že neexistuje jiný hráč se stejným jménem + příjmením
        ensureUniqueNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity entity = playerMapper.toEntity(dto);
        PlayerEntity saved = playerRepository.save(entity);

        // Původně zde byla notifikace:
        // notificationService.notifyPlayer(saved, NotificationType.PLAYER_CREATED, null);
        // → zachováváme původní chování = bez notifikace.

        return playerMapper.toDTO(saved);
    }

    /**
     * Vytvoří nového hráče a rovnou ho přiřadí k uživateli (dle emailu).
     * <p>
     * Kroky:
     * <ol>
     *     <li>najde {@link AppUserEntity} podle emailu,</li>
     *     <li>zkontroluje duplicitu jméno + příjmení,</li>
     *     <li>namapuje DTO → entity, přiřadí uživatele,</li>
     *     <li>uloží hráče a odešle notifikaci typu {@link NotificationType#PLAYER_CREATED}.</li>
     * </ol>
     *
     * @param dto       data hráče
     * @param userEmail email uživatele, kterému má hráč patřit
     * @throws UserNotFoundException         pokud uživatel neexistuje
     * @throws DuplicateNameSurnameException pokud existuje hráč se stejným jménem a příjmením
     */
    @Override
    @Transactional
    public PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail) {
        AppUserEntity user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        ensureUniqueNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity player = playerMapper.toEntity(dto);
        player.setUser(user);

        PlayerEntity saved = saveAndNotify(player, NotificationType.PLAYER_CREATED);

        return playerMapper.toDTO(saved);
    }

    /**
     * Aktualizuje existujícího hráče.
     * <p>
     * Kroky:
     * <ol>
     *     <li>najde hráče podle ID,</li>
     *     <li>pokud se mění jméno/příjmení → zkontroluje duplicitu,</li>
     *     <li>přepíše základní údaje (jméno, příjmení, přezdívka, telefon, typ, tým, status),</li>
     *     <li>uloží hráče a odešle notifikaci {@link NotificationType#PLAYER_UPDATED}.</li>
     * </ol>
     *
     * @throws PlayerNotFoundException       pokud hráč neexistuje
     * @throws DuplicateNameSurnameException pokud nová kombinace jméno+příjmení koliduje s jiným hráčem
     */
    @Override
    @Transactional
    public PlayerDTO updatePlayer(Long id, PlayerDTO dto) {
        PlayerEntity existing = findPlayerOrThrow(id);

        boolean nameChanged =
                !existing.getName().equals(dto.getName()) ||
                        !existing.getSurname().equals(dto.getSurname());

        // kontrola duplicity jen pokud se jméno/příjmení mění
        if (nameChanged) {
            ensureUniqueNameSurname(dto.getName(), dto.getSurname(), id);
        }

        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setNickname(dto.getNickname());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setType(dto.getType());
        existing.setTeam(dto.getTeam());
        if (dto.getPlayerStatus() != null) {
            existing.setPlayerStatus(dto.getPlayerStatus());
        }

        PlayerEntity saved = saveAndNotify(existing, NotificationType.PLAYER_UPDATED);

        return playerMapper.toDTO(saved);
    }

    /**
     * Smaže hráče podle ID.
     *
     * @param id ID hráče
     * @return {@link SuccessResponseDTO} s potvrzující zprávou
     * @throws PlayerNotFoundException pokud hráč neexistuje
     */
    @Override
    @Transactional
    public SuccessResponseDTO deletePlayer(Long id) {
        PlayerEntity player = findPlayerOrThrow(id);
        playerRepository.delete(player);

        String message = "Hráč " + player.getFullName() + " byl úspěšně smazán";
        return buildSuccessResponse(message, id);
    }

    // ======================
    // STATUS – APPROVE / REJECT
    // ======================

    /**
     * Schválí hráče (nastaví {@link PlayerStatus#APPROVED}).
     * <ul>
     *     <li>pokud už je APPROVED → vyhodí {@link InvalidPlayerStatusException},</li>
     *     <li>po uložení odešle notifikaci {@link NotificationType#PLAYER_APPROVED}.</li>
     * </ul>
     */
    @Override
    @Transactional
    public SuccessResponseDTO approvePlayer(Long id) {
        return changePlayerStatus(
                id,
                PlayerStatus.APPROVED,          // cílový status
                PlayerStatus.APPROVED,          // status, při kterém hlásíme "už je schválen"
                NotificationType.PLAYER_APPROVED,
                "BE - Hráč už je schválen.",
                "Hráč %s byl úspěšně aktivován"
        );
    }

    /**
     * Zamítne hráče (nastaví {@link PlayerStatus#REJECTED}).
     * <ul>
     *     <li>pokud už je REJECTED → vyhodí {@link InvalidPlayerStatusException},</li>
     *     <li>po uložení odešle notifikaci {@link NotificationType#PLAYER_REJECTED}.</li>
     * </ul>
     */
    @Override
    @Transactional
    public SuccessResponseDTO rejectPlayer(Long id) {
        return changePlayerStatus(
                id,
                PlayerStatus.REJECTED,           // cílový status
                PlayerStatus.REJECTED,           // status, při kterém hlásíme "už je zamítnut"
                NotificationType.PLAYER_REJECTED,
                "BE - Hráč už je zamítnut.",
                "Hráč %s byl úspěšně zamítnut"
        );
    }

    // ======================
    // CURRENT PLAYER – SESSION
    // ======================

    /**
     * Nastaví aktuálního hráče pro daného uživatele.
     * <p>
     * Kroky:
     * <ol>
     *     <li>najde hráče podle ID,</li>
     *     <li>ověří, že hráč patří danému uživateli (podle emailu),</li>
     *     <li>předá ID hráče do {@link CurrentPlayerService#setCurrentPlayerId(Long)}.</li>
     * </ol>
     *
     * @param userEmail email přihlášeného uživatele
     * @param playerId  ID hráče, který má být nastaven jako aktuální
     * @throws PlayerNotFoundException        pokud hráč neexistuje
     * @throws ForbiddenPlayerAccessException pokud hráč nepatří uživateli
     */
    @Override
    public SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        assertPlayerBelongsToUser(player, userEmail);

        currentPlayerService.setCurrentPlayerId(playerId);

        String message = "BE - Aktuální hráč nastaven na ID: " + playerId;
        return buildSuccessResponse(message, playerId);
    }

    /**
     * Automaticky vybere aktuálního hráče pro uživatele po přihlášení.
     * <ul>
     *     <li>pokud má přesně 1 hráče → nastaví ho jako aktuálního,</li>
     *     <li>pokud má 0 nebo více hráčů → nic nenastaví, FE musí vybrat hráče ručně.</li>
     * </ul>
     *
     * @param userEmail email přihlášeného uživatele
     */
    @Override
    public SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail) {
        List<PlayerDTO> players = getPlayersByUser(userEmail);

        if (players.size() == 1) {
            PlayerDTO player = players.get(0);
            currentPlayerService.setCurrentPlayerId(player.getId());

            String message = "BE - Automaticky nastaven aktuální hráč na ID: " + player.getId();
            return buildSuccessResponse(message, player.getId());
        }

        // 0 nebo více hráčů – necháme na FE, ať si uživatel zvolí hráče
        String message = "BE - Uživatel má více (nebo žádné) hráče, je nutný ruční výběr.";
        return buildSuccessResponse(message, null);
    }

    /**
     * Změní přiřazeného aplikačního uživatele k hráči.
     * <p>
     * Typické použití:
     * <ul>
     *     <li>oprava chybně spárovaného hráče a uživatele,</li>
     *     <li>převod hráče pod jiný účet (např. rodič → jiný rodič),</li>
     *     <li>úklid dat po sloučení / změně uživatelských účtů.</li>
     * </ul>
     *
     * Kroky:
     * <ol>
     *     <li>najde hráče podle ID ({@link #findPlayerOrThrow(Long)}),</li>
     *     <li>najde nového uživatele podle ID ({@link #findUserOrThrow(Long)}),</li>
     *     <li>ověří, zda už není hráč přiřazen tomuto uživateli,</li>
     *     <li>pokud ano → vyhodí {@link InvalidChangePlayerUserException},</li>
     *     <li>pokud ne → přepíše vazbu {@code player.user} na nového uživatele.</li>
     * </ol>
     *
     * Metoda:
     * <ul>
     *     <li>neposílá žádné notifikace,</li>
     *     <li>nezasahuje do nastavení „current player“ – to případně řeší volající.</li>
     * </ul>
     *
     * @param id        ID hráče, kterému se mění vazba na uživatele
     * @param newUserId ID nového {@link AppUserEntity}, ke kterému má být hráč přiřazen
     * @throws PlayerNotFoundException           pokud hráč s daným ID neexistuje
     * @throws UserNotFoundException             pokud uživatel s daným ID neexistuje
     * @throws InvalidChangePlayerUserException  pokud je hráč již přiřazen tomuto uživateli
     */
    @Transactional
    public void changePlayerUser(Long id, Long newUserId) {
        PlayerEntity player = findPlayerOrThrow(id);
        AppUserEntity newUser = findUserOrThrow(newUserId);
        AppUserEntity oldUser = player.getUser();
        if (oldUser != null && oldUser.getId().equals(newUserId)) {

            throw new InvalidChangePlayerUserException();

        }
        player.setUser(newUser);

    }

    // ======================
    // PRIVATE HELPERY – ENTITY / DUPLICITY
    // ======================

    /**
     * Najde hráče podle ID, nebo vyhodí {@link PlayerNotFoundException}.
     */
    private PlayerEntity findPlayerOrThrow(Long Id) {
        return playerRepository.findById(Id)
                .orElseThrow(() -> new PlayerNotFoundException(Id));
    }

    /**
     * Najde uživatele podle ID, nebo vyhodí {@link UserNotFoundException}.
     */
    private AppUserEntity findUserOrThrow(Long Id) {
        return appUserRepository.findById(Id)
                .orElseThrow(() -> new UserNotFoundException(Id));
    }

    /**
     * Kontrola duplicity kombinace jméno + příjmení.
     *
     * @param name     jméno hráče
     * @param surname  příjmení hráče
     * @param ignoreId ID hráče, kterého chceme ignorovat (typicky hráč,
     *                 kterého zrovna upravujeme), nebo {@code null} u create
     */
    private void ensureUniqueNameSurname(String name, String surname, Long ignoreId) {
        Optional<PlayerEntity> duplicateOpt = playerRepository.findByNameAndSurname(name, surname);

        if (duplicateOpt.isPresent()) {
            PlayerEntity duplicate = duplicateOpt.get();

            // pokud máme ignoreId, dovolíme "duplicitu sám se sebou"
            if (ignoreId == null || !duplicate.getId().equals(ignoreId)) {
                throw new DuplicateNameSurnameException(name, surname);
            }
        }
    }

    /**
     * Uloží hráče a odešle notifikaci daného typu.
     * <p>
     * Použití:
     * <ul>
     *     <li>create/update hráče,</li>
     *     <li>approve/reject hráče.</li>
     * </ul>
     */
    private PlayerEntity saveAndNotify(PlayerEntity player, NotificationType type) {
        PlayerEntity saved = playerRepository.save(player);
        notificationService.notifyPlayer(saved, type, null);
        return saved;
    }

    /**
     * Ověří, že hráč skutečně patří danému uživateli (porovnává email v uživateli).
     *
     * @throws ForbiddenPlayerAccessException pokud hráč nepatří danému uživateli
     */
    private void assertPlayerBelongsToUser(PlayerEntity player, String userEmail) {
        if (player.getUser() == null ||
                player.getUser().getEmail() == null ||
                !player.getUser().getEmail().equals(userEmail)) {

            throw new ForbiddenPlayerAccessException(player.getId());
        }
    }

    /**
     * Pomocná metoda pro jednotné vytváření {@link SuccessResponseDTO}
     * (delete / approve / reject / nastavení current player).
     */
    private SuccessResponseDTO buildSuccessResponse(String message, Long id) {
        return new SuccessResponseDTO(
                message,
                id,
                LocalDateTime.now().toString()
        );
    }

    /**
     * Obecná pomocná metoda pro změnu statusu hráče (approve/reject).
     *
     * @param id                     ID hráče
     * @param targetStatus           cílový status, na který se má hráč nastavit
     * @param alreadyStatus          status, který znamená „už je v tomto stavu“
     * @param notificationType       typ notifikace, která se má odeslat
     * @param alreadyMessage         text chyby, pokud je hráč už v {@code alreadyStatus}
     * @param successMessageTemplate šablona textu pro SuccessResponseDTO
     */
    private SuccessResponseDTO changePlayerStatus(Long id,
                                                  PlayerStatus targetStatus,
                                                  PlayerStatus alreadyStatus,
                                                  NotificationType notificationType,
                                                  String alreadyMessage,
                                                  String successMessageTemplate) {

        PlayerEntity player = findPlayerOrThrow(id);

        // ochrana proti dvojímu schválení / zamítnutí
        if (player.getPlayerStatus() == alreadyStatus) {
            throw new InvalidPlayerStatusException(alreadyMessage);
        }

        player.setPlayerStatus(targetStatus);
        PlayerEntity saved = saveAndNotify(player, notificationType);

        String message = String.format(successMessageTemplate, saved.getFullName());
        return buildSuccessResponse(message, id);
    }


}
