// src/pages/AdminPlayersPage.jsx

import { useCallback, useMemo, useState } from "react";
import {
    approvePlayerAdmin,
    rejectPlayerAdmin,
    deletePlayerAdmin,
    updatePlayerAdmin, // ujisti se, že existuje v playerApi
} from "../api/playerApi";
import { useAllPlayersAdminWithUsers } from "../hooks/useAllPlayersAdminWithUsers";
import AdminPlayersTable from "../components/admin/AdminPlayersTable";
import { useNotification } from "../context/NotificationContext";
import AdminPlayerEditModal from "../components/admin/AdminPlayerEditModal";
import BackButton from "../components/BackButton";

/**
 * Stránka pro globální správu hráčů (ADMIN / MANAGER).
 *
 * - načítá všechny hráče (včetně uživatele) přes useAllPlayersAdminWithUsers
 * - zobrazuje je v gridu karet
 * - řeší akce APPROVE / REJECT / DELETE / EDIT / CHANGE USER
 */
const AdminPlayersPage = () => {
    const { players, loading, error, reload } =
        useAllPlayersAdminWithUsers();
    const { showNotification } = useNotification();

    // === STAV PRO EDITACÍ MODAL ===
    const [editingPlayer, setEditingPlayer] = useState(null);
    const [editSaving, setEditSaving] = useState(false);

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
            const confirmDelete = window.confirm(
                `Opravdu chceš smazat hráče s ID ${playerId}?`
            );
            if (!confirmDelete) return;

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

    // Otevření modalu pro editaci
    const handleEdit = useCallback(
        (player) => {
            setEditingPlayer(player);
        },
        []
    );

    // Zatím jen info – implementace přes modal
    const handleChangeUser = useCallback(
        (player) => {
            console.log("Change user for player (TODO):", player);
            showNotification(
                "Změna uživatele zatím není implementována.",
                "info"
            );
        },
        [showNotification]
    );

    // Zavření modalu
    const handleCloseEdit = () => {
        if (!editSaving) {
            setEditingPlayer(null);
        }
    };

    // Uložení změn hráče z modalu
    const handleSaveEdit = async (payload) => {
        try {
            setEditSaving(true);

            // tady předpokládám API ve tvaru updatePlayerAdmin(id, dto)
            // pokud to máš jinak (třeba jen dto), uprav si tento řádek
            await updatePlayerAdmin(payload.id, payload);

            showNotification("Hráč byl úspěšně upraven.", "success");
            setEditingPlayer(null);
            await reload();
        } catch (err) {
            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Úprava hráče se nezdařila.";
            showNotification(message, "danger");
        } finally {
            setEditSaving(false);
        }
    };

    // Seřadit hráče podle příjmení (vzestupně)
    const sortedPlayers = useMemo(() => {
        if (!players) return [];
        return [...players].sort((a, b) => {
            const aSurname = (a.surname || "").toLocaleLowerCase("cs");
            const bSurname = (b.surname || "").toLocaleLowerCase("cs");
            if (aSurname < bSurname) return -1;
            if (aSurname > bSurname) return 1;
            return 0;
        });
    }, [players]);

    return (
        <>
            <BackButton />
            <div className="container mt-4">
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <h1 className="h3 mb-0">Správa hráčů</h1>
                </div>

                <p className="text-muted mb-3">
                    Zde může administrátor (a částečně manažer) spravovat všechny
                    hráče v systému. Akce schválit / zamítnout / upravit / smazat /
                    změnit uživatele jsou dostupné pouze uživatelům s rolí ADMIN.
                </p>

                <AdminPlayersTable
                    players={sortedPlayers}
                    loading={loading}
                    error={error}
                    onApprove={handleApprove}
                    onReject={handleReject}
                    onEdit={handleEdit}
                    onDelete={handleDelete}
                    onChangeUser={handleChangeUser}
                />

                {/* Modal pro úpravu hráče */}
                <AdminPlayerEditModal
                    player={editingPlayer}
                    show={!!editingPlayer}
                    onClose={handleCloseEdit}
                    onSave={handleSaveEdit}
                    saving={editSaving}
                />
            </div>
        </>
    );
};

export default AdminPlayersPage;
