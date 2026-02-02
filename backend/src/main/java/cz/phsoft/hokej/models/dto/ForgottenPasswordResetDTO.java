package cz.phsoft.hokej.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pro nastavení nového hesla na základě resetovacího tokenu.
 *
 * Používá se v procesu zapomenutého hesla. Obsahuje token, nové heslo
 * a jeho potvrzení. Kontrola shody hesel se provádí v servisní vrstvě.
 */
public class ForgottenPasswordResetDTO {

    @NotBlank(message = "Reset token je povinný.")
    private String token;

    @NotBlank(message = "Nové heslo je povinné.")
    @Size(min = 8, max = 64, message = "Nové heslo musí mít 8–64 znaků.")
    private String newPassword;

    @NotBlank(message = "Potvrzení nového hesla je povinné.")
    private String newPasswordConfirm;

    // gettery / settery

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getNewPasswordConfirm() { return newPasswordConfirm; }
    public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }
}
