// src/components/admin/AdminSeasonsTable.jsx
import AdminSeasonCard from "./AdminSeasonCard";

const AdminSeasonsTable = ({
    seasons,
    loading,
    error,
    onEdit,
    onSetActive,
}) => {
    if (loading) {
        return <p>Načítám sezóny…</p>;
    }

    if (error) {
        return (
            <div className="alert alert-danger" role="alert">
                {error}
            </div>
        );
    }

    if (!seasons || seasons.length === 0) {
        return <p>V systému zatím nejsou žádné sezóny.</p>;
    }

    // Seřadíme podle začátku sezóny (nejnovější nahoře)
    const sortedSeasons = seasons
        .slice()
        .sort((a, b) => {
            const aDate = a.startDate || "";
            const bDate = b.startDate || "";
            if (aDate < bDate) return 1;
            if (aDate > bDate) return -1;
            return 0;
        });

    return (
        <div className="d-flex flex-column gap-3">
            {sortedSeasons.map((season) => (
                <AdminSeasonCard
                    key={season.id}
                    season={season}
                    onEdit={onEdit}
                    onSetActive={onSetActive}
                />
            ))}
        </div>
    );
};

export default AdminSeasonsTable;
