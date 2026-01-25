package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Bezpečnostní helper pro kontrolu vlastnictví hráče.
 *
 * ÚČEL:
 * -----
 * Poskytuje metody pro jemnozrnnou autorizaci nad entitou {@link PlayerEntity},
 * typicky ve SpEL výrazech v anotacích jako {@code @PreAuthorize}.
 *
 * Typické použití:
 * <pre>
 * {@code
 * @PreAuthorize("@playerSecurity.isOwner(authentication, #playerId)")
 * public ResponseEntity<?> getPlayerDetail(Long playerId) { ... }
 * }
 * </pre>
 *
 * Poznámka:
 * ---------
 * V aktuální verzi využíváš hlavně endpointy typu {@code /me}
 * a {@link CurrentPlayerContext}, ale tato třída je připravena
 * pro případné rozšíření autorizace na úrovni konkrétních hráčů.
 */
@Component("playerSecurity") // název beany pro použití ve SpEL výrazech
public class PlayerSecurity {

    private static final Logger logger =
            LoggerFactory.getLogger(PlayerSecurity.class);

    private final PlayerRepository playerRepository;

    public PlayerSecurity(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Ověří, zda je aktuálně přihlášený uživatel vlastníkem daného hráče.
     *
     * LOGIKA:
     * -------
     * <ol>
     *     <li>ověří existenci autentizace a její platnost,</li>
     *     <li>ověří, že {@code principal} je typu {@link UserDetails},</li>
     *     <li>načte hráče z DB podle {@code playerId},</li>
     *     <li>porovná email přihlášeného uživatele (username)
     *         s emailem uživatele přiřazeného k hráči.</li>
     * </ol>
     *
     * BEZPEČNOSTNÍ PRAVIDLO:
     * ----------------------
     * Jakákoli chyba nebo nesrovnalost → přístup je zamítnut
     * (metoda vždy vrací {@code false}, nikdy nevyhazuje výjimku).
     *
     * @param authentication aktuální {@link Authentication} objekt
     * @param playerId       ID hráče, ke kterému se má ověřit vlastnictví
     * @return {@code true}, pokud je uživatel vlastníkem hráče; jinak {@code false}
     */
    public boolean isOwner(Authentication authentication, Long playerId) {

        try {
            // 1) základní kontrola autentizace
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn(
                        "Neautorizovaný přístup: žádná autentizace pro playerId {}",
                        playerId
                );
                return false;
            }

            // 2) kontrola typu principal
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails userDetails)) {
                logger.warn(
                        "Neautorizovaný přístup: principal není UserDetails pro playerId {}",
                        playerId
                );
                return false;
            }

            // 3) kontrola vlastnictví hráče
            boolean isOwner = playerRepository.findById(playerId)
                    .map(player ->
                            player.getUser() != null
                                    && player.getUser().getEmail()
                                    .equals(userDetails.getUsername())
                    )
                    .orElse(false);

            // 4) logování pokusu o neoprávněný přístup
            if (!isOwner) {
                logger.warn(
                        "Neautorizovaný přístup: uživatel {} není vlastníkem hráče {}",
                        userDetails.getUsername(),
                        playerId
                );
            }

            return isOwner;

        } catch (Exception e) {
            // NIKDY nepropouštět výjimku do SpEL výrazu – vždy vracíme boolean
            logger.error(
                    "Chyba při kontrole vlastníka hráče {}: {}",
                    playerId,
                    e.getMessage(),
                    e
            );
            return false;
        }
    }
}
