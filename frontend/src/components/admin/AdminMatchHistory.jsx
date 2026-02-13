// src/components/admin/AdminMatchHistory.jsx
import { useMatchHistoryAdmin } from "../../hooks/useMatchHistoryAdmin";
import AdminMatchHistoryCard from "./AdminMatchHistoryCard";

const AdminMatchHistory = ({ matchId }) => {
    const { history, loading, error } = useMatchHistoryAdmin(matchId);

    if (loading) {
        return <p>Načítám historii zápasu…</p>;
    }

    if (error) {
        return <p className="text-danger">{error}</p>;
    }

    if (!history || history.length === 0) {
        return <p>Žádná historie změn zápasu.</p>;
    }

    const sortedHistory = history
        .slice()
        .sort(
            (a, b) =>
                new Date(b.changedAt) - new Date(a.changedAt)
        );

    return (
        <div className="d-flex flex-column gap-2">
            {sortedHistory.map((item) => (
                <AdminMatchHistoryCard
                    key={item.id}
                    item={item}
                />
            ))}
        </div>
    );
};

export default AdminMatchHistory;
