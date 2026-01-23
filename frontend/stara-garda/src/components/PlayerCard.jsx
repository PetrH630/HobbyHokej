// src/components/PlayerCard.jsx
import RoleGuard from "./RoleGuard";
import { PhoneIcon } from "../icons";
import { TeamDarkIcon } from "../icons";
import { TeamLightIcon } from "../icons";
import { CurrentPlayerIcon } from "../icons";
import { PlayerIcon } from "../icons";
import { formatPhoneNumber } from "../utils/formatPhoneNumber";
import "./Player.css";

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
    const statusClass = statusClassMap[player.status] || "";
    const isDarkTeam = player.team === "DARK";
    const isApproved = player.status === "APPROVED";
    const isRejected = player.status === "REJECTED";
    

    return (
        <div
            className={`player-card 
                ${statusClass} 
                ${isApproved ? "clickable" : "disabled"} 
                ${isActive ? "player-card--active" : ""}
            `}
            role={isApproved ? "button" : undefined}
            tabIndex={isApproved ? 0 : -1}
            onClick={isApproved ? onSelect : undefined}
            onKeyDown={
                isApproved
                    ? (e) => e.key === "Enter" && onSelect()
                    : undefined
            }
            title={
                isApproved
                    ? undefined : isRejected ? "Hráč nebyl schválen administrátorem" : "Hráč čeká na schválení administrátorem"
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

                <RoleGuard roles={["ROLE_ADMIN"]}>
                    <p className="card-text text-center mb-2">
                        <strong>Typ:</strong> {player.type}
                    </p>
                </RoleGuard>

                <p className="card-text text-center mb-2">
                    <strong>Status:</strong>{" "}
                    {statusTextMap[player.status] ?? player.status}
                </p>

                <p className="card-text text-center mb-2">
                    <PhoneIcon color="#045ee6ff" />  +{formatPhoneNumber(player.phoneNumber)}
                </p>
            </div>
            {isApproved && (
                <div className="player-card-tooltip">
                    {disabledTooltip}
                </div>
            )}
        </div>
    );
};

export default PlayerCard;
