// src/components/admin/AdminPlayerHistory.jsx
import { usePlayerHistoryAdmin } from "../../hooks/usePlayerHistoryAdmin";
import AdminPlayerHistoryCard from "./AdminPlayerHistoryCard";

const AdminPlayerHistory = ({ playerId }) => {
    const { history, loading, error } = usePlayerHistoryAdmin(playerId);

    if (!playerId) {
        return <p className="text-muted">Hráč není vybrán.</p>;
    }

    if (loading) return <p>Načítám historii hráče…</p>;
    if (error) return <p className="text-danger">{error}</p>;

    if (!history || history.length === 0) {
        return <p>Žádná historie změn hráče.</p>;
    }

    // backend už vrací seřazené, ale pro jistotu:
    const sortedHistory = [...history].sort(
        (a, b) => new Date(b.changedAt) - new Date(a.changedAt)
    );

    return (
        <div className="d-flex flex-column">
            {sortedHistory.map((item) => (
                <AdminPlayerHistoryCard key={item.id} item={item} />
            ))}
        </div>
    );
};

export default AdminPlayerHistory;
