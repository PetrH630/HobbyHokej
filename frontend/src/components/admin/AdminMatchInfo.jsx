// src/components/admin/AdminMatchInfo.jsx
import { useState } from "react";
import AdminMatchRegistrationInfo from "./AdminMatchRegistrationInfo";
import RoleGuard from "../RoleGuard";
import { useNotification } from "../../context/NotificationContext";
import {
    markNoExcusedAdmin,
    cancelNoExcusedAdmin,
} from "../../api/matchRegistrationApi";
import AdminPlayerRegistrationHistoryModal from "./AdminPlayerRegistrationHistoryModal";

const parseDateTime = (dt) => {
    if (!dt) return null;
    const safe = dt.replace(" ", "T");
    const d = new Date(safe);
    return Number.isNaN(d.getTime()) ? null : d;
};

const AdminMatchInfo = ({ match, onRefresh }) => {
    const { showNotification } = useNotification();

    const [saving, setSaving] = useState(false);
    const [historyPlayer, setHistoryPlayer] = useState(null);

    const matchDate = parseDateTime(match?.dateTime);
    const now = new Date();
    const isPastMatch = matchDate ? matchDate < now : false;

    const handleMarkNoExcuse = async (playerId, adminNote) => {
        try {
            setSaving(true);
            await markNoExcusedAdmin(
                match.id,
                playerId,
                adminNote && adminNote.trim()
                    ? adminNote.trim()
                    : "Admin: bez omluvy"
            );
            showNotification("Hráč byl označen jako bez omluvy.", "success");
            if (onRefresh) {
                await onRefresh();
            }
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.message ||
                "Neomluvení hráče se nezdařilo.";
            showNotification(msg, "danger");
        } finally {
            setSaving(false);
        }
    };

    const handleCancelNoExcuse = async (playerId, excuseNote) => {
        try {
            setSaving(true);
            await cancelNoExcusedAdmin(
                match.id,
                playerId,
                excuseNote && excuseNote.trim()
                    ? excuseNote.trim()
                    : "Admin: omluven - JINÉ"
            );
            showNotification(
                "Neomluvení bylo zrušeno (hráč omluven - JINÉ).",
                "success"
            );
            if (onRefresh) {
                await onRefresh();
            }
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.message ||
                "Zrušení neomluvení se nezdařilo.";
            showNotification(msg, "danger");
        } finally {
            setSaving(false);
        }
    };

    // obaly pro aktuálně vybraného hráče v modalu
    const handleMarkNoExcuseForHistoryPlayer = async (adminNote) => {
        if (!historyPlayer) return;
        await handleMarkNoExcuse(historyPlayer.id, adminNote);
    };

    const handleCancelNoExcuseForHistoryPlayer = async (excuseNote) => {
        if (!historyPlayer) return;
        await handleCancelNoExcuse(historyPlayer.id, excuseNote);
    };

    return (
        <div className="card">
            <div className="card-body">
                {/* HLAVIČKA */}
                <div className="d-flex justify-content-between align-items-start mb-3">
                    <div>
                        {match.description && (
                            <p className="card-text mb-2">
                                <strong>Popis: </strong>
                                {match.description}
                            </p>
                        )}

                        <p className="card-text mb-2">
                            <strong>Hráči celkem: </strong>
                            {match.inGamePlayers} / {match.maxPlayers}
                        </p>

                        <p className="card-text mb-2">
                            <strong>Rozdělení týmů: </strong>
                            {match.inGamePlayersDark} /{" "}
                            {match.inGamePlayersLight}
                        </p>

                        {match.price != null && (
                            <p className="card-text mb-2">
                                <strong>Cena zápasu: </strong>
                                {match.price} Kč
                            </p>
                        )}

                        {match.pricePerRegisteredPlayer != null && (
                            <p className="card-text mb-2">
                                <strong>Cena / hráč: </strong>
                                {match.pricePerRegisteredPlayer.toFixed(0)} Kč
                            </p>
                        )}

                        <p className="card-text mb-0">
                            <small className="text-muted">
                                {isPastMatch
                                    ? "Ukončený zápas – lze pracovat s neomluvenými hráči."
                                    : "Budoucí zápas – neomluvení se typicky řeší až po zápase."}
                            </small>
                        </p>
                    </div>

                    {/* Můžeš tady nechat RoleGuard jen jako info, ale akce jsou až v modalu */}
                    <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                        <span className="badge bg-info-subtle text-dark">
                            Admin přehled
                        </span>
                    </RoleGuard>
                </div>

                <h4 className="mt-4">Sestava:</h4>
                <AdminMatchRegistrationInfo
                    match={match}
                    onPlayerClick={setHistoryPlayer}
                />
            </div>

            {/* MODAL – HISTORIE REGISTRACÍ HRÁČE + akce bez omluvy */}
            {historyPlayer && (
                <AdminPlayerRegistrationHistoryModal
                    match={match}
                    player={historyPlayer}
                    saving={saving}
                    onClose={() => setHistoryPlayer(null)}
                    onMarkNoExcuse={handleMarkNoExcuseForHistoryPlayer}
                    onCancelNoExcuse={handleCancelNoExcuseForHistoryPlayer}
                />
            )}
        </div>
    );
};

export default AdminMatchInfo;
