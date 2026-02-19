// src/utils/formatDate.js
export const formatDate = (value) => {
    if (!value) return "";

    // podporuje i "2026-02-10 10:00:00" -> "2026-02-10T10:00:00"
    const raw =
        typeof value === "string" && !value.includes("T")
            ? value.replace(" ", "T")
            : value;

    const date = new Date(raw);
    if (Number.isNaN(date.getTime())) return "";

    return date.toLocaleDateString("cs-CZ", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
};
