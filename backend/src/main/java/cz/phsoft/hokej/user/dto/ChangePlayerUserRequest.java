package cz.phsoft.hokej.user.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO pro změnu přiřazeného uživatele k hráči.
 *
 * Slouží jako vstupní objekt pro administrátorské endpointy,
 * které umožňují změnit vazbu mezi hráčem a aplikačním uživatelem.
 * Třída neobsahuje žádnou business logiku, používá se pouze
 * pro přenos dat z klienta do API vrstvy, kde je dále zpracována
 * v servisní vrstvě.
 */
public class ChangePlayerUserRequest {

    /**
     * ID nového uživatele, ke kterému má být hráč přiřazen.
     *
     * Hodnota je povinná. Validace se provádí anotací NotNull
     * a následně v servisní vrstvě při vyhledávání a ověření
     * existence uživatele.
     */
    @NotNull
    private Long newUserId;

    /**
     * Vrátí ID nového uživatele, ke kterému má být hráč přiřazen.
     *
     * Metoda se používá v servisní vrstvě při provádění změny vazby
     * mezi hráčem a uživatelem.
     *
     * @return ID uživatele
     */
    public Long getNewUserId() {
        return newUserId;
    }

    /**
     * Nastaví ID nového uživatele, ke kterému má být hráč přiřazen.
     *
     * Hodnota se předává z kontroleru, který zpracovává request
     * z klientské aplikace.
     *
     * @param newUserId ID uživatele, ke kterému má být hráč přiřazen
     */
    public void setNewUserId(Long newUserId) {
        this.newUserId = newUserId;
    }
}