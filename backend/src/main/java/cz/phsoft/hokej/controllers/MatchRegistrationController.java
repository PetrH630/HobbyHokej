package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import cz.phsoft.hokej.data.enums.JerseyColor;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class MatchRegistrationController {

    private final MatchRegistrationService service;
    private final MatchRegistrationMapper matchRegistrationMapper;
    private final PlayerMapper playerMapper;

    public MatchRegistrationController(MatchRegistrationService service,
                                       MatchRegistrationMapper matchRegistrationMapper,
                                       PlayerMapper playerMapper) {
        this.service = service;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.playerMapper = playerMapper;
    }

    // vytvoření registrace hráče - id hráče k id zápasu
    @PostMapping("/register")
    public MatchRegistrationDTO register(@RequestParam Long matchId, @RequestParam Long playerId,
                                         @RequestParam(required = false) JerseyColor jerseyColor,
                                         @RequestParam(required = false) String adminNote) {
        MatchRegistrationEntity entity = service.registerPlayer(matchId, playerId, jerseyColor, adminNote);
        return matchRegistrationMapper.toDTO(entity);
    }

    // zrušení registrace hráče k zápasu - změna statutu na unregistered
    @PostMapping("/unregister")
    public MatchRegistrationDTO unregister(@RequestParam Long matchId,
                                           @RequestParam Long playerId,
                                           @RequestParam String reason,
                                           @RequestParam(required = false) String note) {
        MatchRegistrationEntity entity = service.unregisterPlayer(matchId, playerId, note, reason);
        return matchRegistrationMapper.toDTO(entity);
    }

    // omluvení hráče ze zápasu - jen pokud ještě neměl registraci
    @PostMapping("/excuse")
    public MatchRegistrationDTO excuse(@RequestParam Long matchId,
                                       @RequestParam Long playerId,
                                       @RequestParam String reason,
                                       @RequestParam(required = false) String note) {
        MatchRegistrationEntity entity = service.excusePlayer(matchId, playerId, note, reason);
        return matchRegistrationMapper.toDTO(entity);
    }

    // všechny registrace
    @GetMapping("/all")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationMapper.toDTOList(service.getAllRegistrations());
    }

    // všechny registace k hráči dle id hráče
    @GetMapping("/for-player/{playerId}")
    public List<MatchRegistrationDTO> forPlayer(@PathVariable Long playerId) {
        return matchRegistrationMapper.toDTOList(service.getRegistrationsForPlayer(playerId));
    }

    // všechny registrace k zápasu dle id zápasu
    @GetMapping("/for-match/{matchId}")
    public List<MatchRegistrationDTO> forMatch(@PathVariable Long matchId) {
        return matchRegistrationMapper.toDTOList(service.getRegistrationsForMatch(matchId));
    }

    // všichni hráči co se ani neregistrovali, neodhlásili, neomluvili - bez reakce
    @GetMapping("/no-response/{matchId}")
    public List<PlayerDTO> getNoResponse(@PathVariable Long matchId) {
        return playerMapper.toDTOList(service.getNoResponsePlayers(matchId));
    }

}
