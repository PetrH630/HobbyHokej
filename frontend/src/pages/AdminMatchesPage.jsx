// src/pages/AdminMatchesPage.jsx

import { useCallback, useState, useEffect } from "react";
import {
    createMatchAdmin,
    updateMatchAdmin,
    deleteMatchAdmin,
    cancelMatchAdmin,
    unCancelMatchAdmin,
    getMatchHistoryAdmin,
} from "../api/matchApi";
import { useAllMatchesAdmin } from "../hooks/useAllMatchesAdmin";
import { useSeason } from "../hooks/useSeason";
import SeasonSelect from "../components/seasons/seasonSelect";
import AdminMatchesTable from "../components/admin/AdminMatchesTable";
import AdminMatchModal from "../components/admin/AdminMatchModal";
import { useNotification } from "../context/NotificationContext";
import CancelMatchModal from "../components/admin/CancelMatchModal";
import BackButton from  "../components/BackButton";

//  DEMO notifications
import { useAppMode } from "../context/AppModeContext";
import {
    tryGetDemoNotifications,
    tryClearDemoNotifications,
} from "../api/demoApi";
import DemoNotificationsModal from "../components/demo/DemoNotificationsModal";

const hasAnyDemoItems = (demo) => {
    if (!demo) return false;
    return (demo.emails?.length ?? 0) > 0 || (demo.sms?.length ?? 0) > 0;
};

