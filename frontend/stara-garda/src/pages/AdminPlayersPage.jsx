// src/pages/AdminPlayersPage.jsx

import { useCallback } from "react";
import {
    approvePlayerAdmin,
    rejectPlayerAdmin,
    deletePlayerAdmin,
    // createPlayerAdmin,
    // updatePlayerAdmin,
    // changePlayerUserAdmin,
} from "../api/playerApi";
import { useAllPlayersAdmin } from "../hooks/useAllPlayersAdmin";
import AdminPlayersTable from "../components/admin/AdminPlayersTable";
import { useNotification } from "../context/NotificationContext";

/**
 * Stránka pro globální správu hráčů (ADMIN / MANAGER).
 *
 * Odpovědnost:
 *  - načíst všechny hráče (useAllPlayersAdmin)
 *  - předat je tabulce
 *  - obsloužit akce APPROVE / REJECT / DELETE
 *  - zobrazit notifikace o úspěchu / chybě
 *
 * Poznámka:
 *  - tlačítka v řádku (approve/reject/edit/delete/change-user)
 *    se zobrazují pouze ADMINOVI (řeší RoleGuard v AdminPlayerRow).
 *  - MANAGER uvidí seznam hráčů, ale bez akčních tlačítek.
 */
const AdminPlayersPage = () => {
    const { players, loading, error, reload } = useAllPlayersAdmin();
    const { showNotification } = useNotification();

    const handleApprove = useCallback(
        async (playerId) => {
            try {
                await approvePlayerAdmin(playerId);
                showNotification("Hráč byl schválen.", "success");
                await reload();
            } catch (err) {
                const message =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Schválení hráče se nezdařilo.";
                showNotification(message, "danger");
            }
        },
        [reload, showNotification]
    );

    const handleReject = useCallback(
        async (playerId) => {
            try {
                await rejectPlayerAdmin(playerId);
                showNotification("Hráč byl zamítnut.", "success");
                await reload();
            } catch (err) {
                const message =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Zamítnutí hráče se nezdařilo.";
                showNotification(message, "danger");
            }
        },
        [reload, showNotification]
    );

    const handleDelete = useCallback(
        async (playerId) => {
            const confirm = window.confirm(
                `Opravdu chceš smazat hráče s ID ${playerId}?`
            );
            if (!confirm) return;

            try {
                await deletePlayerAdmin(playerId);
                showNotification("Hráč byl smazán.", "success");
                await reload();
            } catch (err) {
                const message =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Smazání hráče se nezdařilo.";
                showNotification(message, "danger");
            }
        },
        [reload, showNotification]
    );

    // Místo pro budoucí implementaci:
    //  - handleEdit(player) → otevře modál, použije updatePlayerAdmin
    //  - handleChangeUser(player) → modál s výběrem uživatele, použije changePlayerUserAdmin

    const handleEdit = useCallback((player) => {
        // TODO: otevřít modál pro editaci hráče a použít updatePlayerAdmin
        console.log("Edit player (TODO):", player);
        showNotification(
            "Editace hráče zatím není implementována.",
            "info"
        );
    }, [showNotification]);

    const handleChangeUser = useCallback((player) => {
        // TODO: otevřít modál pro změnu uživatele a použít changePlayerUserAdmin
        console.log("Change user for player (TODO):", player);
        showNotification(
            "Změna uživatele zatím není implementována.",
            "info"
        );
    }, [showNotification]);

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 mb-0">Správa hráčů</h1>

                {/* Sem může později přijít tlačítko "Vytvořit hráče (ADMIN/MANAGER)" */}
                {/* <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleCreatePlayer}
                >
                    Vytvořit hráče
                </button> */}
            </div>

            <p className="text-muted mb-3">
                Zde může administrátor (a částečně manažer) spravovat všechny
                hráče v systému. Akce schválit / zamítnout / upravit / smazat /
                změnit uživatele jsou dostupné pouze uživatelům s rolí ADMIN.
            </p>

            <AdminPlayersTable
                players={players}
                loading={loading}
                error={error}
                onApprove={handleApprove}
                onReject={handleReject}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onChangeUser={handleChangeUser}
            />
        </div>
    );
};

export default AdminPlayersPage;
