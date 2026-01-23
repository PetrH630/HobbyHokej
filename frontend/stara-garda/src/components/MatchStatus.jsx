// src/components/MatchStatus.jsx
import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
} from "../icons";

const statusTextUpcomingMap = {
    REGISTERED: "přihlášen",
    UNREGISTERED: "odhlášen",
    EXCUSED: "omluven",
    RESERVED: "náhradník",
    NO_RESPONSE: "nepřihlášen",
    NO_EXCUSED: "neomluven",
};

const statusTextPastMap = {
    REGISTERED: "byl",
    UNREGISTERED: "nebyl",
    EXCUSED: "nemohl",
    RESERVED: "náhradník",
    NO_RESPONSE: "bez reakce",
    NO_EXCUSED: "nepřišel",
};

const statusIconMap = {
    REGISTERED: RegisteredIcon,
    UNREGISTERED: UnregisteredIcon,
    EXCUSED: ExcusedIcon,
    RESERVED: ReservedIcon,
    NO_RESPONSE: NoResponseIcon,
    NO_EXCUSED: NoExcusedIcon,
};

const MatchStatus = ({ status, variant = "upcoming" }) => {
    const StatusIcon = statusIconMap[status];
    const textMap =
        variant === "past" ? statusTextPastMap : statusTextUpcomingMap;
    const statusText = textMap[status] ?? status;

    return (
        <div className="text-center mb-3">
            <span className={`match-status status-${status}`}>
                {StatusIcon && <StatusIcon className="status-icon" />}
                <strong className="status-text">{statusText}</strong>
            </span>
        </div>
    );
};

export default MatchStatus;
