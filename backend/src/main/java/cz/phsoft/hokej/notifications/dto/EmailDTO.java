package cz.phsoft.hokej.notifications.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pro zadání e-mailové adresy.
 *
 * Používá se u jednoduchých formulářů, kde uživatel zadává pouze email,
 * například při požadavku na reset zapomenutého hesla.
 */
public class EmailDTO {

    @NotBlank(message = "Email je povinný.")
    @Email(message = "Email nemá platný formát.")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
