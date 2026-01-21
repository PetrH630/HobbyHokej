// src/components/PlayerCard.jsx
import RoleGuard from "./RoleGuard";
import { PhoneIcon } from "../icons";
import { TeamDarkIcon } from "../icons";
import { TeamLightIcon } from "../icons";
import { CurrentPlayerIcon } from "../icons";
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

const PlayerCard = ({ player, onSelect, isActive }) => {
    const statusClass = statusClassMap[player.status] || "";
    const isDarkTeam = player.team === "DARK";
    const isApproved = player.status === "APPROVED";

    return (
        <div
            className={`player-card ${statusClass} ${isApproved ? "clickable" : "disabled"
                }`}
            role={isApproved ? "button" : undefined}
            tabIndex={isApproved ? 0 : -1}
            onClick={isApproved ? onSelect : undefined}
            onKeyDown={
                isApproved
                    ? (e) => e.key === "Enter" && onSelect()
                    : undefined
            }
            title={
                !isApproved
                    ? "Hráč čeká na schválení administrátorem"
                    : undefined
            }
        >
            {/* Indikátor aktivního hráče v pravém horním rohu */}
            <CurrentPlayerIcon
                className={`active-indicator ${isActive ? "active" : "inactive"
                    }`}
            />

            <div className="card-body">
                <h4 className="card-title mb-3 text-center">
                    {player.fullName}
                </h4>

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
                    <p className="card-text mb-2">
                        <strong>Typ:</strong> {player.type}
                    </p>
                </RoleGuard>

                <p className="card-text mb-2">
                    <strong>Status:</strong>{" "}
                    {statusTextMap[player.status] ?? player.status}
                </p>

                <p className="card-text mb-2">
                    <PhoneIcon color="#045ee6ff" /> {player.phoneNumber}
                </p>
            </div>
        </div>
    );
};

export default PlayerCard;
