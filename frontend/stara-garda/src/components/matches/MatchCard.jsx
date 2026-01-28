// src/components/MatchCard.jsx
import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
    UserIcon,
    MoneyIcon,
} from "../../icons";

import "./MatchCard.css";

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
    NO_RESPONSE: "NEVÍM",
    NO_EXCUSED: "NEOMLUVEN",
};

const statusTextPastMap = {
    REGISTERED: "",
    NO_RESPONSE: "NEREAGOVAL",
    NO_EXCUSED: "NEPŘIŠEL",
    UNREGISTERED: "ODHLÁŠEN",
    EXCUSED: "NEMOHL",
    RESERVED: "ČEKAL",
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
        dateTime: `${datePart} ${"  "} ${timePart}`,
    };
};

const MatchCard = ({ match, onClick, disabledTooltip, condensed = false }) => {
    // 🔹 vždy pracujme s jednou proměnnou
    const playerMatchStatus = match.playerMatchStatus ?? "NO_RESPONSE";

    const statusText = condensed
        ? statusTextPastMap[playerMatchStatus] ?? playerMatchStatus
        : statusTextUpcomingMap[playerMatchStatus] ?? playerMatchStatus;

    const statusClass = statusClassMap[playerMatchStatus] ?? "";
    const StatusIcon = statusIconMap[playerMatchStatus] || null;

    const hasDisabledTooltip = !!disabledTooltip;
    const isRegistered = playerMatchStatus === "REGISTERED";

    const isDisabledByTooltip = hasDisabledTooltip && !isRegistered;
    const isClickable = !!onClick && !isDisabledByTooltip;

    const formatted = formatDateTime(match.dateTime);

    // pro BEM modifier – REGISTERED -> "registered", NO_RESPONSE -> "no_response"
    const statusModifier = playerMatchStatus.toLowerCase();

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
                            {formatted.day} {" - "} {match.matchNumber}
                        </h4>

                        <h5 className="text-center mb-2 match-date">
                            {formatted.dateTime}
                        </h5>
                    </>
                )}

                <p className="card-text text-center mb-3">
                    <strong>{match.location}</strong>
                </p>

                {!condensed && match.description && (
                    <p className="card-text mb-1">
                        Popis: <strong>{match.description}</strong>
                    </p>
                )}

                <p className="card-text text-center mb-3">
                    <UserIcon className="player-icon" />
                    <strong>
                        {match.inGamePlayers} / {match.maxPlayers}
                    </strong>
                </p>

                {!condensed && (
                    <p className="card-text text-center mb-1 mt-2">
                        <MoneyIcon className="money-icon" />
                        <strong>
                            {match.pricePerRegisteredPlayer.toFixed(0)} Kč /{" "}
                            <UserIcon className="player-price-icon" /> Kč
                        </strong>
                    </p>
                )}

                {/* 🔹 Stav hráče v zápase – ikona + text */}
                <div className="text-center mt-3">
                    <span
                        className={`player-match-status player-match-status--${statusModifier}`}
                    >
                        {StatusIcon && (
                            <StatusIcon className="player-match-status-icon" />
                        )}
                        <strong className="player-match-status-text">
                            {statusText}
                        </strong>
                    </span>
                </div>
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
