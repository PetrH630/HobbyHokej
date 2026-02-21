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

    // 1) Seřadit vzestupně (od nejstarší po nejnovější) pro výpočet "předchozího" záznamu
    const historyAsc = [...history].sort(
        (a, b) => new Date(a.changedAt) - new Date(b.changedAt)
    );

    // 2) Mapování id -> předchozí záznam (aby se změna zvýraznila v NÁSLEDUJÍCÍm záznamu)
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
        <div className="d-flex flex-column">
            {sortedHistory.map((item) => (
                <AdminPlayerHistoryCard
                    key={item.id}
                    item={item}
                    previousItem={previousById[item.id] || null}
                />
            ))}
        </div>
    );
};

export default AdminPlayerHistory;