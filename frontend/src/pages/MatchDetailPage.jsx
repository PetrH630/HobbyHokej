// src/pages/MatchDetailPage.jsx
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect, useCallback } from "react";
import { useMatchDetail } from "../hooks/useMatchDetail";
import { upsertMyRegistration } from "../api/matchRegistrationApi";
import MatchDetail from "../components/matches/MatchDetail";
import { useNotification } from "../context/NotificationContext";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";
import { ExcuseReason } from "../constants/excuseReason";

// DEMO NOTIF
import { useDemoNotifications } from "../hooks/useDemoNotifications";
import DemoNotificationsModal from "../components/demo/DemoNotificationsModal";
import { useAppMode } from "../context/AppModeContext";
import { tryClearDemoNotifications } from "../api/demoApi";


import ExcuseModal from "../components/matchRegistration/ExcuseModal";

const MatchDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const { match, loading, error, reload } = useMatchDetail(id);
    const [saving, setSaving] = useState(false);
    const [actionError, setActionError] = useState(null);

    const { showNotification } = useNotification();
    const { currentPlayer } = useCurrentPlayer();

    const playerName = currentPlayer?.fullName || "Hráč";
    const { demoMode } = useAppMode();

    // zapamatujeme si, se kterým hráčem jsme na stránku přišli
    const [initialPlayerId, setInitialPlayerId] = useState(null);

    useEffect(() => {
        if (!currentPlayer) return;
        if (initialPlayerId === null) {
            setInitialPlayerId(currentPlayer.id ?? null);
        }
    }, [currentPlayer, initialPlayerId]);

    useEffect(() => {
        if (initialPlayerId === null) return;
        if (!currentPlayer) return;

        if (currentPlayer.id !== initialPlayerId) {
            navigate("/app/matches");
        }
    }, [currentPlayer, initialPlayerId, navigate]);

    const isPast = location.state?.isPast === true;

    const playerMatchStatus = match?.playerMatchStatus ?? "NO_RESPONSE";
    const matchStatus = match?.matchStatus ?? null;

    // EXCUSE modal stav
    const [showExcuseModal, setShowExcuseModal] = useState(false);
    const [selectedReason, setSelectedReason] = useState(ExcuseReason.JINE);
    const [excuseNote, setExcuseNote] = useState("");
    const [isUnregisterFlow, setIsUnregisterFlow] = useState(false);

    // DEMO NOTIF
    const {
        notifications: demoNotifications,
        showModal: showDemoModal,
        loading: demoLoading,
        error: demoError,
        loadAndShow: loadAndShowDemoNotifications,
        close: closeDemoModal,
    } = useDemoNotifications();

    const handleCloseDemoModal = () => {
        closeDemoModal();
        navigate("/app/matches");
    };

    /**
     * DEMO: před akcí vyčistit staré notifikace, aby se "nelepily".
     * V produkci endpoint neexistuje → tryClearDemoNotifications to bezpečně ignoruje.
     */
    const clearDemoBeforeAction = useCallback(async () => {
        if (!demoMode) return;
        await tryClearDemoNotifications();
    }, [demoMode]);

    /**
     * DEMO: po akci ukázat demo notifikace; pokud žádné nejsou, rovnou přesměrovat.
     */
    const handleDemoAfterAction = useCallback(async () => {
        const result = await loadAndShowDemoNotifications();
        if (!result?.hasAny) {
            navigate("/app/matches");
        }
    }, [loadAndShowDemoNotifications, navigate]);

    const handleRegister = async (teamFromModal) => {
        if (!match) return;

        try {
            setSaving(true);
            setActionError(null);

            const team = teamFromModal || currentPlayer?.team || null;

            await clearDemoBeforeAction();

            await upsertMyRegistration({
                matchId: match.id,
                team,
                excuseReason: null,
                excuseNote: null,
                unregister: false,
                substitute: false,
            });

            showNotification(
                `${playerName} byl úspěšně přihlášen na zápas.`,
                "success"
            );

            if (demoMode) {
                await handleDemoAfterAction();
            } else {
                navigate("/app/matches");
            }
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message || "Nepodařilo se přihlásit na zápas."
            );
        } finally {
            setSaving(false);
        }
    };

    const handleSubstitute = async () => {
        if (!match) return;

        try {
            setSaving(true);
            setActionError(null);

            await clearDemoBeforeAction();

            await upsertMyRegistration({
                matchId: match.id,
                team: null,
                excuseReason: null,
                excuseNote: null,
                unregister: false,
                substitute: true,
            });

            showNotification(
                `${playerName} se přihlásil jako náhradník - možná příjde).`,
                "info"
            );

            if (demoMode) {
                await handleDemoAfterAction();
            } else {
                navigate("/app/matches");
            }
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message || "Nepodařilo se nastavit stav 'Možná'."
            );
        } finally {
            setSaving(false);
        }
    };

    const openExcuseFlow = (unregister) => {
        if (!match) return;

        setActionError(null);
        setIsUnregisterFlow(unregister);

        const backendReason = match.excuseReason;
        const backendNote = match.excuseNote;

        setSelectedReason(
            backendReason && ExcuseReason[backendReason]
                ? backendReason
                : ExcuseReason.JINE
        );

        setExcuseNote(
            backendNote ??
            (unregister ? "Odhlášení ze zápasu." : "Nemohu se zúčastnit.")
        );

        setShowExcuseModal(true);
    };

    const handleUnregister = () => openExcuseFlow(true);
    const openExcuseModal = () => openExcuseFlow(false);

    const handleSubmitExcuse = async () => {
        if (!match) return;

        try {
            setSaving(true);
            setActionError(null);

            await clearDemoBeforeAction();

            await upsertMyRegistration({
                matchId: match.id,
                team: null,
                excuseReason: selectedReason,
                excuseNote,
                unregister: isUnregisterFlow,
            });

            if (isUnregisterFlow) {
                showNotification(`${playerName} byl odhlášen ze zápasu.`, "info");
            } else {
                showNotification(`${playerName} se omluvil ze zápasu.`, "warning");
            }

            setShowExcuseModal(false);
            setIsUnregisterFlow(false);

            if (demoMode) {
                await handleDemoAfterAction();
            } else {
                navigate("/app/matches");
            }
        } catch (err) {
            console.error(err);
            setActionError(
                err?.response?.data?.message ||
                (isUnregisterFlow
                    ? "Nepodařilo se odhlásit ze zápasu."
                    : "Nepodařilo se omluvit ze zápasu.")
            );
        } finally {
            setSaving(false);
        }
    };

    const handleCloseExcuseModal = () => {
        if (saving) return;
        setShowExcuseModal(false);
        setIsUnregisterFlow(false);
    };

    return (
        <>
            <MatchDetail
                match={match}
                playerMatchStatus={playerMatchStatus}
                matchStatus={matchStatus}
                loading={loading}
                error={error}
                actionError={actionError}
                onRegister={handleRegister}
                onUnregister={handleUnregister}
                onExcuse={openExcuseModal}
                onSubstitute={handleSubstitute}
                saving={saving}
                isPast={isPast}
                defaultTeam={currentPlayer?.team || "LIGHT"}
                onRefresh={reload}
            />

            <ExcuseModal
                show={showExcuseModal}
                saving={saving}
                isUnregisterFlow={isUnregisterFlow}
                selectedReason={selectedReason}
                excuseNote={excuseNote}
                onChangeReason={setSelectedReason}
                onChangeNote={setExcuseNote}
                onClose={handleCloseExcuseModal}
                onSubmit={handleSubmitExcuse}
            />

            {/* DEMO NOTIF – modal s demo notifikacemi jen v demo režimu */}
            {demoMode && (
                <DemoNotificationsModal
                    show={showDemoModal}
                    onClose={handleCloseDemoModal}
                    notifications={demoNotifications}
                    loading={demoLoading}
                    error={demoError}
                />
            )}
        </>
    );
};

export default MatchDetailPage;
