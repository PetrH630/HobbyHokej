// src/components/admin/AdminPlayerHistoryCard.jsx
import { formatPhoneNumber } from "../../utils/formatPhoneNumber";
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

const getHighlightClass = (changed) =>
    changed ? "p-1 rounded bg-warning bg-opacity-25" : "";

const AdminPlayerHistoryCard = ({ item, previousItem }) => {
    // Výchozí hodnoty – pokud není previousItem, nic se nezvýrazňuje
    const diffs = {
        nameChanged: false,
        surnameChanged: false,
        nicknameChanged: false,
        phoneChanged: false,
        typeChanged: false,
        teamChanged: false,
        statusChanged: false,
        originalTimestampChanged: false,
        userIdChanged: false,
        playerIdChanged: false,
    };

    if (previousItem) {
        diffs.nameChanged =
            (previousItem.name || "") !== (item.name || "");
        diffs.surnameChanged =
            (previousItem.surname || "") !== (item.surname || "");
        diffs.nicknameChanged =
            (previousItem.nickname || "") !== (item.nickname || "");

        diffs.phoneChanged =
            (previousItem.phoneNumber || "") !== (item.phoneNumber || "");

        diffs.typeChanged =
            (previousItem.type || "") !== (item.type || "");

        diffs.teamChanged =
            (previousItem.team || "") !== (item.team || "");

        diffs.statusChanged =
            (previousItem.playerStatus || "") !==
            (item.playerStatus || "");

        diffs.originalTimestampChanged =
            (previousItem.originalTimestamp || "") !==
            (item.originalTimestamp || "");

        diffs.userIdChanged =
            (previousItem.userId ?? null) !== (item.userId ?? null);

        diffs.playerIdChanged =
            (previousItem.playerId ?? null) !== (item.playerId ?? null);
    }

    return (
        <div className="card mb-2 shadow-sm border border-2 border-secondary-subtle">
            <div className="card-body py-3 px-4">
                <div className="d-flex justify-content-between align-items-start">
                    <div>
                        {/* Jméno + příjmení + přezdívka */}
                        <div
                            className={
                                "fw-bold fs-6 mb-1 " +
                                getHighlightClass(
                                    diffs.nameChanged ||
                                    diffs.surnameChanged ||
                                    diffs.nicknameChanged
                                )
                            }
                        >
                            {item.name} {item.surname?.toUpperCase()}{" "}
                            {item.nickname && (
                                <span className="text-muted">
                                    ({item.nickname})
                                </span>
                            )}
                        </div>

                        {/* Datum změny + akce (neměníme, jen info) */}
                        <div className="small text-muted mb-2">
                            {formatDateTime(item.changedAt)}{" "}
                            {item.action && ` ${item.action}`}
                        </div>

                        {/* Telefon */}
                        <div
                            className={
                                "small mb-1 " +
                                getHighlightClass(diffs.phoneChanged)
                            }
                        >
                            <strong>Tel:</strong>{" "}
                            {formatPhoneNumber(item.phoneNumber) || "-"}
                        </div>

                        {/* Typ hráče */}
                        <div
                            className={
                                "small mb-1 " +
                                getHighlightClass(diffs.typeChanged)
                            }
                        >
                            <strong>Typ:</strong>{" "}
                            {playerTypeLabel(item.type)}
                        </div>

                        {/* Tým */}
                        <div
                            className={
                                "small mb-1 " +
                                getHighlightClass(diffs.teamChanged)
                            }
                        >
                            <strong>Tým:</strong>{" "}
                            {teamLabel(item.team)}
                        </div>

                        {/* Status hráče */}
                        <div
                            className={
                                "small mb-1 " +
                                getHighlightClass(diffs.statusChanged)
                            }
                        >
                            <strong>Status hráče:</strong>{" "}
                            {playerStatusLabel(item.playerStatus)}
                        </div>

                        {/* Původní založení */}
                        {item.originalTimestamp && (
                            <div
                                className={
                                    "small text-muted mt-2 " +
                                    getHighlightClass(
                                        diffs.originalTimestampChanged
                                    )
                                }
                            >
                                <strong>Původní založení:</strong>{" "}
                                <div>
                                    {formatDateTime(item.originalTimestamp)}
                                </div>
                            </div>
                        )}

                        {/* User ID */}
                        <div
                            className={
                                "small text-muted mt-2 " +
                                getHighlightClass(diffs.userIdChanged)
                            }
                        >
                            <strong>User ID:</strong>{" "}
                            {item.userId ?? "-"}
                        </div>

                        {/* Player ID */}
                        <div
                            className={
                                "small text-muted mt-2 " +
                                getHighlightClass(diffs.playerIdChanged)
                            }
                        >
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