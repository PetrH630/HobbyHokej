import React, { useState } from "react";
import { useGlobalModal } from "../../hooks/useGlobalModal";

const AdminCancelNoExcuseModal = ({ match, saving, onConfirm, onClose }) => {


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
                                    <option value="">Vyber hráče…</option>
                                    {noExcused.map((p) => (
                                        <option key={p.id} value={p.id}>
                                            {p.fullName ??
                                                `${p.name} ${p.surname}`}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="mb-3">
                                <label className="form-label">
                                    Poznámka k omluvení (volitelné)
                                </label>
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

export default AdminCancelNoExcuseModal;
