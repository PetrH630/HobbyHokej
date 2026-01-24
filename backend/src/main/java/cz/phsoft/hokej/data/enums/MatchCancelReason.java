package cz.phsoft.hokej.data.enums;

public enum MatchCancelReason {
    NOT_ENOUGH_PLAYERS,     // málo hráčů
    TECHNICAL_ISSUE,        // technické problémy (led, hala, bus…)
    WEATHER,                // počasí
    ORGANIZER_DECISION,     // rozhodnutí organizátora
    OTHER                   // jiný důvod
}

