package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class PlayerSecurity {

    private final PlayerRepository playerRepository;

    public PlayerSecurity(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public boolean isOwner(UserDetails userDetails, Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId).orElse(null);
        if (player == null) return false;
        return player.getUser() != null &&
                player.getUser().getEmail().equals(userDetails.getUsername());
    }
}
