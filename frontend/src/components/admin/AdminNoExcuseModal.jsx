import React, { useState } from "react";
import { useGlobalModal } from "../../hooks/useGlobalModal";

const AdminNoExcuseModal = ({ match, saving, onConfirm, onClose }) => {

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
                                    <option value="">Vyber hráče…</option>
                                    {registered.map((p) => (
                                        <option key={p.id} value={p.id}>
                                            {p.fullName ??
                                                `${p.name} ${p.surname}`}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="mb-3">
                                <label className="form-label">
                                    Poznámka (volitelné)
                                </label>
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

export default AdminNoExcuseModal;
