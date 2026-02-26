// src/components/PlayerCard.jsx
import RoleGuard from "../RoleGuard";
import { PhoneIcon, TeamDarkIcon, TeamLightIcon, PlayerIcon } from "../../icons";
import { formatPhoneNumber } from "../../utils/formatPhoneNumber";
import "./PlayerCard.css";
import { getPlayerPositionLabel } from "../../constants/playerPosition";

const statusClassMap = {
    APPROVED: "player-approved",
    PENDING: "player-pending",
    REJECTED: "player-rejected",
};

const statusTextMap = {
    PENDING: "čeká na schválení",
    REJECTED: "zamítnuto",
    APPROVED: "schváleno",
};

const PlayerCard = ({ player, onSelect, isActive, disabledTooltip }) => {
    const playerStatus = player.playerStatus ?? "PENDING";

    const statusClass = statusClassMap[playerStatus] || "";
    const statusText = statusTextMap[playerStatus] ?? playerStatus;

    const isApproved = playerStatus === "APPROVED";
    const isDarkTeam = player.team === "DARK";

    const hasDisabledTooltip = !!disabledTooltip;
    const isDisabled = !isApproved && hasDisabledTooltip;

    const isClickable = isApproved && !!onSelect && !isDisabled;

    // Primární pozice hráče – mapování přes konstantu
    const primaryPositionLabel = getPlayerPositionLabel(
        player.primaryPosition
    );

    return (
        <div
            className={[
                "player-card",
                statusClass,
                isClickable ? "clickable" : "",
                isDisabled ? "player-card--disabled" : "",
                isActive ? "player-card--active" : "",
            ]
                .filter(Boolean)
                .join(" ")}
            role={isClickable ? "button" : undefined}
            tabIndex={isClickable ? 0 : -1}
            onClick={isClickable ? onSelect : undefined}
            onKeyDown={
                isClickable
                    ? (e) => e.key === "Enter" && onSelect()
                    : undefined
            }
        >
            {/* Indikátor aktivního hráče v pravém horním rohu */}
            <PlayerIcon
                className={`active-indicator ${isActive ? "active" : "inactive"
                    }`}
            />

            <div className="card-body">
                <h5 className="card-title mb-4 mt-3 text-center">
                    {player.fullName}
                </h5>

                <div className="mb-2 text-center">
                    <div
                        className={`team-icon-wrapper ${isDarkTeam ? "team-dark" : "team-light"
                            }`}
                    >
                        <TeamDarkIcon className="team-icon base" />
                        <TeamLightIcon className="team-icon overlay" />
                    </div>
                </div>

                {/* Primární pozice hráče – viditelná všem */}
                {player.primaryPosition && (
                    <p className="card-text text-center mb-2">
                        <strong>Post:</strong>{" "}
                        {primaryPositionLabel}
                    </p>
                )}

                <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                    <p className="card-text text-center mb-2">
                        <strong>Typ:</strong> {player.type}
                    </p>
                </RoleGuard>

                <p className="card-text text-center mb-2">
                    <strong>Status:</strong> {player.statusText ?? statusText}
                </p>

                <p className="card-text text-center mb-2">
                    <PhoneIcon className="phone-icon" />
                    +{formatPhoneNumber(player.phoneNumber)}
                </p>
            </div>

            {isDisabled && (
                <div className="player-card-tooltip">
                    {disabledTooltip}
                </div>
            )}
        </div>
    );
};

export default PlayerCard;