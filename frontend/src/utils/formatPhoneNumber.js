/**
 * formatPhoneNumber
 *
 * Normalizuje a formátuje telefonní číslo pro zobrazení v UI.
 * Používá se v administraci i v profilu hráče, aby se číslo zobrazovalo konzistentně.
 */
export const formatPhoneNumber = (phone) => {
    if (!phone) return "";

    // odstraní mezery, +, pomlčky
    const digits = phone.replace(/\D/g, "");

    // 12 číslic (např. 420123456789)
    if (digits.length === 12) {
        return digits.replace(
            /(\d{3})(\d{3})(\d{3})(\d{3})/,
            "$1 $2 $3 $4"
        );
    }

    // 9 číslic (např. 123456789)
    if (digits.length === 9) {
        return digits.replace(
            /(\d{3})(\d{3})(\d{3})/,
            "$1 $2 $3"
        );
    }

    // fallback – vrátí původní
    return phone;
};
