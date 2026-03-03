package cz.phsoft.hokej.security;

import cz.phsoft.hokej.player.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Bezpečnostní helper pro kontrolu vlastnictví hráče.
 *
 * Poskytuje metody určené pro použití ve SpEL výrazech
 * v anotacích typu PreAuthorize.
 *
 * Slouží k jemnozrnné autorizaci nad entitou PlayerEntity,
 * zejména v případech, kdy se pracuje s konkrétním identifikátorem hráče.
 * Kontrola probíhá na základě porovnání e-mailu přihlášeného uživatele
 * s vlastníkem hráče v databázi.
 */
@Component("playerSecurity")
public class PlayerSecurity {

    private static final Logger logger =
            LoggerFactory.getLogger(PlayerSecurity.class);

    private final PlayerRepository playerRepository;

    /**
     * Vytvoří instanci bezpečnostního helperu.
     *
     * @param playerRepository repozitář pro načítání hráčů z databáze
     */
    public PlayerSecurity(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Ověří, zda je aktuálně přihlášený uživatel
     * vlastníkem zadaného hráče.
     *
     * Metoda je určena pro použití v bezpečnostních výrazech.
     * Pokud autentizace neexistuje, není platná,
     * nebo hráč neexistuje, vrací se hodnota false.
     *
     * V případě neoprávněného přístupu je událost zaznamenána do logu.
     * Při neočekávané chybě je přístup zamítnut a chyba je zalogována.
     *
     * @param authentication aktuální autentizační kontext
     * @param playerId identifikátor hráče
     * @return true, pokud je přihlášený uživatel vlastníkem hráče
     */
    public boolean isOwner(Authentication authentication, Long playerId) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails userDetails)) {
                return false;
            }

            boolean isOwner = playerRepository.findById(playerId)
                    .map(player ->
                            player.getUser() != null
                                    && player.getUser().getEmail()
                                    .equals(userDetails.getUsername())
                    )
                    .orElse(false);

            if (!isOwner) {
                logger.warn(
                        "Neoprávněný přístup: uživatel {} není vlastníkem hráče {}",
                        userDetails.getUsername(),
                        playerId
                );
            }

            return isOwner;

        } catch (Exception e) {
            logger.error(
                    "Chyba při ověřování vlastníka hráče {}",
                    playerId,
                    e
            );
            return false;
        }
    }
}