const AdminMatchesPage = () => {
    const { currentSeasonId } = useSeason();
    const { matches, loading, error, reload } = useAllMatchesAdmin(currentSeasonId);
    const { showNotification } = useNotification();

    //  DEMO mode
    const { demoMode } = useAppMode();
    const [showDemoModal, setShowDemoModal] = useState(false);
    const [demoNotifications, setDemoNotifications] = useState(null);
    const [demoLoading, setDemoLoading] = useState(false);
    const [demoError, setDemoError] = useState(null);

    const [editingMatch, setEditingMatch] = useState(null);
    const [saving, setSaving] = useState(false);
    const [serverError, setServerError] = useState(null);
    const [cancelMatch, setCancelMatch] = useState(null);
    const [cancelSaving, setCancelSaving] = useState(false);
    const [cancelError, setCancelError] = useState(null);

    const handleCloseDemoModal = () => {
        setShowDemoModal(false);
        setDemoNotifications(null);
        setDemoError(null);
    };

    const openDemoModalIfAny = useCallback(async () => {
        if (!demoMode) return;

        setDemoLoading(true);
        setDemoError(null);

        try {
            const demo = await tryGetDemoNotifications(); // GET už dělá getAndClear()
            if (hasAnyDemoItems(demo)) {
                setDemoNotifications(demo);
                setShowDemoModal(true);
            }
        } catch (e) {
            setDemoError("Nepodařilo se načíst demo notifikace.");
        } finally {
            setDemoLoading(false);
        }
    }, [demoMode]);

    const clearDemoBeforeOperation = useCallback(async () => {
        if (!demoMode) return;
        await tryClearDemoNotifications(); // v produkci endpoint není, v demo existuje
    }, [demoMode]);

    const handleCreate = () => {
        setServerError(null);
        setEditingMatch({
            id: null,
            dateTime: "",
            location: "",
            description: "",
            maxPlayers: "",
            price: "",
            matchStatus: null,
            cancelReason: null,
            matchNumber: null,
            seasonId: null,
        });
    };

    useEffect(() => {
        if (currentSeasonId == null) return;
        reload();
    }, [currentSeasonId, reload]);

    const handleEdit = useCallback((match) => {
        setServerError(null);
        setEditingMatch(match);
    }, []);

    const handleDelete = useCallback(
        async (matchId) => {
            const confirmDelete = window.confirm(
                `Opravdu chceš smazat zápas s ID ${matchId}?`
            );
            if (!confirmDelete) return;

            try {
                await clearDemoBeforeOperation();

                await deleteMatchAdmin(matchId);
                showNotification("Zápas byl smazán.", "success");

                await openDemoModalIfAny();
                await reload();
            } catch (err) {
                const message =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Smazání zápasu se nezdařilo.";
                showNotification(message, "danger");
            }
        },
        [reload, showNotification, clearDemoBeforeOperation, openDemoModalIfAny]
    );

    const handleCloseModal = () => {
        if (!saving) {
            setEditingMatch(null);
            setServerError(null);
        }
    };

    const handleCloseCancelModal = () => {
        if (!cancelSaving) {
            setCancelMatch(null);
            setCancelError(null);
        }
    };

    const handleSaveMatch = async (payload) => {
        try {
            setSaving(true);
            setServerError(null);

            await clearDemoBeforeOperation();

            if (payload.id) {
                await updateMatchAdmin(payload.id, payload);
                showNotification("Zápas byl úspěšně upraven.", "success");
            } else {
                await createMatchAdmin(payload);
                showNotification("Zápas byl úspěšně vytvořen.", "success");
            }

            setEditingMatch(null);
            setServerError(null);

            await openDemoModalIfAny();
            await reload();
        } catch (err) {
            const status = err?.response?.status;
            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Uložení zápasu se nezdařilo.";

            if (status === 400 || status === 409) {
                setServerError(message);
            } else {
                showNotification(message, "danger");
            }
        } finally {
            setSaving(false);
        }
    };

    const handleCancel = useCallback((match) => {
        setCancelError(null);
        setCancelMatch(match);
    }, []);

    const handleConfirmCancel = async (matchId, reason) => {
        try {
            setCancelSaving(true);
            setCancelError(null);

            await clearDemoBeforeOperation();

            await cancelMatchAdmin(matchId, reason);

            showNotification("Zápas byl zrušen.", "success");
            setCancelMatch(null);

            await openDemoModalIfAny();
            await reload();
        } catch (err) {
            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Zrušení zápasu se nezdařilo.";

            setCancelError(message);
        } finally {
            setCancelSaving(false);
        }
    };

    const handleUnCancel = useCallback(
        async (matchId) => {
            try {
                await clearDemoBeforeOperation();

                await unCancelMatchAdmin(matchId);
                showNotification("Zápas byl obnoven.", "success");

                await openDemoModalIfAny();
                await reload();
            } catch (err) {
                const message =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Obnovení zápasu se nezdařilo.";
                showNotification(message, "danger");
            }
        },
        [reload, showNotification, clearDemoBeforeOperation, openDemoModalIfAny]
    );

    const handleShowHistory = useCallback(
        async (matchId) => {
            try {
                const history = await getMatchHistoryAdmin(matchId);
                console.log("Match history", matchId, history);
                showNotification(
                    "Historie zápasu byla načtena (viz konzole).",
                    "info"
                );
            } catch (err) {
                const message =
                    err?.response?.data?.message ||
                    err?.message ||
                    "Historii zápasu se nepodařilo načíst.";
                showNotification(message, "danger");
            }
        },
        [showNotification]
    );

    return (
        <>
                <BackButton />
        <div className="container mt-4">
            <SeasonSelect />

            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 mb-0">Správa zápasů</h1>

                <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleCreate}
                >
                    Vytvořit nový zápas
                </button>
            </div>

            <p className="text-muted mb-3">
                Zde může administrátor a manažer spravovat zápasy v systému –
                vytvářet je, upravovat, rušit, obnovovat a mazat. Zápasy se
                vždy vztahují k aktuálně zvolené / aktivní sezóně.
            </p>

            <AdminMatchesTable
                matches={matches}
                loading={loading}
                error={error}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onCancel={handleCancel}
                onUnCancel={handleUnCancel}
                onShowHistory={handleShowHistory}
            />

            <AdminMatchModal
                match={editingMatch}
                show={!!editingMatch}
                onClose={handleCloseModal}
                onSave={handleSaveMatch}
                saving={saving}
                serverError={serverError}
            />

            <CancelMatchModal
                match={cancelMatch}
                show={!!cancelMatch}
                onClose={handleCloseCancelModal}
                onConfirm={handleConfirmCancel}
                saving={cancelSaving}
                serverError={cancelError}
            />

            {/*DEMO NOTIF – modal s demo notifikacemi jen v demo režimu */}
            {demoMode && (
                <DemoNotificationsModal
                    show={showDemoModal}
                    onClose={handleCloseDemoModal}
                    notifications={demoNotifications}
                    loading={demoLoading}
                    error={demoError}
                />
            )}
        </div>
        </>
    );
};

export default AdminMatchesPage;
