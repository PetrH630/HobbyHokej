// src/components/admin/AdminMatchCard.jsx
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

const AdminMatchCard = ({ match, onEdit, onDelete, onCancel, onUnCancel }) => {
    const [showHistory, setShowHistory] = useState(false);
    const [showDetail, setShowDetail] = useState(false);

    const isExpanded = showDetail || showHistory;
    const dimActive = isExpanded; //  ztmavení platí pro detail i historii

    //  zapne globální ztmavení, když je detail nebo historie otevřená
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

    // logika badge: zrušený > uplynulý > aktivní
    let badgeText = "budoucí";
    let badgeClass = "bg-success";

    if (isCanceled) {
        badgeText = "zrušený";
        badgeClass = "bg-danger";
    } else if (isPast) {
        badgeText = "uplynulý";
        badgeClass = "bg-secondary";
    }

    const toggleHistory = () => {
        setShowHistory((prev) => !prev);
    };

    const toggleDetail = () => {
        setShowDetail((prev) => !prev);
    };

    //  DŮLEŽITÉ:
    // Když je otevřený detail nebo historie, karta musí být neprůhledná (žádné bg-opacity),
    // aby se "neztmavila" přes overlay stránky.
    const cardClassName =
        "card shadow-sm mb-3 " +
        (isExpanded ? "border border-2 " : "") +
        (dimActive
            ? "bg-white dim-keep"
            : isExpanded
                ? "bg-danger bg-opacity-10"
                : "");

    return (
        <>
            {/*  klik mimo kartu zavře detail/historii (vrstva je pod kartou, nad ztmavením) */}
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
                            <small className="text-muted d-block">Místo</small>
                            {match.location || "-"}
                        </div>

                        <div className="col-md-2">
                            <small className="text-muted d-block">Max. hráčů</small>
                            {match.maxPlayers ?? "-"}
                        </div>

                        <div className="col-md-2">
                            <small className="text-muted d-block">Cena</small>
                            {match.price != null ? `${match.price} Kč` : "-"}
                        </div>
                    </div>
                </div>

                {/* === ŘÁDEK 2 – STAV + AKCE === */}
                <div className="card-body border-bottom bg-light">
                    <div className="row align-items-center">
                        <div className="col-md-4">
                            <small className="text-muted d-block">Stav</small>
                            {statusText || "-"}
                            {match.cancelReason && (
                                <div>
                                    <small className="text-muted d-block">
                                        Důvod zrušení
                                    </small>
                                    <span>{cancelReasonText}</span>
                                </div>
                            )}
                        </div>

                        <div className="col-md-8">
                            <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                                <div className="d-flex justify-content-end">
                                    <div className="btn-group btn-group-sm flex-wrap">
                                        {/* DETAIL */}
                                        <button
                                            type="button"
                                            className={
                                                "btn btn-outline-info" +
                                                (showDetail ? " active" : "")
                                            }
                                            onClick={toggleDetail}
                                        >
                                            {showDetail ? "Skrýt detail" : "Detail"}
                                        </button>

                                        {/* UPRAVIT */}
                                        <button
                                            type="button"
                                            className="btn btn-primary"
                                            disabled={!onEdit}
                                            onClick={() => onEdit && onEdit(match)}
                                        >
                                            Upravit
                                        </button>

                                        {/* HISTORIE */}
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

                                        {/* ZRUŠIT */}
                                        {!isCanceled && (
                                            <button
                                                type="button"
                                                className="btn btn-warning"
                                                disabled={!onCancel}
                                                onClick={() => onCancel && onCancel(match)}
                                            >
                                                Zrušit
                                            </button>
                                        )}

                                        {/* OBNOVIT */}
                                        {isCanceled && (
                                            <button
                                                type="button"
                                                className="btn btn-success"
                                                disabled={!onUnCancel}
                                                onClick={() => onUnCancel && onUnCancel(match.id)}
                                            >
                                                Obnovit
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
        </>
    );
};

export default AdminMatchCard;
