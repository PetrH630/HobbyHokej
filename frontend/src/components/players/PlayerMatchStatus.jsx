import "./PlayerMatchStatus.css";

import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
} from "../../icons";

/**
 * Ikony pro STAV HRÁČE v zápase
 */
const PLAYER_MATCH_STATUS_ICON_MAP = {
    REGISTERED: RegisteredIcon,
    UNREGISTERED: UnregisteredIcon,
    EXCUSED: ExcusedIcon,
    SUBSTITUTE: ExcusedIcon,
    RESERVED: ReservedIcon,
    NO_RESPONSE: NoResponseIcon,
    NO_EXCUSED: NoExcusedIcon,
};

/**
 * Texty – nadcházející zápas
 */
const PLAYER_MATCH_STATUS_TEXT_UPCOMING = {
    REGISTERED: "přihlášen",
    UNREGISTERED: "odhlášen",
    EXCUSED: "omluven",
    SUBSTITUTE: "možná",
    RESERVED: "náhradník",
    NO_RESPONSE: "nepřihlášen",
    NO_EXCUSED: "neomluven",
};

/**
 * Texty – uplynulý zápas
 */
const PLAYER_MATCH_STATUS_TEXT_PAST = {
    REGISTERED: "byl jsem",
    UNREGISTERED: "nebyl jsem",
    EXCUSED: "nemohl jsem",
    SUBSTITUTE: "nebyl jsem",
    RESERVED: "byl jsem náhradník",
    NO_RESPONSE: "nereagoval jsem",
    NO_EXCUSED: "nepřišel jsem",
};

const PlayerMatchStatus = ({
    playerMatchStatus,
    variant = "upcoming",
}) => {
    // 🔹 sjednocení – vždy pracujeme s jednou proměnnou
    const normalizedStatus = playerMatchStatus ?? "NO_RESPONSE";

    const StatusIcon = PLAYER_MATCH_STATUS_ICON_MAP[normalizedStatus];

    const textMap =
        variant === "past"
            ? PLAYER_MATCH_STATUS_TEXT_PAST
            : PLAYER_MATCH_STATUS_TEXT_UPCOMING;

    const text = textMap[normalizedStatus] ?? normalizedStatus;

    const modifier = normalizedStatus.toLowerCase(); // REGISTERED -> registered, NO_RESPONSE -> no_response

    return (
        <div className="text-center mb-3">
            <span
                className={`
                    player-match-status
                    player-match-status--${modifier}
                `}
            >
                {StatusIcon && (
                    <StatusIcon className="player-match-status-icon" />
                )}
                <strong className="player-match-status-text">
                    {text}
                </strong>
            </span>
        </div>
    );
};

export default PlayerMatchStatus;
