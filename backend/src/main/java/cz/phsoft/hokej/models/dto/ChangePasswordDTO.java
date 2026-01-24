package cz.phsoft.hokej.models.dto;

import jakarta.validation.constraints.*;

public class ChangePasswordDTO {
    @NotBlank(message = "Původní heslo je povinné.")
    private String oldPassword;

    @NotBlank(message = "Nové heslo je povinné.")
    @Size(min = 8, max = 64, message = "Nové heslo musí mít 8–64 znaků.")
    private String newPassword;

    @NotBlank(message = "Potvrzení nového hesla je povinné.")
    private String newPasswordConfirm;


    // gettery a settery
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getNewPasswordConfirm() { return newPasswordConfirm; }
    public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }
}

