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
 * Slouží pro jemnozrnnou autorizaci pomocí SpEL výrazů
 * (např. v @PreAuthorize anotacích).
 *
 * Typické použití:
 * ----------------
 * @PreAuthorize("@playerSecurity.isOwner(authentication, #playerId)")
 *
 * Poznámka:
 * ----------
 * Aktuálně nepoužívám (používáš /me a CurrentPlayer),
 * Připravené řešení do budoucna.
 */
@Component("playerSecurity") // bean name pro SpEL
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
     * 1) ověření existence autentizace
     * 2) ověření, že principal je UserDetails
     * 3) načtení hráče z DB
     * 4) porovnání emailu uživatele s email usera u hráče
     *
     * BEZPEČNOSTNÍ PRAVIDLO:
     * ---------------------
     * Jakákoli chyba = přístup ZAMÍTNUT
     *
     * @param authentication aktuální Authentication objekt
     * @param playerId       ID hráče
     * @return true pokud je uživatel vlastníkem hráče
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
                            player.getUser() != null &&
                                    player.getUser().getEmail()
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
            // NIKDY nepropustit výjimku do SpEL výrazu
            // SpEL musí vždy vrátit boolean
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
