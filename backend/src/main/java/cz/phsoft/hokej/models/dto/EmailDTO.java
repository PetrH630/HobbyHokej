package cz.phsoft.hokej.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pro zadání emailu (např. zapomenuté heslo).
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
