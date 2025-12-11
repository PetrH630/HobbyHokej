package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("playerSecurity") // název pro použití v SpEL (@PreAuthorize)
public class PlayerSecurity {

    private static final Logger logger = LoggerFactory.getLogger(PlayerSecurity.class);

    private final PlayerRepository playerRepository;

    public PlayerSecurity(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Zjistí, zda je aktuálně přihlášený uživatel vlastníkem hráče
     *
     * @param authentication aktuální authentication objekt
     * @param playerId       ID hráče
     * @return true pokud je vlastníkem, jinak false
     */
    public boolean isOwner(Authentication authentication, Long playerId) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Neautorizovaný přístup: žádná autentizace pro playerId {}", playerId);
                return false;
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails userDetails)) {
                logger.warn("Neautorizovaný přístup: principal není UserDetails pro playerId {}", playerId);
                return false;
            }

            boolean isOwner = playerRepository.findById(playerId)
                    .map(player -> player.getUser() != null &&
                            player.getUser().getEmail().equals(userDetails.getUsername()))
                    .orElse(false);

            if (!isOwner) {
                logger.warn("Neautorizovaný přístup: uživatel {} není vlastníkem hráče {}", userDetails.getUsername(), playerId);
            }

            return isOwner;

        } catch (Exception e) {
            logger.error("Chyba při kontrole vlastníka hráče {}: {}", playerId, e.getMessage(), e);
            // nikdy nepropustit výjimku do SpEL
            return false;
        }
    }
}
