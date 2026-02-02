// src/components/admin/AdminPlayerRow.jsx
import RoleGuard from "../RoleGuard";

/**
 * Jeden řádek v admin tabulce hráčů.
 *
 * Odpovědnost:
 *  - zobrazit základní informace o hráči
 *  - podle stavu hráče ukázat správná tlačítka (APPROVE / REJECT)
 *  - podle role (ADMIN) tlačítka vůbec zobrazit (MANAGER je neuvidí)
 *
 * Tlačítka:
 *  - Schválit (approve)   → volá onApprove(player.id)
 *  - Zamítnout (reject)   → volá onReject(player.id)
 *  - Upravit (edit)       → volá onEdit(player)
 *  - Smazat (delete)      → volá onDelete(player.id)
 *  - Změnit uživatele     → volá onChangeUser(player)
 *
 * Všechny akce jsou volitelné – když handler nepředáš, tlačítko se nevykreslí.
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

    // Podle stavu hráče, kdy má smysl nabídnout jakou akci
    const canApproveByStatus =
        playerStatus === "PENDING" || playerStatus === "REJECTED";
    const canRejectByStatus =
        playerStatus === "PENDING" || playerStatus === "APPROVED";

    return (
        <tr>
            <td>{player.id}</td>
            <td>{player.name} {" "} {player.surname.toUpperCase()}</td>
            <td>{player.nickname || "-"}</td>
            <td>{player.team || "-"}</td>
            <td>{player.type || "-"}</td>
            <td>{statusText}</td>
            <td>{player.phoneNumber || "-"}</td>

            <td>
                {/* Akce pouze pro ADMIN – role vychází z PlayerControlleru:
                    approve/reject/update/delete/change-user jsou jen ADMIN.
                 */}
                <RoleGuard roles={["ROLE_ADMIN"]}>
                    <div className="btn-group btn-group-sm" role="group">
                        {onApprove && canApproveByStatus && (
                            <button
                                type="button"
                                className="btn btn-success"
                                onClick={() => onApprove(player.id)}
                            >
                                Schválit
                            </button>
                        )}

                        {onReject && canRejectByStatus && (
                            <button
                                type="button"
                                className="btn btn-warning"
                                onClick={() => onReject(player.id)}
                            >
                                Zamítnout
                            </button>
                        )}

                        {onEdit && (
                            <button
                                type="button"
                                className="btn btn-primary"
                                onClick={() => onEdit(player)}
                            >
                                Upravit
                            </button>
                        )}

                        {onChangeUser && (
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                onClick={() => onChangeUser(player)}
                            >
                                Změnit uživatele
                            </button>
                        )}

                        {onDelete && (
                            <button
                                type="button"
                                className="btn btn-danger"
                                onClick={() => onDelete(player.id)}
                            >
                                Smazat
                            </button>
                        )}
                    </div>
                </RoleGuard>
            </td>
        </tr>
    );
};

export default AdminPlayerRow;
