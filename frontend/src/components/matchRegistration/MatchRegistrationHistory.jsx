// src/components/MatchRegistration/MatchRegistrationHistory.jsx
import { useMyMatchRegistrationHistory } from "../..//hooks/useMatchRegistrationHistory";
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

    if (history.length === 0) {
        return <p>Žádná historie registrací.</p>;
    }
    const sortedHistory = [...history].sort(
        (a, b) => new Date(b.changedAt) - new Date(a.changedAt)
    );


    return (
        <div className="table-responsive">
            <table className="table table-sm table-striped table-hover align-middle">
                <thead className="table-light">
                    <tr>
                        <th>Datum změny</th>
                        <th>Status</th>
                        <th>Tým</th>
                        <th>Změnil</th>
                        <th>Poznámka</th>
                    </tr>
                </thead>
                <tbody>
                    {sortedHistory.map((item) => (
                        <tr key={item.id}>
                            <td>{formatDateTime(item.changedAt)}</td>
                            <td>
                                <span className="text">
                                    <strong>{statusLabel(item.status)}</strong>
                                </span>

                            </td>
                            <td>{teamLabel(item.team)}</td>
                            <td>{item.createdBy}</td>
                            <td>
                                {excuseReasonLabel(item.excuseReason)} {" - "}
                                {item.adminNote || item.excuseNote}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default MatchRegistrationHistory;
