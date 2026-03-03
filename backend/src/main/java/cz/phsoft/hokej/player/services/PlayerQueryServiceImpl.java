package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.mappers.PlayerMapper;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace čtecí servisní vrstvy pro práci s hráči.
 *
 * Třída zajišťuje výhradně čtecí operace nad entitou hráče.
 * Neprovádí žádné změny stavu systému, neobsahuje zápisovou logiku
 * a nevyvolává žádné notifikace.
 *
 * Odpovědnost:
 * - načítání hráčů z repository vrstvy,
 * - převod entit na DTO pomocí mapperu,
 * - vyhazování doménových výjimek při nenalezení hráče.
 */
@Service
public class PlayerQueryServiceImpl implements PlayerQueryService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    /**
     * Vytvoří instanci query služby pro práci s hráči.
     *
     * Závislosti jsou injektovány konstruktorově.
     *
     * @param playerRepository repository pro přístup k hráčům
     * @param playerMapper mapper pro převod entity na DTO
     */
    public PlayerQueryServiceImpl(PlayerRepository playerRepository,
                                  PlayerMapper playerMapper) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
    }

    /**
     * Vrátí seznam všech hráčů evidovaných v systému.
     *
     * Metoda načte všechny entity hráčů z databáze
     * a provede jejich převod na DTO reprezentaci.
     *
     * @return seznam hráčů ve formě PlayerDTO
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    /**
     * Vrátí detail hráče podle jeho identifikátoru.
     *
     * Pokud hráč s daným identifikátorem neexistuje,
     * je vyhozena výjimka PlayerNotFoundException.
     *
     * @param id identifikátor hráče
     * @return hráč ve formě PlayerDTO
     */
    @Override
    public PlayerDTO getPlayerById(Long id) {
        PlayerEntity player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
        return playerMapper.toDTO(player);
    }

    /**
     * Vrátí seznam hráčů přiřazených ke konkrétnímu uživateli.
     *
     * Hráči jsou vráceni seřazeni podle identifikátoru vzestupně.
     * Metoda načítá hráče podle vazby na uživatelský účet
     * a provádí jejich převod na DTO reprezentaci.
     *
     * @param email e-mail uživatele
     * @return seznam hráčů daného uživatele ve formě PlayerDTO
     */
    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerRepository.findByUser_EmailOrderByIdAsc(email).stream()
                .map(playerMapper::toDTO)
                .toList();
    }
}