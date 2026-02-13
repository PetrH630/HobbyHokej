package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.CurrentPlayerNotSelectedException;
import cz.phsoft.hokej.exceptions.InvalidPlayerStatusException;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.security.SessionKeys;
import cz.phsoft.hokej.security.impersonation.ImpersonationContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Implementace rozhraní CurrentPlayerService.
 *
 * Třída spravuje identifikátor aktuálně zvoleného hráče v HTTP session
 * přihlášeného uživatele. V session se ukládá pouze ID hráče, nikoli
 * celá entita.
 *
 * Pokud je aktivní režim zastoupení administrátorem, je jako aktuální hráč
 * vracen hráč určený v impersonačním kontextu. V takovém případě se hodnota
 * v session nepoužívá a zůstává beze změny.
 *
 * Pomocí PlayerRepository se ověřuje, zda hráč existuje a zda je ve stavu
 * vhodném pro použití v aplikaci.
 *
 * Třída neřeší oprávnění uživatele k danému hráči ani business logiku
 * zápasů a registrací. Tyto oblasti jsou pokryty jinými service třídami.
 */
@Service
public class CurrentPlayerServiceImpl implements CurrentPlayerService {

    /**
     * HTTP session vázaná na přihlášeného uživatele.
     * Slouží k uchování identifikátoru aktuálního hráče.
     */
    private final HttpSession session;

    /**
     * Repository pro práci s entitami hráčů.
     * Používá se k ověření existence hráče a jeho aktuálního stavu.
     */
    private final PlayerRepository playerRepository;

    public CurrentPlayerServiceImpl(HttpSession session,
                                    PlayerRepository playerRepository) {
        this.session = session;
        this.playerRepository = playerRepository;
    }

    /**
     * Vrátí identifikátor aktuálního hráče.
     *
     * Pokud je aktivní režim zastoupení, je vrácen identifikátor impersonovaného
     * hráče. V opačném případě je vrácen identifikátor uložený v HTTP session.
     *
     * @return ID hráče nebo null, pokud aktuální hráč ještě nebyl zvolen
     */
    @Override
    public Long getCurrentPlayerId() {
        Long impersonatedPlayerId = ImpersonationContext.getImpersonatedPlayerId();
        if (impersonatedPlayerId != null) {
            return impersonatedPlayerId;
        }
        return (Long) session.getAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }

    /**
     * Nastaví aktuálního hráče do HTTP session.
     *
     * Před uložením do session se ověří, že hráč existuje
     * a že je ve stavu PlayerStatus.APPROVED. Pokud některá
     * z podmínek není splněna, je vyhozena výjimka.
     *
     * Režim zastoupení tuto operaci nemění. Zvolený hráč se ukládá
     * do session a používá se při běžném režimu bez impersonace.
     *
     * @param playerId ID hráče, který má být nastaven jako aktuální
     * @throws PlayerNotFoundException pokud hráč s daným ID neexistuje
     * @throws InvalidPlayerStatusException pokud hráč není ve schváleném stavu
     */
    @Override
    public void setCurrentPlayerId(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);
        validatePlayerSelectable(player);

        session.setAttribute(SessionKeys.CURRENT_PLAYER_ID, playerId);
    }

    /**
     * Ověří, že je aktuální hráč nastaven.
     *
     * Metoda se používá před operacemi, které vyžadují kontext
     * aktuálního hráče, například před registrací na zápas
     * nebo při volání endpointů pracujících s „/me“.
     *
     * Pokud je aktivní režim zastoupení, je podmínka považována
     * za splněnou, protože aktuální hráč je určen impersonačním kontextem.
     *
     * @throws CurrentPlayerNotSelectedException pokud aktuální hráč není nastaven
     */
    @Override
    public void requireCurrentPlayer() {
        Long currentPlayerId = getCurrentPlayerId();
        if (currentPlayerId == null) {
            throw new CurrentPlayerNotSelectedException();
        }
    }

    /**
     * Odstraní informaci o aktuálním hráči z HTTP session.
     *
     * Metoda se používá při odhlášení uživatele nebo při resetu
     * uživatelského kontextu, kdy již nemá být vazba na konkrétního hráče.
     *
     * Režim zastoupení se neukládá do session a jeho vyčištění se řeší
     * na úrovni request filtru.
     */
    @Override
    public void clear() {
        session.removeAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }

    // ==================================================
    // Helper metody
    // ==================================================

    /**
     * Najde hráče podle ID nebo vyhodí výjimku.
     *
     * @param playerId ID hledaného hráče
     * @return entita PlayerEntity odpovídající zadanému ID
     * @throws PlayerNotFoundException pokud hráč s daným ID neexistuje
     */
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Ověří, zda může být hráč zvolen jako aktuální.
     *
     * V současné době je povolen pouze stav PlayerStatus.APPROVED.
     * Ostatní stavy jsou považovány za neplatné pro použití
     * v kontextu přihlášeného uživatele.
     *
     * @param player entita hráče, která má být ověřena
     * @throws InvalidPlayerStatusException pokud hráč není ve schváleném stavu
     */
    private void validatePlayerSelectable(PlayerEntity player) {
        if (player.getPlayerStatus() != PlayerStatus.APPROVED) {
            throw new InvalidPlayerStatusException(
                    "BE - Nelze zvolit hráče, který není schválen administrátorem."
            );
        }
    }
}
