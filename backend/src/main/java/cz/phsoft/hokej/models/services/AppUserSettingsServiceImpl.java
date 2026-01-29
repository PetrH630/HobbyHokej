package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.data.enums.GlobalNotificationLevel;
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
 * Implementace service pro práci s nastavením uživatele.
 */
@Service
@Transactional
public class AppUserSettingsServiceImpl implements AppUserSettingsService {

    private final AppUserRepository appUserRepository;
    private final AppUserSettingsRepository appUserSettingsRepository;
    private final AppUserSettingsMapper mapper;

    public AppUserSettingsServiceImpl(AppUserRepository appUserRepository,
                                      AppUserSettingsRepository appUserSettingsRepository,
                                      AppUserSettingsMapper mapper) {
        this.appUserRepository = appUserRepository;
        this.appUserSettingsRepository = appUserSettingsRepository;
        this.mapper = mapper;
    }

    @Override
    public AppUserSettingsDTO getSettingsForUser(String userEmail) {
        AppUserEntity user = findUserByEmailOrThrow(userEmail);

        // zkusíme najít existující settings
        Optional<AppUserSettingsEntity> existingOpt = appUserSettingsRepository.findByUser(user);

        AppUserSettingsEntity settings = existingOpt.orElseGet(() -> {
            // vytvoření default nastavení pro uživatele, pokud ještě neexistuje
            AppUserSettingsEntity created = createDefaultSettingsForUser(user);
            return appUserSettingsRepository.save(created);
        });

        return mapper.toDTO(settings);
    }

    @Override
    public AppUserSettingsDTO updateSettingsForUser(String userEmail, AppUserSettingsDTO dto) {
        AppUserEntity user = findUserByEmailOrThrow(userEmail);

        AppUserSettingsEntity settings = appUserSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettingsForUser(user));

        // aplikovat hodnoty z DTO
        mapper.updateEntityFromDTO(dto, settings);

        // zajistit napojení na usera (pro případ nového objektu)
        settings.setUser(user);

        AppUserSettingsEntity saved = appUserSettingsRepository.save(settings);

        return mapper.toDTO(saved);
    }

    // =========================================
    // HELPER METODY
    // =========================================

    private AppUserEntity findUserByEmailOrThrow(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public AppUserSettingsEntity createDefaultSettingsForUser(AppUserEntity user) {
        AppUserSettingsEntity settings = new AppUserSettingsEntity();
        settings.setUser(user);

        // výchozí hodnoty – stejné jako v entitě, ale explicitně
        settings.setPlayerSelectionMode(PlayerSelectionMode.FIRST_PLAYER);
        settings.setGlobalNotificationLevel(GlobalNotificationLevel.ALL);
        settings.setCopyAllPlayerNotificationsToUserEmail(false);
        settings.setReceiveNotificationsForPlayersWithOwnEmail(false);
        settings.setEmailDigestEnabled(false);
        settings.setEmailDigestTime(null);
        settings.setUiLanguage("cs");
        settings.setTimezone("Europe/Prague");
        settings.setDefaultLandingPage("DASHBOARD");

        appUserSettingsRepository.save(settings);

        return settings;

    }
}
