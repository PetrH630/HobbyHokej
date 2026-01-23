import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
    AdminIcon,
    UserIcon,
    MoneyIcon
} from "../icons";



import "./MatchCard.css";

const statusTextMap = {
    REGISTERED: "přihlášen",
    UNREGISTERED: "odhlášen",
    EXCUSED: "omluven",
    RESERVED: "náhradník",
    NO_RESPONSE: "nepřihlášen",
    NO_EXCUSED: "neomluven",
};

const statusClassMap = {
    REGISTERED: "match-registered",
    UNREGISTERED: "match-unregistered",
    EXCUSED: "match-excused",
    RESERVED: "match-reserved",
    NO_RESPONSE: "match-no-response",
    NO_EXCUSED: "match-no-excused",
};

const statusTextUpcomingMap = {
    REGISTERED: "BUDU",
    UNREGISTERED: "ODHLÁŠEN",
    EXCUSED: "NEMŮŽU",
    RESERVED: "ČEKÁM",
    NO_RESPONSE: "NEREAGUJI",
    NO_EXCUSED: "neomluven",
};

const statusTextPastMap = {
    REGISTERED: "BYL JSEM",
    NO_RESPONSE: "NEREAGOVAL",
    NO_EXCUSED: "NEPŘIŠEL",
    UNREGISTERED: "ODHLÁŠEN",
    EXCUSED: "NEMOHL",
    RESERVED: "ČEKAL", // nebo klidně "čekal"
};

const statusIconMap = {
    REGISTERED: RegisteredIcon,
    UNREGISTERED: UnregisteredIcon,
    EXCUSED: ExcusedIcon,
    RESERVED: ReservedIcon,
    NO_RESPONSE: NoResponseIcon,
    NO_EXCUSED: NoExcusedIcon,
};

const formatDateTime = (dateTime) => {
    if (!dateTime) return null;

    const iso = dateTime.replace(" ", "T");
    const d = new Date(iso);

    const dayName = new Intl.DateTimeFormat("cs-CZ", {
        weekday: "long",
    }).format(d);

    const datePart = new Intl.DateTimeFormat("cs-CZ", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    }).format(d);

    const timePart = d.toLocaleTimeString("cs-CZ", {
        hour: "2-digit",
        minute: "2-digit",
    });

    return {
        day: dayName.charAt(0).toUpperCase() + dayName.slice(1),
        dateTime: `${datePart} · ${timePart}`,
    };
};

// ⬇⬇⬇ přidaný prop `condensed = false`
const MatchCard = ({ match, onClick, disabledTooltip, condensed = false }) => {
    const statusText = condensed
        ? statusTextPastMap[match.status] ?? match.status
        : statusTextUpcomingMap[match.status] ?? match.status;
    const statusClass = statusClassMap[match.status] ?? "";
    const StatusIcon = statusIconMap[match.status];

    const hasDisabledTooltip = !!disabledTooltip;
    const isRegistered = match.status === "REGISTERED";

    const isDisabledByTooltip = hasDisabledTooltip && !isRegistered;
    const isClickable = !!onClick && !isDisabledByTooltip;
    const formatted = formatDateTime(match.dateTime);

    const handleClick = () => {
        if (isClickable && onClick) {
            onClick();
        }
    };

    return (
        <div
            className={`match-card ${statusClass} ${isClickable ? "clickable" : ""
                } ${isDisabledByTooltip ? "match-card--disabled" : ""}`}
            role={isClickable ? "button" : undefined}
            tabIndex={isClickable ? 0 : -1}
            onClick={isClickable ? handleClick : undefined}
            onKeyDown={
                isClickable
                    ? (e) => e.key === "Enter" && handleClick()
                    : undefined
            }
        >
            <div className="card-body">
            
                {formatted && (
                    <>
                        <h4 className="card-title text-muted text-center mb-3 match-day">
                            {formatted.day} {" - "} {match.id}
                        </h4>

                        <h5 className="text-center mb-2 match-date">
                            {formatted.dateTime}
                        </h5>
                    </>
                )}

                <p className="card-text text-center mb-3">
                    <strong>{match.location}</strong>
                </p>


                {/* Popis jen pokud NEjsme v condensed režimu */}
                {!condensed && match.description && (
                    <p className="card-text mb-1">
                        Popis:{" "}
                        <strong>{match.description}</strong>
                    </p>
                )}

                <p className="card-text text-center mb-3">
                    <UserIcon className="player-icon" />
                    <strong>
                        {match.inGamePlayers} / {match.maxPlayers}
                    </strong>
                </p>

                {/*Cena zápasu jen v plné verzi 
                {!condensed && match.price != null && (
                    <p className="card-text text-center mb-1">
                        Cena zápasu:{" "}
                        {match.price} Kč
                    </p>
                )}
                */}

                {/* Cena / hráč jen v plné verzi */}
                {!condensed && (
                    <p className="card-text text-center mb-1 mt-2">
                        <MoneyIcon className="money-icon" />
                        <strong>
                            {match.pricePerRegisteredPlayer.toFixed(0)} Kč {" "}/ {" "}
                            <UserIcon className="player-price-icon" /> Kč
                        </strong>
                    </p>
                )}

                <span className={`match-status status-${match.status}`}>
                    {StatusIcon && <StatusIcon className="status-icon" />}
                    <strong className="status-text">{statusText}</strong>
                </span>


            </div>

            {isDisabledByTooltip && (
                <div className="match-card-tooltip">
                    {disabledTooltip}
                </div>
            )}
        </div>
    );
};

export default MatchCard;
