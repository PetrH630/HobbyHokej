package cz.phsoft.hokej.user.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO pro změnu přiřazeného uživatele k hráči.
 *
 * Slouží jako vstupní objekt pro administrátorské endpointy,
 * které umožňují změnit vazbu mezi hráčem a aplikačním uživatelem.
 * Třída neobsahuje žádnou business logiku, používá se pouze
 * pro přenos dat z klienta do API vrstvy.
 */
public class ChangePlayerUserRequest {

    /**
     * ID nového uživatele, ke kterému má být hráč přiřazen.
     *
     * Hodnota je povinná. Validace se provádí anotací {@link NotNull}
     * a následně v servisní vrstvě při vyhledávání uživatele.
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
