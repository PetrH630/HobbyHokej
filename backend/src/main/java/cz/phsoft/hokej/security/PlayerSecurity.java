package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("playerSecurity") // název pro použití v SpEL (@PreAuthorize)
public class PlayerSecurity {

    private final PlayerRepository playerRepository;

    public PlayerSecurity(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public boolean isOwner(org.springframework.security.core.Authentication authentication, Long playerId) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
                return false;
            }

            return playerRepository.findById(playerId)
                    .map(player -> player.getUser() != null &&
                            player.getUser().getEmail().equals(userDetails.getUsername()))
                    .orElse(false);

        } catch (Exception e) {
            // nikdy nepropustit výjimku do SpEL
            return false;
        }
    }
}