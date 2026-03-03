package cz.phsoft.hokej.notifications.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pro přenos e-mailové adresy.
 *
 * Používá se v jednoduchých formulářích,
 * kde je zadávána pouze e-mailová adresa,
 * například při žádosti o reset hesla.
 *
 * Validace je zajištěna pomocí Bean Validation anotací.
 */
public class EmailDTO {

    @NotBlank(message = "Email je povinný.")
    @Email(message = "Email nemá platný formát.")
    private String email;

    /**
     * Vrací e-mailovou adresu.
     *
     * @return e-mailová adresa
     */
    public String getEmail() {
        return email;
    }

    /**
     * Nastavuje e-mailovou adresu.
     *
     * @param email e-mailová adresa
     */
    public void setEmail(String email) {
        this.email = email;
    }
}