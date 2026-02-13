// src/components/admin/AdminUserCard.jsx
import RoleGuard from "../RoleGuard";

const AdminUserCard = ({
    user,
    onEdit,
    onResetPassword,
    onActivate,
    onDeactivate,
}) => {

    const roleLabelMap = {
        ROLE_ADMIN: "Administrátor",
        ROLE_MANAGER: "Manažer",
        ROLE_PLAYER: "Hráč",
    };

    const isActive =
        user.active ?? user.enabled ?? false; // podle toho, jak se to u tebe jmenuje

    // sjednotíme si role na pole stringů
    let rolesArray = [];

    if (Array.isArray(user.roles)) {
        rolesArray = user.roles;
    } else if (typeof user.roles === "string" && user.roles) {
        rolesArray = [user.roles];
    } else if (Array.isArray(user.role)) {
        rolesArray = user.role;
    } else if (typeof user.role === "string" && user.role) {
        rolesArray = [user.role];
    }

    // přemapujeme na české názvy
    const rolesText =
        rolesArray.length > 0
            ? rolesArray
                .map((r) => roleLabelMap[r] || r) // fallback na původní string
                .join(", ")
            : "-";

    const isAdminUser = rolesArray.includes("ROLE_ADMIN");


    const hasPlayers = Array.isArray(user.players) && user.players.length > 0;

    return (
        <div className="card shadow-sm">
            {/* === ŘÁDEK 1 – UŽIVATEL === */}
            <div className="card-body border-bottom">
                <div className="row align-items-center">
                    <div className="col-md-4 fw-bold">
                        {user.name} {user.surname?.toUpperCase()}
                        {isActive ? (
                            <span className="badge bg-success ms-2">
                                aktivní
                            </span>
                        ) : (
                            <span className="badge bg-secondary ms-2">
                                neaktivní
                            </span>
                        )}
                    </div>

                    <div className="col-md-4">
                        <small className="text-muted d-block">E-mail</small>
                        {user.email}
                    </div>

                    <div className="col-md-4">
                        <small className="text-muted d-block">Role</small>
                        {rolesText}
                    </div>
                </div>
            </div>

            {/* === ŘÁDEK 2 – AKCE === */}
            <div className="card-body border-bottom bg-light">
                <RoleGuard roles={["ROLE_ADMIN"]}>
                    <div className="d-flex justify-content-end">
                        <div className="btn-group btn-group-sm flex-wrap">
                            
                                                      
                            {/* RESET HESLA */}
                            <button
                                type="button"
                                className="btn btn-warning"
                                disabled={!onResetPassword}
                                onClick={() =>
                                    onResetPassword && onResetPassword(user.id)
                                }
                            >
                                Reset hesla
                            </button>

                            {/* AKTIVOVAT */}
                            <button
                                type="button"
                                className="btn btn-success"
                                disabled={!onActivate || isActive || isAdminUser}
                                title={
                                    isAdminUser
                                        ? "Administrátora nelze deaktivovat ani znovu aktivovat"
                                        : undefined
                                }
                                onClick={() => onActivate && onActivate(user.id)}
                            >
                                Aktivovat
                            </button>

                            {/* DEAKTIVOVAT */}
                            <button
                                type="button"
                                className="btn btn-outline-danger"
                                disabled={!onDeactivate || !isActive || isAdminUser}
                                title={
                                    isAdminUser
                                        ? "Administrátora nelze deaktivovat"
                                        : undefined
                                }
                                onClick={() => onDeactivate && onDeactivate(user.id)}
                            >
                                Deaktivovat
                            </button>

                        </div>
                    </div>
                </RoleGuard>
            </div>

            {/* === ŘÁDKY 3+ – HRÁČI U UŽIVATELE === */}
            <div className="card-body">
                <small className="text-muted d-block mb-2">
                    Hráči přiřazení k uživateli:
                </small>

                {hasPlayers ? (
                    <div className="d-flex flex-column gap-1">
                        {user.players.map((player) => (
                            <div
                                key={player.id}
                                className="d-flex justify-content-between border-bottom py-1"
                            >
                                
                                <div>
                                    <strong>
                                        {player.name}{" "}
                                        {player.surname?.toUpperCase()}{" "} {"- id: "} {player.id}
                                    </strong>
                                    {player.nickname && (
                                        <span className="text-muted ms-2">
                                            ({player.nickname})
                                        </span>
                                    )}
                                </div>
                                <div className="text-end">
                                    <small className="text-muted d-block">
                                        Tým
                                    </small>
                                    <span>{player.team || "-"}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <span className="text-muted">
                        Uživatel zatím nemá přiřazené žádné hráče.
                    </span>
                )}
            </div>
        </div>
    );
};

export default AdminUserCard;
