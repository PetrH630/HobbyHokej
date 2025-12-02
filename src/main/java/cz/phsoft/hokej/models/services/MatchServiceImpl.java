package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final MatchRegistrationService registrationService;

    public MatchServiceImpl(MatchRepository matchRepository,
                            MatchMapper matchMapper,
                            MatchRegistrationService registrationService) {
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.registrationService = registrationService;
    }

    @Override
    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDTO> getUpcomingMatches() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getPastMatches() {
        return matchRepository.findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MatchDTO getNextMatch() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .findFirst()
                .map(matchMapper::toDTO)
                .orElse(null);
    }

    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found: " + id)));
    }

    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        return matchMapper.toDTO(matchRepository.save(entity));
    }

    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        MatchEntity entity = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found: " + id));

        int oldMaxPlayers = entity.getMaxPlayers(); // uložíme starou hodnotu
        matchMapper.updateEntity(dto, entity);
        MatchEntity saved = matchRepository.save(entity);

        // pokud se změnila kapacita, přepočítáme statusy hráčů
        if (saved.getMaxPlayers() != oldMaxPlayers) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }

        return matchMapper.toDTO(saved);
    }

    @Override
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }


}
