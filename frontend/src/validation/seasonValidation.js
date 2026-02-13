// src/validation/seasonValidation.js

/**
 * Frontend validace pro SeasonDTO.
 *
 * @param {object} values - hodnoty formuláře (name, startDate, endDate, id)
 * @param {Array} allSeasons - seznam existujících sezón (SeasonDTO[])
 * @returns {object} errors - mapování field -> message
 */
export const validateSeason = (values, allSeasons = []) => {
    const errors = {};

    const rawName = values.name ?? "";
    const name = rawName.trim();
    const startDate = values.startDate;
    const endDate = values.endDate;
    const currentId = values.id ?? null;

    // 1) Název
    if (!name) {
        errors.name = "Název sezóny je povinný (např. 2025/2026).";
    }

    // 2) Datum od / do vyplněno
    if (!startDate) {
        errors.startDate = "Datum začátku sezóny je povinné.";
    }
    if (!endDate) {
        errors.endDate = "Datum konce sezóny je povinné.";
    }

    // Když chybí některé datum, nemá smysl pokračovat dál
    if (!startDate || !endDate) {
        // ale ještě můžeme zkontrolovat duplicitní název
        checkDuplicateName(name, currentId, allSeasons, errors);
        return errors;
    }

    // 3) start < end (stejně jako na backendu: start.isBefore(end))
    const start = new Date(startDate);
    const end = new Date(endDate);

    if (!(start < end)) {
        const msg = "Datum 'od' musí být dříve než datum 'do'.";
        errors.startDate = errors.startDate || msg;
        errors.endDate = errors.endDate || msg;
    }

    // 4) Překryv s jinými sezónami
    // backend: existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start)
    // => otherStart <= end && otherEnd >= start
    const overlaps = allSeasons.some((season) => {
        if (!season.startDate || !season.endDate) {
            return false;
        }

        const otherStart = new Date(season.startDate);
        const otherEnd = new Date(season.endDate);

        // u update ignorujeme právě editovanou sezónu
        if (season.id != null && currentId != null && season.id === currentId) {
            return false;
        }

        return otherStart <= end && otherEnd >= start;
    });

    if (overlaps) {
        const msg = "Sezóna se překrývá s existující sezónou.";
        errors.startDate = errors.startDate || msg;
        errors.endDate = errors.endDate || msg;
    }

    // 5) Duplicitní název sezóny
    checkDuplicateName(name, currentId, allSeasons, errors);

    return errors;
};

/**
 * Zkontroluje duplicitní název sezóny.
 *
 * - ignoruje aktuální sezónu podle id (aby šlo uložit beze změny názvu)
 */
const checkDuplicateName = (name, currentId, allSeasons, errors) => {
    if (!name) return;

    const normalizedName = name.toLowerCase();

    const hasDuplicateName = allSeasons.some((season) => {
        if (!season.name) return false;

        const seasonName = season.name.trim().toLowerCase();

        // u editace ignorujeme tuto konkrétní sezónu
        if (season.id != null && currentId != null && season.id === currentId) {
            return false;
        }

        return seasonName === normalizedName;
    });

    if (hasDuplicateName) {
        errors.name =
            errors.name ||
            "Sezóna s tímto názvem již existuje. Zvol jiný název.";
    }
};
