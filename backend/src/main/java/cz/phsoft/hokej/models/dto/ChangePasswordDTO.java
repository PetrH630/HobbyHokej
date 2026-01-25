package cz.phsoft.hokej.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pro změnu hesla přihlášeného uživatele.
 *
 * Používá se při:
 * <ul>
 *     <li>změně hesla z uživatelského profilu,</li>
 *     <li>ověření původního hesla před nastavením nového.</li>
 * </ul>
 *
 * Validace:
 * <ul>
 *     <li>všechna pole jsou povinná,</li>
 *     <li>nové heslo musí splňovat minimální délku,</li>
 *     <li>shoda nového hesla a potvrzení se kontroluje v servisní vrstvě.</li>
 * </ul>
 */
public class ChangePasswordDTO {

    @NotBlank(message = "Původní heslo je povinné.")
    private String oldPassword;

    @NotBlank(message = "Nové heslo je povinné.")
    @Size(min = 8, max = 64, message = "Nové heslo musí mít 8–64 znaků.")
    private String newPassword;

    @NotBlank(message = "Potvrzení nového hesla je povinné.")
    private String newPasswordConfirm;

    // gettery / settery

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getNewPasswordConfirm() { return newPasswordConfirm; }
    public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }
}
