package cz.phsoft.hokej.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pro nastavení nového hesla na základě resetovacího tokenu.
 *
 * Používá se v procesu zapomenutého hesla. Obsahuje token, nové heslo
 * a jeho potvrzení. Kontrola platnosti tokenu, expirace a shody hesel
 * se provádí v servisní vrstvě, která odpovídá za bezpečné nastavení
 * nového hesla.
 *
 * DTO slouží pouze jako transportní objekt mezi klientem a API.
 */
public class ForgottenPasswordResetDTO {

    /**
     * Resetovací token vygenerovaný backendem.
     *
     * Token se používá k ověření oprávněnosti požadavku na změnu hesla.
     */
    @NotBlank(message = "Reset token je povinný.")
    private String token;

    /**
     * Nové heslo uživatele.
     *
     * Musí splňovat minimální délku a případné další bezpečnostní
     * požadavky definované v servisní vrstvě.
     */
    @NotBlank(message = "Nové heslo je povinné.")
    @Size(min = 8, max = 64, message = "Nové heslo musí mít 8–64 znaků.")
    private String newPassword;

    /**
     * Potvrzení nového hesla.
     *
     * Slouží k ověření, že uživatel zadal nové heslo správně.
     * Kontrola shody s newPassword se provádí v servisní vrstvě.
     */
    @NotBlank(message = "Potvrzení nového hesla je povinné.")
    private String newPasswordConfirm;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getNewPasswordConfirm() { return newPasswordConfirm; }
    public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }
}