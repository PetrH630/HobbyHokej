// src/components/admin/AdminPlayerHistoryCard.jsx
import { formatPhoneNumber } from "../../utils/formatPhoneNumber"
import { formatDateTime } from "../../utils/formatDateTime";

const playerStatusLabel = (status) => {
    switch (status) {
        case "PENDING":
            return "čeká na schválení";
        case "APPROVED":
            return "schváleno";
        case "REJECTED":
            return "zamítnuto";
        default:
            return status || "-";
    }
};

const playerTypeLabel = (type) => {
    switch (type) {
        case "VIP":
            return "VIP";
        case "STANDARD":
            return "Standartní";
        case "BASIC":
            return "základní";
        default:
            return type || "-";
    }
};

const teamLabel = (team) => {
    switch (team) {
        case "DARK":
            return "DARK";
        case "LIGHT":
            return "LIGHT";
        default:
            return team || "-";
    }
};

const AdminPlayerHistoryCard = ({ item }) => {
    return (
        <div className="card mb-2 shadow-sm border border-2 border-secondary-subtle">
            <div className="card-body py-3 px-4">
                <div className="d-flex justify-content-between align-items-start">

                    <div>
                        <div className="fw-bold fs-6 mb-1">
                            {item.name} {item.surname?.toUpperCase()}{" "}
                            {item.nickname && (
                                <span className="text-muted">
                                    ({item.nickname})
                                </span>
                            )}
                        </div>

                        <div className="small text-muted mb-2">
                            {formatDateTime(item.changedAt)}{" "}
                            {item.action && ` ${item.action}`}
                        </div>


                        <div className="small mb-1">
                            <strong>Tel:</strong>{" "}
                            {formatPhoneNumber(item.phoneNumber) || "-"}
                        </div>

                        <div className="small mb-1">
                            <strong>Typ:</strong>{" "}
                            {playerTypeLabel(item.type)}
                        </div>

                        <div className="small mb-1">
                            <strong>Tým:</strong>{" "}
                            {teamLabel(item.team)}
                        </div>

                        <div className="small mb-1">
                            <strong>Status hráče:</strong>{" "}
                            {playerStatusLabel(item.playerStatus)}
                        </div>

                        {item.originalTimestamp && (
                            <div className="small text-muted mt-2">
                                <strong>Původní založení:</strong>{" "}
                                <div>
                                {formatDateTime(item.originalTimestamp)}
                                </div>
                            </div>
                        )}
                        <div className="small text-muted mt-2">
                            <strong>User ID:</strong>{" "}
                            {item.userId ?? "-"}
                        </div>
                        <div className="small text-muted mt-2">
                            <strong>Player ID:</strong>{" "}
                            {item.playerId ?? "-"}
                        </div>
                    </div>


                </div>
            </div>
        </div>
    );
};

export default AdminPlayerHistoryCard;