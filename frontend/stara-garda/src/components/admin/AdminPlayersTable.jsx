// src/components/admin/AdminPlayersTable.jsx
import AdminPlayerRow from "./AdminPlayerRow";

/**
 * Tabulka pro zobrazení všech hráčů v admin rozhraní.
 *
 * Nemá vlastní logiku načítání dat – jen:
 *  - zobrazí loading stav
 *  - zobrazí chybu
 *  - vykreslí řádky přes <AdminPlayerRow />
 */
const AdminPlayersTable = ({
    players,
    loading,
    error,
    onApprove,
    onReject,
    onEdit,
    onDelete,
    onChangeUser,
}) => {
    if (loading) {
        return <p>Načítám hráče…</p>;
    }

    if (error) {
        return (
            <div className="alert alert-danger" role="alert">
                {error}
            </div>
        );
    }

    if (!players || players.length === 0) {
        return <p>V systému zatím nejsou žádní hráči.</p>;
    }

    return (
        <div className="table-responsive">
            <table className="table table-striped table-hover align-middle">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Jméno</th>
                        <th>Přezdívka</th>
                        <th>Tým</th>
                        <th>Typ</th>
                        <th>Status</th>
                        <th>Telefon</th>
                        <th>Akce</th>
                    </tr>
                </thead>
                <tbody>
                    {players.map((player) => (
                        <AdminPlayerRow
                            key={player.id}
                            player={player}
                            onApprove={onApprove}
                            onReject={onReject}
                            onEdit={onEdit}
                            onDelete={onDelete}
                            onChangeUser={onChangeUser}
                        />
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminPlayersTable;
