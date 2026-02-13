package cz.phsoft.hokej.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pro změnu hesla přihlášeného uživatele.
 *
 * Používá se při změně hesla z uživatelského profilu, kde je potřeba
 * ověřit původní heslo a nastavit nové. DTO obsahuje staré heslo,
 * nové heslo a jeho potvrzení. Kontrola shody nového hesla a potvrzení
 * se provádí v servisní vrstvě.
 *
 * Všechna pole jsou povinná a nové heslo musí splňovat minimální délku.
 */
public class ChangePasswordDTO {

    @NotBlank(message = "Původní heslo je povinné.")
    private String oldPassword;

    @NotBlank(message = "Nové heslo je povinné.")
    @Size(min = 8, max = 64, message = "Nové heslo musí mít 8–64 znaků.")
    private String newPassword;

    @NotBlank(message = "Potvrzení nového hesla je povinné.")
    private String newPasswordConfirm;



    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getNewPasswordConfirm() { return newPasswordConfirm; }
    public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }
}
