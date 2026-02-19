// src/components/admin/AdminPlayerCard.jsx
import { useState } from "react";
import RoleGuard from "../RoleGuard";
import AdminPlayerHistory from "./AdminPlayerHistory";
import AdminPlayerInactivityModal from "./AdminPlayerInactivityModal";
import { useGlobalDim } from "../../hooks/useGlobalDim";
import ConfirmActionModal from "../common/ConfirmActionModal";
import PlayerStats from "../players/PlayerStats";
import { usePlayerStatsAdmin } from "../../hooks/usePlayerStatsAdmin";
import { formatPhoneNumber } from "../../utils/formatPhoneNumber";
import { formatDateTime } from "../../utils/formatDateTime";
import {
    Check2Circle,
    XCircle,
    PencilSquare,
    PersonArmsUp,
    ClockHistory,
    BarChartLine,
    Trash3,
} from "react-bootstrap-icons";

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
    const [showStats, setShowStats] = useState(false);
    const [confirmAction, setConfirmAction] = useState(null);

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
             setShowStats(false);
    };

    const toggleStats = () => {
        setShowStats((prev) => !prev);
        setShowHistory(false);
    };

    
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
 

    const fullName = `${player.name || ""} ${player.surname?.toUpperCase() || ""}`.trim();

    
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
                    <div className="row align-items-center g-2">
                        {/* Jméno + meta */}
                        <div className="col-md-4">
                            <div className="fw-bold">
                                {fullName}
                                {player.nickname && (
                                    <span className="text-muted ms-2">
                                        ({player.nickname})
                                    </span>
                                )}
                            </div>

                            <div className="text-muted small">
                                ID: {player.id ?? "-"}
                                {"  "}
                                tel: +<strong>{formatPhoneNumber(player.phoneNumber) || "-"}</strong>
                            </div>
                        </div>

                        {/* Tým */}
                        <div className="col-md-2">
                            <div className="text-muted small">Tým: {" "}<strong>{player.team || "-"}</strong></div>
                        </div>

                        {/* Typ */}
                        <div className="col-md-2">
                            <div className="text-muted small">Typ: {" "}{player.type || "-"}</div>
                        </div>

                        {/* Status a Inaktivita badge bez labelu */}
                        <div className="col-md-1 text-center d-flex flex-column gap-1 align-items-center">
                            <span className={`badge ${statusBadgeClass}`}>
                                {statusText}
                            </span>
                            {isInactive ? (
                                <span className="badge bg-dark">NEAKTIVNÍ</span>
                            ) : (
                                <span className="badge bg-success">AKTIVNÍ</span>
                            )}
                        </div>

                        {/* Aktivní / Neaktivní + od */}
                        <div className="col-md-3">
                            
                            <div className="text-muted text-center small mt-1">od: <strong>{formatDateTime(player.timestamp)}</strong></div>
                        </div>
                    </div>
                </div>

                {/* === ŘÁDEK 2 – UŽIVATEL === */}
                <div className="card-body border-bottom bg-light">
                    {user ? (
                        <div className="row align-items-center g-2">
                            <div className="col-md-4">
                                <span className="text-muted small">Uživatel id { user.id }: </span>
                                <span className="fw-semibold">
                                    {user.name} {user.surname}
                                </span>
                            </div>

                            <div className="col-md-4">
                                <span className="text-muted small">email: </span>
                                <span>{user.email}</span>
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
                        <div className="d-flex justify-content-end gap-2 flex-wrap">

                            {/* Primární akce */}
                            <div className="btn-group btn-group-sm" role="group" aria-label="Primární akce">
                                <button
                                    type="button"
                                    className="btn btn-success"
                                    disabled={!onApprove || !canApproveByStatus}
                                    onClick={() =>
                                        setConfirmAction({
                                            type: "approve",
                                            title: "Potvrzení schválení",
                                            message: `Opravdu chcete schválit hráče: ${fullName}?`,
                                        })
                                    }
                                    title="Schválit hráče"
                                >
                                    <Check2Circle className="me-1" />
                                    <span className="d-none d-md-inline">Schválit</span>
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-outline-danger"
                                    disabled={!onReject || !canRejectByStatus}
                                    onClick={() =>
                                        setConfirmAction({
                                            type: "reject",
                                            title: "Potvrzení zamítnutí",
                                            message: `Opravdu chcete zamítnout hráče: ${fullName}?`,
                                        })
                                    }
                                    title="Zamítnout hráče"
                                >
                                    <XCircle className="me-1" />
                                    <span className="d-none d-md-inline">Zamítnout</span>
                                </button>
                            </div>

                            {/* Správa */}
                            <div className="btn-group btn-group-sm" role="group" aria-label="Správa">
                                <button
                                    type="button"
                                    className="btn btn-primary"
                                    disabled={!onEdit}
                                    onClick={() => onEdit && onEdit(player)}
                                    title="Upravit údaje hráče"
                                >
                                    <PencilSquare className="me-1" />
                                    <span className="d-none d-md-inline">Upravit</span>
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-outline-primary"
                                    disabled={!onChangeUser}
                                    onClick={() => onChangeUser && onChangeUser(player)}
                                    title="Převést hráče na jiného uživatele"
                                >
                                    <PersonArmsUp className="me-1" />
                                    <span className="d-none d-md-inline">Převést</span>
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-warning"
                                    onClick={() => setShowInactivity(true)}
                                    title="Nastavit neaktivitu"
                                >
                                    <ClockHistory className="me-1" />
                                    <span className="d-none d-md-inline">Neaktivita</span>
                                </button>
                            </div>

                            {/* Přehledy */}
                            <div className="btn-group btn-group-sm" role="group" aria-label="Přehledy">
                                <button
                                    type="button"
                                    className={"btn btn-outline-primary" + (showStats ? " active" : "")}
                                    onClick={toggleStats}
                                    title="Zobrazit statistiky hráče"
                                >
                                    <BarChartLine className="me-1" />
                                    <span className="d-none d-md-inline">
                                        {showStats ? "Skrýt" : "Statistika"}
                                    </span>
                                </button>

                                <button
                                    type="button"
                                    className={"btn btn-outline-secondary" + (showHistory ? " active" : "")}
                                    onClick={toggleHistory}
                                    title="Zobrazit historii změn"
                                >
                                    <ClockHistory className="me-1" />
                                    <span className="d-none d-md-inline">
                                        {showHistory ? "Skrýt" : "Historie"}
                                    </span>
                                </button>
                            </div>

                            {/* Nebezpečná akce zvlášť */}
                            <div className="btn-group btn-group-sm" role="group" aria-label="Nebezpečné akce">
                                <button
                                    type="button"
                                    className="btn btn-outline-danger"
                                    disabled={!onDelete}
                                    onClick={() =>
                                        setConfirmAction({
                                            type: "delete",
                                            title: "Potvrzení smazání",
                                            message: `Opravdu chcete smazat hráče: ${fullName}?`,
                                        })
                                    }
                                ><Trash3 className="me-1" />
                                    <span className="d-none d-md-inline">Smazat</span>
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
                {confirmAction && (
                    <ConfirmActionModal
                        show={true}
                        title={confirmAction.title}
                        message={confirmAction.message}
                        confirmText={
                            confirmAction.type === "approve"
                                ? "Schválit"
                                : confirmAction.type === "reject"
                                    ? "Zamítnout"
                                    : "Smazat"
                        }
                        confirmVariant={
                            confirmAction.type === "approve"
                                ? "success"
                                : confirmAction.type === "reject"
                                    ? "danger"
                                    : "danger"
                        }
                        onClose={() => setConfirmAction(null)}
                        onConfirm={() => {
                            if (confirmAction.type === "approve") {
                                onApprove && onApprove(player.id);
                            } else if (confirmAction.type === "reject") {
                                onReject && onReject(player.id);
                            } else if (confirmAction.type === "delete") {
                                onDelete && onDelete(player.id);
                            }
                            setConfirmAction(null);
                        }}
                    />
                )}

            </div>
        </>
    );
};

export default AdminPlayerCard;
