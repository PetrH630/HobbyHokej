// src/components/admin/AdminMatchHistoryCard.jsx
import {
    formatDateTime,
    matchStatusLabel,
    matchCancelReasonLabel,
    matchActionLabel,
} from "../../utils/matchFormatter";

const getHighlightClass = (changed) =>
    changed ? "p-1 rounded bg-warning bg-opacity-25" : "";

const AdminMatchHistoryCard = ({ item, previousItem }) => {
    const statusText = matchStatusLabel(item.matchStatus);
    const cancelReasonText = matchCancelReasonLabel(item.cancelReason);
    const actionText = matchActionLabel(item.action);

    // Výchozí diffy – pokud není previousItem, nic se nezvýrazňuje
    const diffs = {
        statusChanged: false,
        cancelReasonChanged: false,
        dateTimeChanged: false,
        locationChanged: false,
        maxPlayersChanged: false,
        priceChanged: false,
        seasonIdChanged: false,
        createdByChanged: false,
        lastModifiedChanged: false,
    };

    if (previousItem) {
        diffs.statusChanged =
            (previousItem.matchStatus || "") !==
            (item.matchStatus || "");

        diffs.cancelReasonChanged =
            (previousItem.cancelReason || "") !==
            (item.cancelReason || "");

        diffs.dateTimeChanged =
            (previousItem.dateTime || "") !== (item.dateTime || "");

        diffs.locationChanged =
            (previousItem.location || "") !== (item.location || "");

        diffs.maxPlayersChanged =
            (previousItem.maxPlayers ?? null) !==
            (item.maxPlayers ?? null);

        diffs.priceChanged =
            (previousItem.price ?? null) !== (item.price ?? null);

        diffs.seasonIdChanged =
            (previousItem.seasonId ?? null) !==
            (item.seasonId ?? null);

        diffs.createdByChanged =
            (previousItem.createdByUserId ?? null) !==
            (item.createdByUserId ?? null);

        diffs.lastModifiedChanged =
            (previousItem.lastModifiedByUserId ?? null) !==
            (item.lastModifiedByUserId ?? null);
    }

    return (
        <div className="card shadow-sm">
            {/* === HLAVIČKA – datum změny, akce, stav === */}
            <div className="card-body border-bottom py-2 py-md-3">
                <div className="d-flex flex-column flex-md-row justify-content-between gap-2">
                    <div>
                        <small className="text-muted d-block">Datum změny</small>
                        <strong>{formatDateTime(item.changedAt)}</strong>
                    </div>

                    <div>
                        <small className="text-muted d-block">Akce</small>
                        <span>{actionText}</span>
                    </div>

                    <div className={"text-md-end " + getHighlightClass(diffs.statusChanged)}>
                        <small className="text-muted d-block">Stav zápasu</small>
                        {statusText ? (
                            <span className="badge bg-primary">
                                {statusText}
                            </span>
                        ) : (
                            "-"
                        )}
                    </div>
                </div>
            </div>

            {/* === DETAILY ZÁPASU === */}
            <div className="card-body py-2 py-md-3">
                <div className="row">
                    <div className={"col-md-6 mb-2 " + getHighlightClass(diffs.dateTimeChanged)}>
                        <small className="text-muted d-block">Datum zápasu</small>
                        <span>{formatDateTime(item.dateTime)}</span>
                    </div>

                    <div className={"col-md-6 mb-2 " + getHighlightClass(diffs.locationChanged)}>
                        <small className="text-muted d-block">Místo</small>
                        <span>{item.location || "-"}</span>
                    </div>

                    <div className={"col-6 col-md-3 mb-2 " + getHighlightClass(diffs.maxPlayersChanged)}>
                        <small className="text-muted d-block">
                            Max. hráčů
                        </small>
                        <span>{item.maxPlayers ?? "-"}</span>
                    </div>

                    <div className={"col-6 col-md-3 mb-2 " + getHighlightClass(diffs.priceChanged)}>
                        <small className="text-muted d-block">Cena</small>
                        <span>
                            {item.price != null ? `${item.price} Kč` : "-"}
                        </span>
                    </div>

                    <div className={"col-6 col-md-3 mb-2 " + getHighlightClass(diffs.seasonIdChanged)}>
                        <small className="text-muted d-block">Sezóna (ID)</small>
                        <span>{item.seasonId ?? "-"}</span>
                    </div>

                    <div className={"col-12 col-md-3 mb-2 " + getHighlightClass(diffs.cancelReasonChanged)}>
                        <small className="text-muted d-block">
                            Důvod zrušení
                        </small>
                        <span>{cancelReasonText || "-"}</span>
                    </div>
                </div>
            </div>

            {/* === METADATA === */}
            <div className="card-body border-top bg-light py-2 py-md-3">
                <div className="row">
                    <div className={"col-md-6 mb-1 " + getHighlightClass(diffs.createdByChanged)}>
                        <small className="text-muted d-block">
                            Vytvořil (uživatel ID)
                        </small>
                        <span>{item.createdByUserId ?? "-"}</span>
                    </div>

                    <div className={"col-md-6 mb-1 " + getHighlightClass(diffs.lastModifiedChanged)}>
                        <small className="text-muted d-block">
                            Naposledy změnil (uživatel ID)
                        </small>
                        <span>{item.lastModifiedByUserId ?? "-"}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminMatchHistoryCard;