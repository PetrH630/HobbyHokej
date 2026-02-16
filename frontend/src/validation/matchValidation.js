// src/validation/matchValidation.js

/**
 * Frontend validace pro MatchDTO.
 *
 * Očekávaná pole v values:
 *  - dateTime: buď Date (Flatpickr), nebo string pro <input type="datetime-local"> (YYYY-MM-DDTHH:mm)
 *  - location: místo zápasu
 *  - description: volitelný popis
 *  - maxPlayers: maximální počet hráčů
 *  - price: celková cena zápasu
 */
export const validateMatch = (values) => {
    const errors = {};

    // --- dateTime ---
    const dt = values?.dateTime;

    const isValidDateObject =
        dt instanceof Date && !Number.isNaN(dt.getTime());

    const isValidDateString =
        typeof dt === "string" && dt.trim() !== "";

    if (!dt || (!isValidDateObject && !isValidDateString)) {
        errors.dateTime = "Datum a čas zápasu je povinné.";
    }

    // location – @NotBlank, @Size(3, 70)
    const location = (values.location ?? "").trim();
    if (!location) {
        errors.location = "Místo zápasu je povinné.";
    } else if (location.length < 3 || location.length > 70) {
        errors.location = "Místo musí mít 3 až 70 znaků.";
    }

    // description – @Size(max=255)
    const description = (values.description ?? "").trim();
    if (description.length > 255) {
        errors.description = "Popis může mít maximálně 255 znaků.";
    }

    // maxPlayers – @NotNull + rozumná kontrola > 0
    if (
        values.maxPlayers === null ||
        values.maxPlayers === undefined ||
        values.maxPlayers === ""
    ) {
        errors.maxPlayers = "Maximální počet hráčů je povinný.";
    } else {
        const maxPlayersNum = Number(values.maxPlayers);
        if (!Number.isInteger(maxPlayersNum) || maxPlayersNum <= 0) {
            errors.maxPlayers =
                "Maximální počet hráčů musí být kladné celé číslo.";
        }
    }

    // price – @NotNull + rozumná kontrola ≥ 0
    if (values.price === null || values.price === undefined || values.price === "") {
        errors.price = "Cena je povinná.";
    } else {
        const priceNum = Number(values.price);
        if (!Number.isFinite(priceNum) || priceNum < 0) {
            errors.price = "Cena musí být číslo větší nebo rovno nule.";
        }
    }

    return errors;
};
