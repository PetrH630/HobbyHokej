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

    const sortedHistory = [...history].sort(
        (a, b) => new Date(b.changedAt) - new Date(a.changedAt)
    );

    return (
        <div className="d-flex flex-column gap-3">
            {sortedHistory.map((item) => (
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

                            <div className="col-12 col-md-4">
                                <span className="fw-semibold">Status:</span>{" "}
                                <strong>{statusLabel(item.status)}</strong>
                            </div>

                            <div className="col-12 col-md-4">
                                <span className="fw-semibold">Tým:</span>{" "}
                                {teamLabel(item.team)}
                            </div>

                            <div className="col-12 col-md-4">
                                <span className="fw-semibold">Změnil:</span>{" "}
                                {item.createdBy}
                            </div>

                            <div className="col-12">
                                <span className="fw-semibold">Poznámka:</span>{" "}
                                {excuseReasonLabel(item.excuseReason)}
                                {item.adminNote || item.excuseNote ? " - " : ""}
                                {item.adminNote || item.excuseNote}
                            </div>

                        </div>

                    </div>
                </div>
            ))}
        </div>
    );
};

export default MatchRegistrationHistory;
