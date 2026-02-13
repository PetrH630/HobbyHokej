// src/components/MatchInfo.jsx
import { useState } from "react";
import MatchRegistrationInfo from "../MatchRegistration/MatchRegistrationInfo";
import RoleGuard from "../RoleGuard";
import { useNotification } from "../../context/NotificationContext";
import {
    markNoExcusedAdmin,
    cancelNoExcusedAdmin,
} from "../../api/matchRegistrationApi";

const parseDateTime = (dt) => {
    if (!dt) return null;
    const safe = dt.replace(" ", "T");
    const d = new Date(safe);
    return Number.isNaN(d.getTime()) ? null : d;
};

const MatchInfo = ({ match, onRefresh }) => {
    const { showNotification } = useNotification();

    const [showNoExcuseModal, setShowNoExcuseModal] = useState(false);
    const [showCancelNoExcuseModal, setShowCancelNoExcuseModal] =
        useState(false);
    const [saving, setSaving] = useState(false);

    const matchDate = parseDateTime(match?.dateTime);
    const now = new Date();
    const isPastMatch = matchDate ? matchDate < now : false;

    // seznamy hráčů pro výběr
    const registeredPlayers = match?.registeredPlayers ?? [];
    const noExcusedPlayers = match?.noExcusedPlayers ?? [];

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
            setShowNoExcuseModal(false);
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
            setShowCancelNoExcuseModal(false);
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

    return (
        <div className="card">
            <div className="card-body">
              {/* HLAVIČKA – vlevo info o zápase, vpravo admin tlačítka */}
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
                    </div>

                    {/* PRAVÝ HORNÍ ROH – jen pro ADMIN/MANAGER a jen u uplynulého zápasu */}
                    <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                        {isPastMatch && (
                            <div className="d-flex flex-column gap-2 text-end">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-danger"
                                    disabled={
                                        registeredPlayers.length === 0 ||
                                        saving
                                    }
                                    onClick={() => setShowNoExcuseModal(true)}
                                >
                                    Neomluvit hráče
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-secondary"
                                    disabled={
                                        noExcusedPlayers.length === 0 ||
                                        saving
                                    }
                                    onClick={() =>
                                        setShowCancelNoExcuseModal(true)
                                    }
                                >
                                    Zrušit neomluvení
                                </button>
                            </div>
                        )}
                    </RoleGuard>
                </div>

                <h4 className="mt-4">Sestava:</h4>
                <MatchRegistrationInfo match={match} />
            </div>

            {/* MODAL – NEOMLUVIT HRÁČE */}
            {showNoExcuseModal && (
                <NoExcuseModal
                    match={match}
                    saving={saving}
                    onConfirm={handleMarkNoExcuse}
                    onClose={() => !saving && setShowNoExcuseModal(false)}
                />
            )}

            {/* MODAL – ZRUŠIT NEOMLUVENÍ */}
            {showCancelNoExcuseModal && (
                <CancelNoExcuseModal
                    match={match}
                    saving={saving}
                    onConfirm={handleCancelNoExcuse}
                    onClose={() =>
                        !saving && setShowCancelNoExcuseModal(false)
                    }
                />
            )}
        </div>
    );
};

