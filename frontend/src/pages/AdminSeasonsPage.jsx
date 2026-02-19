// src/pages/AdminSeasonsPage.jsx

import { useCallback, useState } from "react";
import {
    createSeasonAdmin,
    updateSeasonAdmin,
    setActiveSeasonAdmin,
} from "../api/seasonApi";
import { useAllSeasonsAdmin } from "../hooks/useAllSeasonsAdmin";
import AdminSeasonsTable from "../components/admin/AdminSeasonsTable";
import AdminSeasonModal from "../components/admin/AdminSeasonModal";
import { useNotification } from "../context/NotificationContext";
import BackButton from "../components/BackButton";
import ConfirmActionModal from "../components/common/ConfirmActionModal";


const AdminSeasonsPage = () => {
    const { seasons, loading, error, reload } = useAllSeasonsAdmin();
    const { showNotification } = useNotification();

    const [editingSeason, setEditingSeason] = useState(null);
    const [saving, setSaving] = useState(false);
    const [serverError, setServerError] = useState(null);
    const [confirmAction, setConfirmAction] = useState(null);

    const handleCreate = () => {
        setServerError(null);
        setEditingSeason({
            id: null,
            name: "",
            startDate: "",
            endDate: "",
            active: false, // nová sezóna je defaultně neaktivní
        });
    };

    const handleEdit = useCallback((season) => {
        setServerError(null);
        setEditingSeason(season);
    }, []);

    // NOVÉ: nastavení aktivní sezóny
    const handleSetActiveSeason = useCallback(
        (seasonId) => {
            const season = seasons?.find((s) => s.id === seasonId);
            const seasonName = season?.name || `#${seasonId}`;

            setConfirmAction({
                type: "setActiveSeason",
                seasonId,
                title: "Potvrzení změny aktivní sezóny",
                message: `Opravdu chcete nastavit sezónu "${seasonName}" jako aktivní?`,
            });
        },
        [seasons]
    );


    const handleCloseModal = () => {
        if (!saving) {
            setEditingSeason(null);
            setServerError(null);
        }
    };

    const handleSaveSeason = async (payload) => {
        try {
            setSaving(true);
            setServerError(null);

            if (payload.id) {
                await updateSeasonAdmin(payload.id, payload);
                showNotification("Sezóna byla úspěšně upravena.", "success");
            } else {
                await createSeasonAdmin(payload);
                showNotification("Sezóna byla úspěšně vytvořena.", "success");
            }

            setEditingSeason(null);
            setServerError(null);
            await reload();
        } catch (err) {
            const status = err?.response?.status;
            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Uložení sezóny se nezdařilo.";

            if (status === 400 || status === 409) {
                setServerError(message);
            } else {
                showNotification(message, "danger");
            }
        } finally {
            setSaving(false);
        }
    };

    return (

        <>
            <BackButton />
        <div className="container mt-4">
         
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 mb-0">Správa sezón</h1>

                <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleCreate}
                >
                    Vytvořit novou sezónu
                </button>
            </div>

            <p className="text-muted mb-3">
                Zde může administrátor (a manažer) spravovat sezóny,
                ke kterým se vážou zápasy a statistiky.
            </p>

            <AdminSeasonsTable
                seasons={seasons}
                loading={loading}
                error={error}
                onEdit={handleEdit}
                onSetActive={handleSetActiveSeason}
            />

            <AdminSeasonModal
                season={editingSeason}
                show={!!editingSeason}
                onClose={handleCloseModal}
                onSave={handleSaveSeason}
                saving={saving}
                allSeasons={seasons}
                serverError={serverError}
            />
                {confirmAction?.type === "setActiveSeason" && (
                    <ConfirmActionModal
                        show={true}
                        title={confirmAction.title}
                        message={confirmAction.message}
                        confirmText="Nastavit jako aktivní"
                        confirmVariant="primary"
                        onClose={() => setConfirmAction(null)}
                        onConfirm={async () => {
                            try {
                                await setActiveSeasonAdmin(confirmAction.seasonId);
                                showNotification("Sezóna byla nastavena jako aktivní.", "success");
                                await reload();
                            } catch (err) {
                                const message =
                                    err?.response?.data?.message ||
                                    err?.message ||
                                    "Nepodařilo se nastavit aktivní sezónu.";
                                showNotification(message, "danger");
                            } finally {
                                setConfirmAction(null);
                            }
                        }}
                    />
                )}

      
        </div>
        </>
    );
};

export default AdminSeasonsPage;
