import { useState } from "react";
import RoleGuard from "../RoleGuard";
import AdminMatchHistory from "./AdminMatchHistory";
import AdminMatchDetailInline from "./AdminMatchDetailInline";
import {
    matchStatusLabel,
    matchCancelReasonLabel,
    matchActionLabel,
} from "../../utils/matchFormatter";
import { useGlobalDim } from "../../hooks/useGlobalDim";
import ConfirmActionModal from "../common/ConfirmActionModal";

import {
    InfoCircle,
    PencilSquare,
    ClockHistory,
    XOctagon,
    ArrowCounterclockwise,
} from "react-bootstrap-icons";

const AdminMatchCard = ({ match, onEdit, onDelete, onCancel, onUnCancel }) => {
    const [showHistory, setShowHistory] = useState(false);
    const [showDetail, setShowDetail] = useState(false);

    // ✅ confirm jen pro zrušit/obnovit
    const [confirmAction, setConfirmAction] = useState(null); // { type, title, message }

    const isExpanded = showDetail || showHistory;
    const dimActive = isExpanded;

    useGlobalDim(dimActive);

    const statusText = matchStatusLabel(match.matchStatus);
    const cancelReasonText = matchCancelReasonLabel(match.cancelReason);
    const actionText = matchActionLabel(match.action);

    const isCanceled = match.matchStatus === "CANCELED";

    const parseDateTime = (dt) => {
        if (!dt) return null;
        const safe = dt.replace(" ", "T");
        const d = new Date(safe);
        return Number.isNaN(d.getTime()) ? null : d;
    };

    const formatDateTime = (dt) => {
        const d = parseDateTime(dt);
        if (!d) return "-";
        return d.toLocaleString("cs-CZ");
    };

    const matchDate = parseDateTime(match.dateTime);
    const now = new Date();
    const isPast = matchDate ? matchDate < now : false;

    // badge: zrušený > uplynulý > budoucí
    let badgeText = "budoucí";
    let badgeClass = "bg-success";

    if (isCanceled) {
        badgeText = "zrušený";
        badgeClass = "bg-danger";
    } else if (isPast) {
        badgeText = "uplynulý";
        badgeClass = "bg-secondary";
    }

    const toggleHistory = () => setShowHistory((prev) => !prev);
    const toggleDetail = () => setShowDetail((prev) => !prev);

    const cardClassName =
        "card shadow-sm mb-3 " +
        (isExpanded ? "border border-2 " : "") +
        (dimActive
            ? "bg-white dim-keep"
            : isExpanded
                ? "bg-danger bg-opacity-10"
                : "");

    const buildMatchTitle = () =>
        `#${match.matchNumber ?? match.id} – ${formatDateTime(match.dateTime)}`;

    return (
        <>
            {dimActive && (
                <div
                    className="global-dim-click"
                    onClick={() => {
                        setShowHistory(false);
                        setShowDetail(false);
                    }}
                    aria-hidden="true"
                />
            )}

            <div className={cardClassName}>
                {/* === ŘÁDEK 1 – ZÁKLADNÍ INFO O ZÁPASE === */}
                <div className="card-body border-bottom">
                    <div className="row align-items-center">
                        <div className="col-md-4 fw-bold">
                            {match.matchNumber != null && (
                                <span className="me-2">#{match.matchNumber}</span>
                            )}
                            <span>{formatDateTime(match.dateTime)}</span>

                            <span className={`badge ms-2 ${badgeClass}`}>
                                {badgeText}
                            </span>
                        </div>

                        <div className="col-md-4">
                            <small className="text-muted d-block">
                                Místo: <strong>{match.location || "-"}</strong>
                            </small>
                        </div>

                        <div className="col-md-2">
                            <small className="text-muted d-block">
                                Max. hráčů: <strong>{match.maxPlayers ?? "-"}</strong>
                            </small>
                        </div>

                        <div className="col-md-2">
                            <small className="text-muted d-block">
                                Cena:{" "}
                                <strong>
                                    {match.price != null ? `${match.price} Kč` : "-"}
                                </strong>
                            </small>
                        </div>
                    </div>
                </div>

                {/* === ŘÁDEK 2 – STAV + AKCE === */}
                <div className="card-body border-bottom bg-light">
                    <div className="row align-items-center">
                        <div className="col-md-4">
                            <small className="text-muted d-block">
                                Stav{" "}
                                <strong>
                                    {statusText || "-"}
                                    {match.cancelReason && (
                                        <div className="mt-1">
                                            <small className="text-muted d-block">
                                                Důvod zrušení
                                            </small>
                                            <span>{cancelReasonText}</span>
                                        </div>
                                    )}
                                </strong>
                            </small>
                        </div>

                        <div className="col-md-8">
                            <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                                <div className="d-flex justify-content-end">
                                    {/* ✅ jen styling + ikony, žádné nové položky */}
                                    <div className="btn-group btn-group-sm flex-wrap gap-2">
                                        {/* DETAIL */}
                                        <button
                                            type="button"
                                            className={
                                                "btn btn-outline-info d-inline-flex align-items-center justify-content-center gap-1" +
                                                (showDetail ? " active" : "")
                                            }
                                            onClick={toggleDetail}
                                            title="Detail"
                                        >
                                            <InfoCircle className="me-1" />
                                            <span className="d-none d-md-inline text-nowrap">
                                                {showDetail ? "Skrýt detail" : "Detail"}
                                            </span>
                                        </button>

                                        {/* UPRAVIT */}
                                        <button
                                            type="button"
                                            className="btn btn-primary d-inline-flex align-items-center justify-content-center gap-1"
                                            disabled={!onEdit}
                                            onClick={() => onEdit && onEdit(match)}
                                            title="Upravit"
                                        >
                                            <PencilSquare className="me-1" />
                                            <span className="d-none d-md-inline text-nowrap">
                                                Upravit
                                            </span>
                                        </button>

                                        {/* HISTORIE */}
                                        <button
                                            type="button"
                                            className={
                                                "btn btn-outline-secondary d-inline-flex align-items-center justify-content-center gap-1" +
                                                (showHistory ? " active" : "")
                                            }
                                            onClick={toggleHistory}
                                            title="Historie"
                                        >
                                            <ClockHistory className="me-1" />
                                            <span className="d-none d-md-inline text-nowrap">
                                                {showHistory ? "Skrýt historii" : "Historie"}
                                            </span>
                                        </button>

                                        {/* ZRUŠIT (s confirm) */}
                                        {!isCanceled && (
                                            <button
                                                type="button"
                                                className="btn btn-warning d-inline-flex align-items-center justify-content-center gap-1"
                                                disabled={!onCancel}
                                                onClick={() =>
                                                    setConfirmAction({
                                                        type: "cancel",
                                                        title: "Potvrzení zrušení",
                                                        message: `Opravdu chcete zrušit zápas ${buildMatchTitle()}?`,
                                                    })
                                                }
                                                title="Zrušit"
                                            >
                                                <XOctagon className="me-1" />
                                                <span className="d-none d-md-inline text-nowrap">
                                                    Zrušit
                                                </span>
                                            </button>
                                        )}

                                        {/* OBNOVIT (s confirm) */}
                                        {isCanceled && (
                                            <button
                                                type="button"
                                                className="btn btn-success d-inline-flex align-items-center justify-content-center gap-1"
                                                disabled={!onUnCancel}
                                                onClick={() =>
                                                    setConfirmAction({
                                                        type: "uncancel",
                                                        title: "Potvrzení obnovení",
                                                        message: `Opravdu chcete obnovit zápas ${buildMatchTitle()}?`,
                                                    })
                                                }
                                                title="Obnovit"
                                            >
                                                <ArrowCounterclockwise className="me-1" />
                                                <span className="d-none d-md-inline text-nowrap">
                                                    Obnovit
                                                </span>
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </RoleGuard>
                        </div>
                    </div>
                </div>

                {/* === ŘÁDEK 3 – DETAIL ZÁPASU === */}
                {showDetail && (
                    <div className="card-body bg-white border-top">
                        <h6 className="mb-2">
                            Detail zápasu #{match.matchNumber ?? match.id}
                        </h6>
                        <AdminMatchDetailInline matchId={match.id} />
                    </div>
                )}

                {/* === ŘÁDEK 4 – HISTORIE ZÁPASU === */}
                {showHistory && (
                    <div className="card-body bg-white border-top">
                        <h6 className="mb-2">
                            Historie zápasu #{match.matchNumber ?? match.id}
                        </h6>
                        <AdminMatchHistory matchId={match.id} />
                    </div>
                )}
            </div>

            {/* ✅ ConfirmActionModal */}
            {confirmAction && (
                <ConfirmActionModal
                    show={true}
                    title={confirmAction.title}
                    message={confirmAction.message}
                    confirmText={confirmAction.type === "cancel" ? "Zrušit" : "Obnovit"}
                    confirmVariant={confirmAction.type === "cancel" ? "warning" : "success"}
                    onClose={() => setConfirmAction(null)}
                    onConfirm={() => {
                        if (confirmAction.type === "cancel") {
                            onCancel && onCancel(match);
                        } else if (confirmAction.type === "uncancel") {
                            onUnCancel && onUnCancel(match.id);
                        }
                        setConfirmAction(null);
                    }}
                />
            )}
        </>
    );
};

export default AdminMatchCard;
