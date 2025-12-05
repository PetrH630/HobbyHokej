package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.models.services.SmsService;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final SmsService smsService;

    public PlayerServiceImpl(PlayerRepository playerRepository, SmsService smsService) {
        this.playerRepository = playerRepository;
        this.smsService = smsService;
    }

    @Override
    public List<PlayerEntity> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public PlayerEntity getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found: " + id));
    }

    @Override
    public PlayerEntity createPlayer(PlayerEntity player) {
        PlayerEntity savedPlayer = playerRepository.save(player);

        // odeslání jen na testovací číslo
        smsService.sendSms("Testovací zpráva: hráč " + savedPlayer.getName() + " byl zaregistrován.");

        return savedPlayer;
    }

    @Override
    public PlayerEntity updatePlayer(Long id, PlayerEntity newData) {
        PlayerEntity existing = getPlayerById(id);

        existing.setName(newData.getName());
        existing.setSurname(newData.getSurname());
        existing.setType(newData.getType());
        existing.setJerseyColor((newData.getJerseyColor()));
        return playerRepository.save(existing);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }
}