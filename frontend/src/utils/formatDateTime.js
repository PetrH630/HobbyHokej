/**
 * formatDateTime
 *
 * Pomocná funkce pro formátování data a času do českého formátu.
 * Vrací prázdný řetězec, pokud je vstup prázdný nebo neplatný.
 */
export const formatDateTime = (value) => {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "";
    
    return date.toLocaleString("cs-CZ", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
};

