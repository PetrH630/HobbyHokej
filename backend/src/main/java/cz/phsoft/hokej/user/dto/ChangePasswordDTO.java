package cz.phsoft.hokej.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pro změnu hesla přihlášeného uživatele.
 *
 * Používá se při změně hesla z uživatelského profilu, kde je potřeba
 * ověřit původní heslo a nastavit nové. DTO obsahuje staré heslo,
 * nové heslo a jeho potvrzení. Kontrola shody nového hesla a potvrzení
 * se provádí v servisní vrstvě společně s kontrolou správnosti
 * původního hesla.
 *
 * Anotace pro bean validaci zajišťují základní kontrolu povinnosti
 * polí a minimální délky nového hesla. Další pravidla bezpečnosti
 * hesla se případně uplatňují v service vrstvě.
 */
public class ChangePasswordDTO {

    /**
     * Původní heslo přihlášeného uživatele.
     *
     * Slouží k ověření, že změnu hesla provádí skutečný držitel účtu.
     */
    @NotBlank(message = "Původní heslo je povinné.")
    private String oldPassword;

    /**
     * Nové heslo, které má být nastaveno.
     *
     * Musí splňovat minimální požadovanou délku a případné další
     * bezpečnostní požadavky kontrolované v servisní vrstvě.
     */
    @NotBlank(message = "Nové heslo je povinné.")
    @Size(min = 8, max = 64, message = "Nové heslo musí mít 8–64 znaků.")
    private String newPassword;

    /**
     * Potvrzení nového hesla.
     *
     * Používá se pro ověření, že uživatel nezadal nové heslo chybně.
     * Kontrola shody s newPassword probíhá v servisní vrstvě.
     */
    @NotBlank(message = "Potvrzení nového hesla je povinné.")
    private String newPasswordConfirm;



    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getNewPasswordConfirm() { return newPasswordConfirm; }
    public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }
}