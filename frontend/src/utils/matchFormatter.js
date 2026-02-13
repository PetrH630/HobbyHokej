// src/utils/matchFormatter.js

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

export const matchStatusLabel = (status) => {
    if (!status) return "";
    switch (status) {
        case "CANCELED":
            return "Zrušený";
        case "UNCANCELED":
            return "Obnovený";
        case "UPDATED":
            return "Změněný";
        default:
            return status;
    }
};

export const matchCancelReasonLabel = (reason) => {
    if (!reason) return "";
    switch (reason) {
        case "NOT_ENOUGH_PLAYERS":
            return "Nedostatek hráčů";
        case "TECHNICAL_ISSUE":
            return "Technické problémy (led, hala, doprava)";
        case "WEATHER":
            return "Nepříznivé počasí";
        case "ORGANIZER_DECISION":
            return "Rozhodnutí organizátora";
        case "OTHER":
            return "Jiný důvod"
        default:
            return reason;
    }
};

export const matchActionLabel = (action) => {
    if (!action) return "";
    switch (action) {
        case "CREATED":
            return "Vytvoření zápasu";
        case "UPDATED":
            return "Úprava zápasu";
        case "CANCELED":
            return "Zrušení zápasu";
        case "UNCANCELED":
            return "Obnovení zápasu";
        default:
            return action;
    }
};
