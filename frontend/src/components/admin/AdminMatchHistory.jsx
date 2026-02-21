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

    // 1) Seřadit vzestupně (od nejstarší po nejnovější) pro výpočet "předchozího" záznamu
    const historyAsc = [...history].sort(
        (a, b) => new Date(a.changedAt) - new Date(b.changedAt)
    );

    // 2) Mapování id -> předchozí záznam
    const previousById = {};

    for (let i = 1; i < historyAsc.length; i++) {
        const prev = historyAsc[i - 1];
        const curr = historyAsc[i];
        previousById[curr.id] = prev;
    }

    // 3) Pro zobrazení chceme pořadí od nejnovějšího po nejstarší
    const sortedHistory = [...historyAsc].sort(
        (a, b) => new Date(b.changedAt) - new Date(a.changedAt)
    );

    return (
        <div className="d-flex flex-column gap-2">
            {sortedHistory.map((item) => (
                <AdminMatchHistoryCard
                    key={item.id}
                    item={item}
                    previousItem={previousById[item.id] || null}
                />
            ))}
        </div>
    );
};

export default AdminMatchHistory;