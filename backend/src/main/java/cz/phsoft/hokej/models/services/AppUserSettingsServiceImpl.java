package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.data.enums.GlobalNotificationLevel;
import cz.phsoft.hokej.data.enums.LandingPage;
import cz.phsoft.hokej.data.enums.PlayerSelectionMode;
import cz.phsoft.hokej.data.repositories.AppUserSettingsRepository;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.exceptions.UserNotFoundException;
import cz.phsoft.hokej.models.dto.AppUserSettingsDTO;
import cz.phsoft.hokej.models.mappers.AppUserSettingsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementace service pro práci s uživatelským nastavením.
 *
 * Odpovědností je vyhledávání uživatele podle e-mailu,
 * získávání nebo vytváření odpovídajícího AppUserSettingsEntity
 * a mapování na AppUserSettingsDTO. V rámci této třídy se udržuje
 * vazba mezi nastavením a uživatelským účtem.
 *
 * Třída představuje transakční hranici pro operace s uživatelským
 * nastavením. Validace vstupů z HTTP vrstvy a autorizace se řeší
 * v controllerech a bezpečnostní vrstvě.
 */
@Service
@Transactional
public class AppUserSettingsServiceImpl implements AppUserSettingsService {

    private final AppUserRepository appUserRepository;
    private final AppUserSettingsRepository appUserSettingsRepository;
    private final AppUserSettingsMapper mapper;

    /**
     * Vytvoří instanci service s repository a mapperem.
     *
     * Repository se používají pro práci s entitami AppUserEntity
     * a AppUserSettingsEntity. Mapper zajišťuje převod mezi entitou
     * a AppUserSettingsDTO pro komunikaci s frontendem.
     *
     * @param appUserRepository repository pro uživatelské účty
     * @param appUserSettingsRepository repository pro uživatelská nastavení
     * @param mapper mapper pro převod mezi entitou a DTO
     */
    public AppUserSettingsServiceImpl(AppUserRepository appUserRepository,
                                      AppUserSettingsRepository appUserSettingsRepository,
                                      AppUserSettingsMapper mapper) {
        this.appUserRepository = appUserRepository;
        this.appUserSettingsRepository = appUserSettingsRepository;
        this.mapper = mapper;
    }

    /**
     * Načte nastavení pro uživatele identifikovaného e-mailem.
     *
     * Pokud nastavení neexistuje, vytvoří se nová entita s výchozími
     * hodnotami pomocí metody createDefaultSettingsForUser a uloží se.
     * Volající část aplikace tak vždy obdrží validní nastavení.
     *
     * @param userEmail e-mail uživatele, pro kterého se nastavení načítá
     * @return AppUserSettingsDTO s aktuálním nastavením uživatele
     * @throws UserNotFoundException pokud uživatel s daným e-mailem neexistuje
     */
    @Override
    public AppUserSettingsDTO getSettingsForUser(String userEmail) {
        AppUserEntity user = findUserByEmailOrThrow(userEmail);

        Optional<AppUserSettingsEntity> existingOpt = appUserSettingsRepository.findByUser(user);

        AppUserSettingsEntity settings = existingOpt.orElseGet(() -> {
            AppUserSettingsEntity created = createDefaultSettingsForUser(user);
            return appUserSettingsRepository.save(created);
        });

        return mapper.toDTO(settings);
    }

    /**
     * Aktualizuje nastavení pro uživatele identifikovaného e-mailem.
     *
     * Pokud uživatel žádné nastavení nemá, vytvoří se nová entita
     * s výchozími hodnotami a následně se do ní aplikují hodnoty
     * z předaného DTO. Je zajištěno, že nastavení je navázáno
     * na správného uživatele.
     *
     * @param userEmail e-mail uživatele, pro kterého se nastavení aktualizuje
     * @param dto nové hodnoty nastavení z frontendu
     * @return AppUserSettingsDTO reprezentující uložené nastavení
     * @throws UserNotFoundException pokud uživatel s daným e-mailem neexistuje
     */
    @Override
    public AppUserSettingsDTO updateSettingsForUser(String userEmail, AppUserSettingsDTO dto) {
        AppUserEntity user = findUserByEmailOrThrow(userEmail);

        AppUserSettingsEntity settings = appUserSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettingsForUser(user));

        mapper.updateEntityFromDTO(dto, settings);

        settings.setUser(user);

        AppUserSettingsEntity saved = appUserSettingsRepository.save(settings);

        return mapper.toDTO(saved);
    }

    // =========================================
    // Helper metody
    // =========================================

    /**
     * Najde uživatele podle e-mailu nebo vyhodí výjimku.
     *
     * Metoda centralizuje logiku pro vyhledávání uživatele v databázi.
     *
     * @param email e-mail hledaného uživatele
     * @return entita AppUserEntity, pokud byla nalezena
     * @throws UserNotFoundException pokud uživatel s daným e-mailem neexistuje
     */
    private AppUserEntity findUserByEmailOrThrow(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    /**
     * Vytvoří výchozí nastavení pro daného uživatele.
     *
     * Výchozí hodnoty jsou nastaveny explicitně, aby byly
     * snadno dohledatelné na jednom místě. Metoda se používá
     * při prvním načtení nastavení nebo při zakládání nového účtu.
     *
     * @param user entita uživatele, ke které se nastavení naváže
     * @return entita AppUserSettingsEntity s výchozími hodnotami
     */
    @Override
    public AppUserSettingsEntity createDefaultSettingsForUser(AppUserEntity user) {
        AppUserSettingsEntity settings = new AppUserSettingsEntity();
        settings.setUser(user);

        settings.setPlayerSelectionMode(PlayerSelectionMode.FIRST_PLAYER);
        settings.setGlobalNotificationLevel(GlobalNotificationLevel.ALL);
        settings.setManagerNotificationLevel(GlobalNotificationLevel.ALL);
        settings.setCopyAllPlayerNotificationsToUserEmail(false);
        settings.setReceiveNotificationsForPlayersWithOwnEmail(false);
        settings.setEmailDigestEnabled(false);
        settings.setEmailDigestTime(null);
        settings.setUiLanguage("cs");
        settings.setTimezone("Europe/Prague");
        settings.setDefaultLandingPage(LandingPage.DASHBOARD);

        appUserSettingsRepository.save(settings);

        return settings;
    }
}
