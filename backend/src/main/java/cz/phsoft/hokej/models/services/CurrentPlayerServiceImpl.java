package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.CurrentPlayerNotSelectedException;
import cz.phsoft.hokej.exceptions.InvalidPlayerStatusException;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.security.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Service pro správu „aktuálně zvoleného hráče“ přihlášeného uživatele.
 * <p>
 * význam:
 * <ul>
 *     <li>jeden uživatel může mít více hráčů,</li>
 *     <li>pro většinu operací (registrace na zápasy, přehledy, statistiky)
 *     musí být jednoznačně určen aktuální hráč.</li>
 * </ul>
 *
 * Technické řešení:
 * <ul>
 *     <li>aktuální hráč je uložen v HTTP session,</li>
 *     <li>do session se ukládá pouze ID hráče, nikoliv celá entita.</li>
 * </ul>
 *
 * Tato service:
 * <ul>
 *     <li>ukládá a čte ID aktuálního hráče ze session,</li>
 *     <li>ověřuje existenci hráče,</li>
 *     <li>hlídá, že hráč je ve správném stavu (APPROVED).</li>
 * </ul>
 *
 * Tato service neřeší:
 * <ul>
 *     <li>oprávnění uživatele k hráči (řeší {@link PlayerService}),</li>
 *     <li>business logiku zápasů.</li>
 * </ul>
 */
@Service
public class CurrentPlayerServiceImpl implements CurrentPlayerService {

    /**
     * HTTP session vázaná na přihlášeného uživatele.
     */
    private final HttpSession session;

    /**
     * Repozitář hráčů – slouží k ověření existence a stavu hráče.
     */
    private final PlayerRepository playerRepository;

    public CurrentPlayerServiceImpl(HttpSession session,
                                    PlayerRepository playerRepository) {
        this.session = session;
        this.playerRepository = playerRepository;
    }

    /**
     * Vrátí ID aktuálně zvoleného hráče ze session.
     *
     * @return ID hráče nebo {@code null}, pokud ještě nebyl vybrán
     */
    @Override
    public Long getCurrentPlayerId() {
        return (Long) session.getAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }

    /**
     * Nastaví aktuálního hráče do session.
     * <p>
     * Validace:
     * <ul>
     *     <li>hráč musí existovat,</li>
     *     <li>hráč musí být ve stavu {@link PlayerStatus#APPROVED}.</li>
     * </ul>
     *
     * @param playerId ID hráče, který má být nastaven jako aktuální
     */
    @Override
    public void setCurrentPlayerId(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);
        validatePlayerSelectable(player);

        session.setAttribute(SessionKeys.CURRENT_PLAYER_ID, playerId);
    }

    /**
     * Ověří, že je aktuální hráč nastaven.
     * <p>
     * Používá se zejména:
     * <ul>
     *     <li>před registrací na zápas,</li>
     *     <li>u endpointů pracujících s kontextem „/me“.</li>
     * </ul>
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
     * Odstraní aktuálního hráče ze session.
     * <p>
     * Typicky se používá při:
     * <ul>
     *     <li>odhlášení uživatele,</li>
     *     <li>resetu uživatelského kontextu.</li>
     * </ul>
     */
    @Override
    public void clear() {
        session.removeAttribute(SessionKeys.CURRENT_PLAYER_ID);
    }

    // ==================================================
    // HELPER METODY
    // ==================================================

    /**
     * Najde hráče podle ID nebo vyhodí výjimku.
     */
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Ověří, zda hráč může být zvolen jako „aktuální“.
     * <p>
     * Aktuálně je povolen pouze stav {@link PlayerStatus#APPROVED}.
     *
     * Možná budoucí rozšíření:
     * <ul>
     *     <li>hráč nesmí být smazaný,</li>
     *     <li>hráč musí mít vyplněné kontaktní údaje,</li>
     *     <li>hráč nesmí být dlouhodobě neaktivní.</li>
     * </ul>
     */
    private void validatePlayerSelectable(PlayerEntity player) {
        if (player.getPlayerStatus() != PlayerStatus.APPROVED) {
            throw new InvalidPlayerStatusException(
                    "BE - Nelze zvolit hráče, který není schválen administrátorem."
            );
        }
    }
}
