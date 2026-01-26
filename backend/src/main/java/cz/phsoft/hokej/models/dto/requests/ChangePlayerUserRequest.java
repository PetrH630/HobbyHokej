package cz.phsoft.hokej.models.dto.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO pro změnu přiřazeného uživatele k hráči.
 * <p>
 * Slouží jako vstupní objekt pro administrátorské endpointy,
 * které umožňují změnit vazbu mezi hráčem a aplikačním uživatelem.
 * </p>
 *
 * Typické použití:
 * <ul>
 *     <li>oprava chybně přiřazeného uživatelského účtu k hráči,</li>
 *     <li>převod hráče pod jiný účet,</li>
 *     <li>technické nebo datové korekce prováděné administrátorem.</li>
 * </ul>
 *
 * Validace:
 * <ul>
 *     <li>{@link NotNull} – ID nového uživatele musí být vždy uvedeno.</li>
 * </ul>
 *
 * Třída neobsahuje žádnou business logiku – slouží výhradně
 * jako přenosový objekt mezi klientem a API vrstvou.
 */
public class ChangePlayerUserRequest {

    /**
     * ID nového uživatele, ke kterému má být hráč přiřazen.
     */
    @NotNull
    private Long newUserId;

    /**
     * Vrátí ID nového uživatele.
     *
     * @return ID uživatele
     */
    public Long getNewUserId() {
        return newUserId;
    }

    /**
     * Nastaví ID nového uživatele.
     *
     * @param newUserId ID uživatele, ke kterému má být hráč přiřazen
     */
    public void setNewUserId(Long newUserId) {
        this.newUserId = newUserId;
    }
}
