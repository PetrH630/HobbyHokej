package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.ImpersonationInfoDTO;
import cz.phsoft.hokej.security.impersonation.ImpersonationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller poskytující informace o aktuálním uživatelském kontextu.
 *
 * Obsahuje endpointy typu /api/me, které vrací informace
 * o aktuálním přihlášeném uživateli nebo režimu zastoupení.
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final PlayerRepository playerRepository;

    public MeController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Vrátí informaci o aktuálním režimu zastoupení.
     *
     * Pokud je aktivní impersonace, vrátí identifikátor a jméno
     * zastupovaného hráče. Pokud není aktivní, vrátí pouze příznak false.
     *
     * Endpoint vyžaduje přihlášení.
     *
     * @return informace o zastoupení
     */
    @GetMapping("/impersonation")
    public ResponseEntity<ImpersonationInfoDTO> getImpersonationInfo() {

        Long playerId = ImpersonationContext.getImpersonatedPlayerId();

        if (playerId == null) {
            return ResponseEntity.ok(
                    new ImpersonationInfoDTO(false, null, null)
            );
        }

        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);

        if (playerOpt.isEmpty()) {
            return ResponseEntity.ok(
                    new ImpersonationInfoDTO(false, null, null)
            );
        }

        PlayerEntity player = playerOpt.get();

        String playerName = player.getName() + " " + player.getSurname();

        return ResponseEntity.ok(
                new ImpersonationInfoDTO(true, player.getId(), playerName)
        );
    }
}
