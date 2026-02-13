// src/components/matchRegistration/TeamSelectModal.jsx
import React from "react";
import { TeamDarkIcon, TeamLightIcon } from "../../icons";
import "./TeamSelectModal.css";
import { useGlobalModal } from "../../hooks/useGlobalModal";

const TeamSelectModal = ({
    isOpen,
    onClose,
    match,
    defaultTeam = "LIGHT",
    onSelectTeam,
    onBeforeSelectTeam,
}) => {
 
    useGlobalModal(isOpen);

    if (!isOpen) return null;

    const lightCount = match?.inGamePlayersLight ?? 0;
    const lightMax = match?.maxPlayersLight ?? match?.maxPlayers ?? 0;

    const darkCount = match?.inGamePlayersDark ?? 0;
    const darkMax = match?.maxPlayersDark ?? match?.maxPlayers ?? 0;

    const isLightDefault = defaultTeam === "LIGHT";
    const isDarkDefault = defaultTeam === "DARK";

    const handleSelect = async (team) => {
        if (!onSelectTeam) return;
       
        if (onBeforeSelectTeam) {
            await onBeforeSelectTeam();
        }

        onSelectTeam(team);
    };

    return (
        <div
            className="modal d-block"
            tabIndex="-1"
            role="dialog"
            style={{ backgroundColor: "rgba(0,0,0,0.5)" }}
        >
            <div className="modal-dialog modal-dialog-centered" role="document">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Vyber tým pro tento zápas</h5>
                        <button type="button" className="btn-close" onClick={onClose}></button>
                    </div>

                    <div className="modal-body">
                        <p className="mb-3 text-center">
                            Po kliknutí na tým se <strong>provede registrace</strong> k zápasu.
                        </p>

                        <div className="team-cards-row">
                            {/* DARK */}
                            <div
                                className={
                                    "card team-card text-center " +
                                    (isDarkDefault ? "border-primary" : "")
                                }
                                style={{ cursor: "pointer" }}
                                onClick={() => handleSelect("DARK")}
                            >
                                <div className="card-body">
                                    <div className="team-icon-wrapper">
                                        <TeamDarkIcon className="team-icon base" />
                                    </div>

                                    <p className="card-text mb-1">
                                        Hráči: <strong>{darkCount} / {darkMax / 2}</strong>
                                    </p>
                                    <small className="text-muted">Tmavé dresy</small>
                                </div>
                            </div>

                            {/* LIGHT */}
                            <div
                                className={
                                    "card team-card text-center " +
                                    (isLightDefault ? "border-primary" : "")
                                }
                                style={{ cursor: "pointer" }}
                                onClick={() => handleSelect("LIGHT")}
                            >
                                <div className="card-body">
                                    <div className="team-icon-wrapper">
                                        <TeamLightIcon className="team-icon overlay" />
                                    </div>

                                    <p className="card-text mb-1">
                                        Hráči: <strong>{lightCount} / {lightMax / 2}</strong>
                                    </p>
                                    <small className="text-muted">Světlé dresy</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>
                            Zrušit
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TeamSelectModal;
