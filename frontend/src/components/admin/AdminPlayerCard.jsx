// src/components/admin/AdminPlayerCard.jsx
import { useState } from "react";
import RoleGuard from "../RoleGuard";
import AdminPlayerHistory from "./AdminPlayerHistory";
import AdminPlayerInactivityModal from "./AdminPlayerInactivityModal";
import { useGlobalDim } from "../../hooks/useGlobalDim";

import PlayerStats from "../players/PlayerStats";
import { usePlayerStatsAdmin } from "../../hooks/usePlayerStatsAdmin";

const statusTextMap = {
    PENDING: "čeká na schválení",
    REJECTED: "zamítnuto",
    APPROVED: "schváleno",
};

const statusBadgeClassMap = {
    PENDING: "bg-warning text-dark",
    REJECTED: "bg-danger",
    APPROVED: "bg-success",
};

const AdminPlayerCard = ({
    player,
    isInactive,
    onApprove,
    onReject,
    onEdit,
    onDelete,
    onChangeUser,
    onInactivityChanged,
}) => {
    const [showHistory, setShowHistory] = useState(false);
    const [showInactivity, setShowInactivity] = useState(false);

    // ✅ NOVĚ – statistiky
    const [showStats, setShowStats] = useState(false);

    // dimujeme když je otevřená historie nebo statistiky
    const dimActive = showHistory || showStats;
    useGlobalDim(dimActive);

    const playerStatus = player.playerStatus ?? "PENDING";
    const statusText = statusTextMap[playerStatus] ?? playerStatus;
    const statusBadgeClass = statusBadgeClassMap[playerStatus] ?? "bg-secondary";

    const user = player.user || null;

    const canApproveByStatus =
        playerStatus === "PENDING" || playerStatus === "REJECTED";
    const canRejectByStatus =
        playerStatus === "PENDING" || playerStatus === "APPROVED";

    const toggleHistory = () => {
        setShowHistory((prev) => !prev);
        // ať nejsou otevřené obě sekce zároveň
        setShowStats(false);
    };

    const toggleStats = () => {
        setShowStats((prev) => !prev);
        setShowHistory(false);
    };

    // ✅ statistiky načítáme jen když jsou otevřené
    const {
        stats,
        loading: statsLoading,
        error: statsError,
        reload: reloadStats,
    } = usePlayerStatsAdmin(player.id, { enabled: showStats });

    const cardClassName =
        "card shadow-sm mb-3 " +
        (dimActive ? "bg-white dim-keep " : "") +
        (showHistory ? "border border-3 border-info " : "") +
        (showStats ? "border border-3 border-primary " : "") +
        (isInactive ? "border-start border-4 border-warning" : "");

    const closeOverlays = () => {
        setShowHistory(false);
        setShowStats(false);
    };

    return (
        <>
            {dimActive && (
                <div
                    className="global-dim-click"
                    onClick={closeOverlays}
                    aria-hidden="true"
                />
            )}

            <div className={cardClassName}>
                {/* === ŘÁDEK 1 – HRÁČ === */}
                <div className="card-body border-bottom">
                    <div className="row align-items-center">
                        <div className="col-md-3 fw-bold">
                            {player.name} {player.surname?.toUpperCase()}
                            {player.nickname && (
                                <span className="text-muted ms-2">
                                    ({player.nickname})
                                </span>
                            )}
                        </div>

                        <div className="col-md-2">
                            <small className="text-muted d-block">Tým</small>
                            {player.team || "-"}
                        </div>

                        <div className="col-md-2">
                            <small className="text-muted d-block">Typ</small>
                            {player.type || "-"}
                        </div>

                        <div className="col-md-3">
                            <small className="text-muted d-block">Status</small>
                            <span className={`badge ${statusBadgeClass}`}>
                                {statusText}
                            </span>
                        </div>

                        <div className="col-md-2">
                            <small className="text-muted d-block">Neaktivita</small>
                            {isInactive ? (
                                <span className="badge bg-dark">NEAKTIVNÍ</span>
                            ) : (
                                <span className="text-muted">aktivní</span>
                            )}
                        </div>
                    </div>
                </div>

                {/* === ŘÁDEK 2 – UŽIVATEL === */}
                <div className="card-body border-bottom bg-light">
                    {user ? (
                        <div className="row">
                            <div className="col-md-4">
                                <small className="text-muted d-block">
                                    Uživatel
                                </small>
                                {user.name} {user.surname}
                            </div>

                            <div className="col-md-4">
                                <small className="text-muted d-block">
                                    E-mail
                                </small>
                                {user.email}
                            </div>

                            <div className="col-md-4">
                                <small className="text-muted d-block">
                                    ID uživatele
                                </small>
                                {user.id}
                            </div>
                        </div>
                    ) : (
                        <span className="text-muted">
                            Hráč nemá přiřazeného uživatele.
                        </span>
                    )}
                </div>

                {/* === ŘÁDEK 3 – AKCE === */}
                <div className="card-footer bg-white">
                    <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                        <div className="d-flex justify-content-end">
                            <div className="btn-group btn-group-sm flex-wrap">
                                <button
                                    type="button"
                                    className="btn btn-success"
                                    disabled={!onApprove || !canApproveByStatus}
                                    onClick={() => onApprove && onApprove(player.id)}
                                >
                                    Schválit
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-warning"
                                    disabled={!onReject || !canRejectByStatus}
                                    onClick={() => onReject && onReject(player.id)}
                                >
                                    Zamítnout
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-primary"
                                    disabled={!onEdit}
                                    onClick={() => onEdit && onEdit(player)}
                                >
                                    Upravit
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-outline-primary"
                                    disabled={!onChangeUser}
                                    onClick={() => onChangeUser && onChangeUser(player)}
                                >
                                    Převést
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-outline-info"
                                    onClick={() => setShowInactivity(true)}
                                >
                                    Neaktivita
                                </button>

                                {/* ✅ NOVÉ – STATISTIKY */}
                                <button
                                    type="button"
                                    className={
                                        "btn btn-outline-primary" +
                                        (showStats ? " active" : "")
                                    }
                                    onClick={toggleStats}
                                >
                                    {showStats ? "Skrýt statistiku" : "Statistika"}
                                </button>

                                <button
                                    type="button"
                                    className={
                                        "btn btn-outline-secondary" +
                                        (showHistory ? " active" : "")
                                    }
                                    onClick={toggleHistory}
                                >
                                    {showHistory ? "Skrýt historii" : "Historie"}
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-danger"
                                    disabled={!onDelete}
                                    onClick={() => onDelete && onDelete(player.id)}
                                >
                                    Smazat
                                </button>
                            </div>
                        </div>
                    </RoleGuard>
                </div>

                {showStats && (
                    <div className="card-body bg-white">
                        <div className="d-flex justify-content-between align-items-center mb-2">
                            <h6 className="mb-0">Statistiky hráče #{player.id}</h6>
                            <button
                                type="button"
                                className="btn btn-sm btn-outline-secondary"
                                onClick={reloadStats}
                                disabled={statsLoading}
                                title="Obnovit statistiky"
                            >
                                Obnovit
                            </button>
                        </div>

                        <PlayerStats
                            stats={stats}
                            loading={statsLoading}
                            error={statsError}
                            onReload={reloadStats}
                        />
                    </div>
                )}

                {showHistory && (
                    <div className="card-body bg-white">
                        <h6 className="mb-2">Historie hráče #{player.id}</h6>
                        <AdminPlayerHistory playerId={player.id} />
                    </div>
                )}

                {showInactivity && (
                    <AdminPlayerInactivityModal
                        player={player}
                        onClose={() => setShowInactivity(false)}
                        onSaved={() => {
                            setShowInactivity(false);
                            onInactivityChanged && onInactivityChanged();
                        }}
                    />
                )}
            </div>
        </>
    );
};

export default AdminPlayerCard;
