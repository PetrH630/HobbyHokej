package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.data.entities.PlayerEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final MatchRegistrationService registrationService;
    private final PlayerRepository playerRepository;
    private final PlayerInactivityPeriodService playerInactivityPeriodService;

    public MatchServiceImpl(MatchRepository matchRepository,
                            MatchMapper matchMapper,
                            MatchRegistrationService registrationService,
                            PlayerRepository playerRepository,
                            PlayerInactivityPeriodService playerInactivityPeriodService) { // ← DOPLNĚNO !!!
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.registrationService = registrationService;
        this.playerRepository = playerRepository;
        this.playerInactivityPeriodService = playerInactivityPeriodService;
    }

    //Metoda pro získání všech zápasu
    @Override
    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }
    // Metoda pro získání nadcházejících zápasů
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }
    // metoda pro získání uplynulých zápasů
    public List<MatchDTO> getPastMatches() {
        return matchRepository.findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }
    // metoda pro získání následujícího zápasu - možná zbytečná už mám metodu - getUpcomingMatchesForPlayer
    @Override
    public MatchDTO getNextMatch() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .findFirst()
                .map(matchMapper::toDTO)
                .orElse(null);
    }
    // metoda pro získání zápasu dle id
    @Override
    public MatchDTO getMatchById(Long id) {
        MatchEntity match = findMatchOrThrow(id);
        return matchMapper.toDTO(match);
    }

    // metoda pro vytvoření zápasu
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        return matchMapper.toDTO(matchRepository.save(entity));
    }

    // metoda pro update zápasů - po změně maxPlayers se automaticky přepočítá kapacita a hráči
    // co byli reserved nebo naopak registered budou dle kapacity přesunutí do reserved/registered
    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        MatchEntity match = findMatchOrThrow(id);

        int oldMaxPlayers = match.getMaxPlayers();
        matchMapper.updateEntity(dto, match);
        MatchEntity saved = matchRepository.save(match);

        if (saved.getMaxPlayers() != oldMaxPlayers) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }

        return matchMapper.toDTO(saved);
    }
    // metoda pro vymazání zápasu
    @Override
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

   // metoda pro detail zápasu - do samostatné přepravky MatchDetailDto
    @Override
    public MatchDetailDTO getMatchDetail(Long id) {

        MatchEntity match = findMatchOrThrow(id);

        List<MatchRegistrationEntity> registrations = registrationService.getRegistrationsForMatch(id);

        List<MatchRegistrationEntity> registered = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .toList();

        List<MatchRegistrationEntity> reserved = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.RESERVED)
                .toList();

        List<MatchRegistrationEntity> unregistered = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.UNREGISTERED)
                .toList();

        List<MatchRegistrationEntity> excused = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.EXCUSED)
                .toList();

        // --- 2) NO-RESPONSE HRÁČI PŘÍMO ZDE ---
        List<PlayerEntity> allPlayers = playerRepository.findAll();

        Set<Long> respondedIds = registrations.stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());

        List<PlayerEntity> noResponsePlayers = allPlayers.stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .toList();

        // počty hráčů - stats
        int inGamePlayers = registered.size();
        int outGamePlayers = unregistered.size() + excused.size();
        int waitingPlayers = reserved.size();
        int noActionPlayers = noResponsePlayers.size();

        int remainingSlots = match.getMaxPlayers() - inGamePlayers;
        // cena za jednoho
        double pricePerRegistered = inGamePlayers > 0
                ? match.getPrice() / (double) inGamePlayers
                : 0;

        // nakrmení dto
        MatchDetailDTO dto = new MatchDetailDTO();
        dto.setId(match.getId());
        dto.setDateTime(match.getDateTime());
        dto.setMaxPlayers(match.getMaxPlayers());

        dto.setInGamePlayers(inGamePlayers);
        dto.setOutGamePlayers(outGamePlayers);
        dto.setWaitingPlayers(waitingPlayers);
        dto.setNoActionPlayers(noActionPlayers);

        dto.setPricePerRegisteredPlayer(pricePerRegistered);
        dto.setRemainingSlots(remainingSlots);

        dto.setRegisteredPlayers(
                registered.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setReservedPlayers(
                reserved.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setUnregisteredPlayers(
                unregistered.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setExcusedPlayers(
                excused.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setNoResponsePlayers(
                noResponsePlayers.stream()
                        .map(p -> p.getName() + " " + p.getSurname())
                        .toList()
        );

        return dto;
    }

    // metoda pro zobrazení zápasu hráči dle id hráče - pouze zápasy v období kdy byl aktivní
    public List<MatchEntity> getAvailableMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        List<MatchEntity> allMatches = matchRepository.findAll();

        return allMatches.stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .collect(Collectors.toList());
    }
    // metoda pro zobrazení nadcházejících zápasů pro hráče dle typu - VIP, STANDARD, BASIC -
    // je nastaveno aby se někteří mohli přihlásit na zápas dříve než ostatní
    public List<MatchEntity> getUpcomingMatchesForPlayer(Long playerId) {
        // najde hráče
        PlayerEntity player = findPlayerOrThrow(playerId);

        PlayerType type = player.getType();

        LocalDateTime now = LocalDateTime.now();

        // načte všechny zápasy po dnešku
        List<MatchEntity> upcoming = matchRepository.findByDateTimeAfterOrderByDateTimeAsc(now);

        // Vyfiltruje podle aktivity hráče - jestli je v daném termínu aktivní
        List<MatchEntity> activeMatches = upcoming.stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .toList();

        // nastavení omezení podle PlayerType
        return switch (type) {
            case VIP -> activeMatches; // všechny nadcházející aktivní zápasy

            case STANDARD -> activeMatches.stream()
                    .limit(2)
                    .toList(); // první dva

            case BASIC -> activeMatches.isEmpty()
                    ? List.of()
                    : List.of(activeMatches.get(0)); // jen nejbližší
        };
    }
    // pomocná metoda - boiler code
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));
    }
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));
    }

}
