package cz.phsoft.hokej.models.dto.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO používané pro změnu přiřazení hráče k aplikačnímu uživateli.
 *
 * Slouží jako vstupní objekt pro administrátorské endpointy, které umožňují
 * změnit vazbu mezi entitou hráče a aplikačním uživatelským účtem.
 * Používá se například při opravě chybného přiřazení nebo při převodu
 * hráče pod jiný uživatelský účet.
 *
 * Třída je určena výhradně pro přenos dat z klienta do API vrstvy
 * a neobsahuje žádnou business logiku ani validační rozhodování
 * nad rámec základních validačních anotací.
 */
public class ChangePlayerUserRequest {

    /**
     * ID uživatele, ke kterému má být hráč nově přiřazen.
     *
     * Hodnota je povinná a musí odpovídat existujícímu uživatelskému
     * účtu v systému. Samotná kontrola existence uživatele se provádí
     * v servisní vrstvě.
     */
    @NotNull
    private Long newUserId;

    /**
     * Vrací ID nového uživatele.
     *
     * @return ID uživatele, ke kterému má být hráč přiřazen
     */
    public Long getNewUserId() {
        return newUserId;
    }

    /**
     * Nastavuje ID nového uživatele.
     *
     * @param newUserId ID uživatele, ke kterému má být hráč přiřazen
     */
    public void setNewUserId(Long newUserId) {
        this.newUserId = newUserId;
    }
}
