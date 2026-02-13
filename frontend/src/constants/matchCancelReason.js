// src/constants/matchCancelReason.js

export const MatchCancelReason = {
    NOT_ENOUGH_PLAYERS: "NOT_ENOUGH_PLAYERS",
    TECHNICAL_ISSUE: "TECHNICAL_ISSUE",
    WEATHER: "WEATHER",
    ORGANIZER_DECISION: "ORGANIZER_DECISION",
    OTHER: "OTHER",
};

export const MATCH_CANCEL_REASON_OPTIONS = [
    {
        value: MatchCancelReason.NOT_ENOUGH_PLAYERS,
        label: "Nedostatečný počet hráčů",
    },
    {
        value: MatchCancelReason.TECHNICAL_ISSUE,
        label: "Technické problémy (led, hala, doprava)",
    },
    {
        value: MatchCancelReason.WEATHER,
        label: "Nepříznivé počasí",
    },
    {
        value: MatchCancelReason.ORGANIZER_DECISION,
        label: "Rozhodnutí organizátora",
    },
    {
        value: MatchCancelReason.OTHER,
        label: "Jiný důvod",
    },
];
