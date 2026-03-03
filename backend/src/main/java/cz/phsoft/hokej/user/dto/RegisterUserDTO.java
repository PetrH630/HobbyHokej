package cz.phsoft.hokej.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pro registraci nového uživatele.
 *
 * Slouží k přenosu registračních údajů z klienta do backendu
 * při vytváření nového uživatelského účtu. DTO obsahuje základní
 * validační pravidla pro formát dat, jako je povinnost vyplnění,
 * minimální délka hesla a správný formát e-mailu.
 *
 * Kontrola shody hesel, ověření jedinečnosti e-mailu a další
 * business validace se provádí v servisní vrstvě.
 */
public class RegisterUserDTO {

    /**
     * Křestní jméno nového uživatele.
     *
     * Musí splňovat minimální a maximální délku definovanou anotacemi.
     */
    @NotBlank(message = "Křestní jméno je povinné.")
    @Size(min = 2, max = 50)
    private String name;

    /**
     * Příjmení nového uživatele.
     *
     * Musí splňovat minimální a maximální délku definovanou anotacemi.
     */
    @NotBlank(message = "Příjmení je povinné.")
    @Size(min = 2, max = 50)
    private String surname;

    /**
     * E-mail nového uživatele.
     *
     * Slouží jako přihlašovací identifikátor. Formát je kontrolován
     * anotací Email a dalšími pravidly v servisní vrstvě.
     */
    @NotBlank(message = "Email je povinný.")
    @Email
    private String email;

    /**
     * Heslo nového uživatele.
     *
     * Musí splňovat minimální délku. Další bezpečnostní pravidla,
     * jako je složitost hesla, se případně kontrolují v servisní vrstvě.
     */
    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    /**
     * Potvrzení hesla.
     *
     * Používá se pro ověření, že uživatel zadal heslo správně.
     * Kontrola shody s password probíhá v servisní vrstvě.
     */
    @NotBlank
    private String passwordConfirm;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
}