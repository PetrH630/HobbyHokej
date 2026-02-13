// src/validation/playerValidation.js

// Frontend validace dle PlayerDTO
export const validatePlayerProfile = (values) => {
    const errors = {};

    // name: @NotBlank + @Size(2, 50)
    if (!values.name || !values.name.trim()) {
        errors.name = "Křestní jméno je povinné.";
    } else if (
        values.name.trim().length < 2 ||
        values.name.trim().length > 50
    ) {
        errors.name = "Křestní jméno musí mít 2 až 50 znaků.";
    }

    // surname: @NotBlank + @Size(2, 50)
    if (!values.surname || !values.surname.trim()) {
        errors.surname = "Příjmení je povinné.";
    } else if (
        values.surname.trim().length < 2 ||
        values.surname.trim().length > 50
    ) {
        errors.surname = "Příjmení musí mít 2 až 50 znaků.";
    }

    // phoneNumber: @Pattern(E.164), ale může být null/"" → kontrolujeme jen pokud je něco vyplněné
    // Navíc chceme, aby měl pro české prostředí celkem ( 12 číslic po +)
    if (values.phoneNumber && values.phoneNumber.trim() !== "") {
        const trimmed = values.phoneNumber.trim();

        // + [1-9] + 8–14 dalších číslic => celkem 9–15 číslic
        const phoneRegex = /^\+[1-9]\d{11}$/;

        if (!phoneRegex.test(trimmed)) {
            errors.phoneNumber =
                "Telefon musí být v mezinárodním formátu a dostatečně dlouhý, např. +420123456789.";
        }
    }


    return errors;
};
