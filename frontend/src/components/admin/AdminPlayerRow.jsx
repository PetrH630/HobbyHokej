// src/components/admin/AdminPlayerRow.jsx
import RoleGuard from "../RoleGuard";

/**
 * Jeden řádek v admin tabulce hráčů.
 *
 * Odpovědnost:
 *  - zobrazit základní informace o hráči
 *  - VŽDY zobrazit všechna akční tlačítka
 *  - podle stavu hráče pouze povolit / zakázat akce (disabled)
 *  - akce jsou dostupné pouze pro roli ADMIN
 *
 * Tlačítka:
 *  - Schválit (approve)   → onApprove(player.id)
 *  - Zamítnout (reject)   → onReject(player.id)
 *  - Upravit (edit)       → onEdit(player)
 *  - Změnit uživatele     → onChangeUser(player)
 *  - Smazat (delete)      → onDelete(player.id)
 *
 * Pokud handler není předán, tlačítko se zobrazí, ale je disabled.
 */
const statusTextMap = {
    PENDING: "čeká na schválení",
    REJECTED: "zamítnuto",
    APPROVED: "schváleno",
};

const AdminPlayerRow = ({
    player,
    onApprove,
    onReject,
    onEdit,
    onDelete,
    onChangeUser,
}) => {
    const playerStatus = player.playerStatus ?? "PENDING";
    const statusText = statusTextMap[playerStatus] ?? playerStatus;

    // Povolení akcí dle stavu hráče
    const canApproveByStatus =
        playerStatus === "PENDING" || playerStatus === "REJECTED";

    const canRejectByStatus =
        playerStatus === "PENDING" || playerStatus === "APPROVED";

    return (
        <tr>
            <td>{player.id}</td>
            <td>
                {player.name} {player.surname?.toUpperCase()}
            </td>
            <td>{player.nickname || "-"}</td>
            <td>{player.team || "-"}</td>
            <td>{player.type || "-"}</td>
            <td>{statusText}</td>
            <td>{player.phoneNumber || "-"}</td>

            <td>
                {/* Akce jsou viditelné pouze pro ADMIN */}
                <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                    <div className="btn-group btn-group-sm" role="group">
                        {/* SCHVÁLIT */}
                        <button
                            type="button"
                            className="btn btn-success"
                            disabled={!onApprove || !canApproveByStatus}
                            title={
                                !canApproveByStatus
                                    ? "Hráče nelze v tomto stavu schválit"
                                    : undefined
                            }
                            onClick={() =>
                                onApprove && onApprove(player.id)
                            }
                        >
                            Schválit
                        </button>

                        {/* ZAMÍTNOUT */}
                        <button
                            type="button"
                            className="btn btn-warning"
                            disabled={!onReject || !canRejectByStatus}
                            title={
                                !canRejectByStatus
                                    ? "Hráče nelze v tomto stavu zamítnout"
                                    : undefined
                            }
                            onClick={() =>
                                onReject && onReject(player.id)
                            }
                        >
                            Zamítnout
                        </button>

                        {/* UPRAVIT */}
                        <button
                            type="button"
                            className="btn btn-primary"
                            disabled={!onEdit}
                            onClick={() => onEdit && onEdit(player)}
                        >
                            Upravit
                        </button>

                        {/* ZMĚNIT UŽIVATELE */}
                        <button
                            type="button"
                            className="btn btn-outline-secondary"
                            disabled={!onChangeUser}
                            onClick={() =>
                                onChangeUser && onChangeUser(player)
                            }
                        >
                            Změnit uživatele
                        </button>

                        {/* SMAZAT */}
                        <button
                            type="button"
                            className="btn btn-danger"
                            disabled={!onDelete}
                            onClick={() =>
                                onDelete && onDelete(player.id)
                            }
                        >
                            Smazat
                        </button>
                    </div>
                </RoleGuard>
            </td>
        </tr>
    );
};

export default AdminPlayerRow;
