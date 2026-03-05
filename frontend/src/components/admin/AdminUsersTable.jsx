// src/components/admin/AdminUsersTable.jsx
import AdminUserCard from "./AdminUserCard";

/**
 * AdminUsersTable
 *
 * React komponenta používaná ve frontend aplikaci.
 *
 * Props:
 * @param {Object} props.users vstupní hodnota komponenty.
 * @param {boolean} props.loading Příznak, že probíhá načítání dat a UI má zobrazit stav načítání.
 * @param {string} props.error Chybová zpráva určená k zobrazení uživateli.
 * @param {Function} props.onEdit vstupní hodnota komponenty.
 * @param {Function} props.onResetPassword vstupní hodnota komponenty.
 * @param {Function} props.onActivate vstupní hodnota komponenty.
 * @param {Function} props.onDeactivate vstupní hodnota komponenty.
 */

const AdminUsersTable = ({
    users,
    loading,
    error,
    onEdit,
    onResetPassword,
    onActivate,
    onDeactivate,
}) => {
    if (loading) {
        return <p>Načítám uživatele…</p>;
    }

    if (error) {
        return (
            <div className="alert alert-danger" role="alert">
                {error}
            </div>
        );
    }

    if (!users || users.length === 0) {
        return <p>V systému zatím nejsou žádní uživatelé.</p>;
    }

    // ŘAZENÍ PODLE PŘÍJMENÍ (vzestupně, CZ locale)
    const sortedUsers = users
        .slice()
        .sort((a, b) =>
            (a.surname || "").localeCompare(b.surname || "", "cs", {
                sensitivity: "base",
            })
        );

    return (
        <div className="d-flex flex-column gap-3">
            {sortedUsers.map((user) => (
                <AdminUserCard
                    key={user.id}
                    user={user}
                    onEdit={onEdit}
                    onResetPassword={onResetPassword}
                    onActivate={onActivate}
                    onDeactivate={onDeactivate}
                />
            ))}
        </div>
    );
};

export default AdminUsersTable;
