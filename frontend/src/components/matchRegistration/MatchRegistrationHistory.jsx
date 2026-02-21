// src/components/MatchRegistration/MatchRegistrationHistory.jsx
import { useMyMatchRegistrationHistory } from "../../hooks/useMatchRegistrationHistory";
import {
    excuseReasonLabel,
    formatDateTime,
    statusLabel,
    teamLabel
} from "../../utils/registrationFormatter";

const MatchRegistrationHistory = ({ matchId }) => {
    const { history, loading, error } = useMyMatchRegistrationHistory(matchId);

    if (loading) return <p>Načítám historii…</p>;
    if (error) return <p className="text-danger">{error}</p>;

    if (!history || history.length === 0) {
        return <p>Žádná historie registrací.</p>;
    }

    // 1) Seřazení vzestupně podle času (od nejstarší po nejnovější)
    const historyAsc = [...history].sort(
        (a, b) => new Date(a.changedAt) - new Date(b.changedAt)
    );

    // 2) Vytvoření mapy změn: pro každou položku (kromě první)
    //    se zjistí, co se změnilo oproti předchozí položce.
    const diffMap = {};

    for (let i = 1; i < historyAsc.length; i++) {
        const prev = historyAsc[i - 1];
        const curr = historyAsc[i];

        const statusChanged = prev.status !== curr.status;
        const teamChanged = prev.team !== curr.team;
        const changedByChanged = prev.createdBy !== curr.createdBy;

        const excuseReasonChanged =
            (prev.excuseReason || null) !== (curr.excuseReason || null);
        const adminNoteChanged =
            (prev.adminNote || null) !== (curr.adminNote || null);
        const excuseNoteChanged =
            (prev.excuseNote || null) !== (curr.excuseNote || null);

        const noteChanged =
            excuseReasonChanged || adminNoteChanged || excuseNoteChanged;

        diffMap[curr.id] = {
            statusChanged,
            teamChanged,
            changedByChanged,
            noteChanged
        };
    }

    // 3) Pro zobrazení použijeme původní směr – od nejnovější po nejstarší
    const sortedHistory = [...historyAsc].sort(
        (a, b) => new Date(b.changedAt) - new Date(a.changedAt)
    );

    const getHighlightClass = (changed) =>
        changed ? "p-1 rounded bg-warning bg-opacity-25" : "";

    return (
        <div className="d-flex flex-column gap-3">
            {sortedHistory.map((item) => {
                const diffs = diffMap[item.id] || {
                    statusChanged: false,
                    teamChanged: false,
                    changedByChanged: false,
                    noteChanged: false
                };

                return (
                    <div key={item.id} className="card shadow-sm">
                        <div className="card-body">

                            <div className="mb-2">
                                <span className="fw-semibold">Datum změny:</span>{" "}
                                <div>
                                    {formatDateTime(item.changedAt)}
                                </div>
                            </div>

                            <hr className="my-2" />

                            <div className="row g-2">

                                {/* STATUS */}
                                <div className="col-12 col-md-4">
                                    <div className={getHighlightClass(diffs.statusChanged)}>
                                        <span className="fw-semibold">Status:</span>{" "}
                                        <strong>{statusLabel(item.status)}</strong>
                                    </div>
                                </div>

                                {/* TÝM */}
                                <div className="col-12 col-md-4">
                                    <div className={getHighlightClass(diffs.teamChanged)}>
                                        <span className="fw-semibold">Tým:</span>{" "}
                                        {teamLabel(item.team)}
                                    </div>
                                </div>

                                {/* ZMĚNIL */}
                                <div className="col-12 col-md-4">
                                    <div className={getHighlightClass(diffs.changedByChanged)}>
                                        <span className="fw-semibold">Změnil:</span>{" "}
                                        {item.createdBy}
                                    </div>
                                </div>

                                {/* POZNÁMKA */}
                                <div className="col-12">
                                    <div className={getHighlightClass(diffs.noteChanged)}>
                                        <span className="fw-semibold">Poznámka:</span>{" "}
                                        {excuseReasonLabel(item.excuseReason)}
                                        {item.adminNote || item.excuseNote ? " - " : ""}
                                        {item.adminNote || item.excuseNote}
                                    </div>
                                </div>

                            </div>

                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default MatchRegistrationHistory;