// === MODAL: Neomluvit hráče ===
const NoExcuseModal = ({ match, saving, onConfirm, onClose }) => {
    const [selectedPlayerId, setSelectedPlayerId] = useState("");
    const [note, setNote] = useState("Nepřišel bez omluvy");
    const registered = match?.registeredPlayers ?? [];

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!selectedPlayerId) return;
        onConfirm(Number(selectedPlayerId), note);
    };

    return (
        <div className="modal d-block" tabIndex="-1">
            <div className="modal-dialog">
                <div className="modal-content">
                    <form onSubmit={handleSubmit}>
                        <div className="modal-header">
                            <h5 className="modal-title">Neomluvit hráče</h5>
                            <button
                                type="button"
                                className="btn-close"
                                onClick={onClose}
                                disabled={saving}
                            />
                        </div>
                        <div className="modal-body">
                            <p>
                                Vyber hráče, kterého chceš označit jako{" "}
                                <strong>bez omluvy</strong> pro tento zápas.
                            </p>

                            <div className="mb-3">
                                <label className="form-label">Hráč</label>
                                <select
                                    className="form-select"
                                    value={selectedPlayerId}
                                    onChange={(e) =>
                                        setSelectedPlayerId(e.target.value)
                                    }
                                    disabled={saving || registered.length === 0}
                                >
                                    <option value="">
                                        Vyber hráče…
                                    </option>
                                    {registered.map((p) => (
                                        <option key={p.id} value={p.id}>
                                            {p.fullName ??
                                                `${p.name} ${p.surname}`}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Poznámka (volitelné)</label>
                                <textarea
                                    className="form-control"
                                    rows="2"
                                    value={note}
                                    onChange={(e) => setNote(e.target.value)}
                                    disabled={saving}
                                    placeholder="Důvod neomluvení hráče…"
                                />
                                <div className="form-text">
                                    Poznámka se uloží k tomuto neomluvení hráče.
                                </div>
                            </div>


                            {registered.length === 0 && (
                                <div className="alert alert-info">
                                    Pro tento zápas nejsou k dispozici žádní
                                    registrovaní hráči.
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={onClose}
                                disabled={saving}
                            >
                                Zavřít
                            </button>
                            <button
                                type="submit"
                                className="btn btn-danger"
                                disabled={
                                    saving ||
                                    !selectedPlayerId ||
                                    registered.length === 0
                                }
                            >
                                {saving
                                    ? "Ukládám…"
                                    : "Označit jako bez omluvy"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

// === MODAL: Zrušit neomluvení ===
const CancelNoExcuseModal = ({ match, saving, onConfirm, onClose }) => {
    const [selectedPlayerId, setSelectedPlayerId] = useState("");
    const [note, setNote] = useState("Omluven - nakonec opravdu nemohl");

    const noExcused = match?.noExcusedPlayers ?? [];

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!selectedPlayerId) return;
        onConfirm(Number(selectedPlayerId), note);
    };

    return (
        <div className="modal d-block" tabIndex="-1">
            <div className="modal-dialog">
                <div className="modal-content">
                    <form onSubmit={handleSubmit}>
                        <div className="modal-header">
                            <h5 className="modal-title">
                                Zrušit neomluvení hráče
                            </h5>
                            <button
                                type="button"
                                className="btn-close"
                                onClick={onClose}
                                disabled={saving}
                            />
                        </div>
                        <div className="modal-body">
                            <p>
                                Vyber hráče, u kterého chceš zrušit neomluvení.
                            </p>

                            <div className="mb-3">
                                <label className="form-label">Hráč</label>
                                <select
                                    className="form-select"
                                    value={selectedPlayerId}
                                    onChange={(e) =>
                                        setSelectedPlayerId(e.target.value)
                                    }
                                    disabled={saving || noExcused.length === 0}
                                >
                                    <option value="">
                                        Vyber hráče…
                                    </option>
                                    {noExcused.map((p) => (
                                        <option key={p.id} value={p.id}>
                                            {p.fullName ??
                                                `${p.name} ${p.surname}`}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Poznámka k omluvení (volitelné)</label>
                                <textarea
                                    className="form-control"
                                    rows="2"
                                    value={note}
                                    onChange={(e) => setNote(e.target.value)}
                                    disabled={saving}
                                    placeholder="Důvod, proč je hráč dodatečně omluven…"
                                />
                                <div className="form-text">
                                    Poznámka se uloží k omluvení tohoto hráče.
                                </div>
                            </div>
                            {noExcused.length === 0 && (
                                <div className="alert alert-info">
                                    Pro tento zápas není nikdo označen jako
                                    „bez omluvy“.
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={onClose}
                                disabled={saving}
                            >
                                Zavřít
                            </button>
                            <button
                                type="submit"
                                className="btn btn-primary"
                                disabled={
                                    saving ||
                                    !selectedPlayerId ||
                                    noExcused.length === 0
                                }
                            >
                                {saving
                                    ? "Ukládám…"
                                    : "Zrušit neomluvení a omluvit"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default MatchInfo;
