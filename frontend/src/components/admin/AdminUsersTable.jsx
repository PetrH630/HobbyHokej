// src/components/admin/AdminUsersTable.jsx
import AdminUserCard from "./AdminUserCard";

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
