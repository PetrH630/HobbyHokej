import "./PlayerMatchStatus.css";

import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
} from "../../icons";


const PLAYER_MATCH_STATUS_ICON_MAP = {
    REGISTERED: RegisteredIcon,
    UNREGISTERED: UnregisteredIcon,
    EXCUSED: ExcusedIcon,
    SUBSTITUTE: ExcusedIcon,
    RESERVED: ReservedIcon,
    NO_RESPONSE: NoResponseIcon,
    NO_EXCUSED: NoExcusedIcon,
};


const PLAYER_MATCH_STATUS_TEXT_UPCOMING = {
    REGISTERED: "přihlášen",
    UNREGISTERED: "odhlášen",
    EXCUSED: "omluven",
    SUBSTITUTE: "možná",
    RESERVED: "náhradník",
    NO_RESPONSE: "nepřihlášen",
    NO_EXCUSED: "neomluven",
};


const PLAYER_MATCH_STATUS_TEXT_PAST = {
    REGISTERED: "byl jsem",
    UNREGISTERED: "nebyl jsem",
    EXCUSED: "nemohl jsem",
    SUBSTITUTE: "nebyl jsem",
    RESERVED: "byl jsem náhradník",
    NO_RESPONSE: "nereagoval jsem",
    NO_EXCUSED: "nepřišel jsem",
};

/**
 * PlayerMatchStatus
 *
 * Komponenta související se zápasy, registracemi a jejich zobrazením.
 *
 * Props:
 * @param {string} props.playerMatchStatus data vybraného zápasu.
 * @param {Object} props.variant vstupní hodnota komponenty. [default: "upcoming"]
 */
const PlayerMatchStatus = ({
    playerMatchStatus,
    variant = "upcoming",
}) => {

    const normalizedStatus = playerMatchStatus ?? "NO_RESPONSE";

    const StatusIcon = PLAYER_MATCH_STATUS_ICON_MAP[normalizedStatus];

    const textMap =
        variant === "past"
            ? PLAYER_MATCH_STATUS_TEXT_PAST
            : PLAYER_MATCH_STATUS_TEXT_UPCOMING;

    const text = textMap[normalizedStatus] ?? normalizedStatus;

    const modifier = normalizedStatus.toLowerCase();

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