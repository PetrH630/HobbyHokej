// src/components/matchRegistration/TeamSelectModal.jsx
import React from "react";
import { TeamDarkIcon, TeamLightIcon } from "../../icons";
import "./TeamSelectModal.css";
import { useGlobalModal } from "../../hooks/useGlobalModal";
import { computeTeamPositionAvailability } from "../../utils/matchPositionUtils";

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
    const darkCount = match?.inGamePlayersDark ?? 0;

    // původní max hodnoty z match
    const rawLightMax = match?.maxPlayersLight ?? match?.maxPlayers ?? 0;
    const rawDarkMax = match?.maxPlayersDark ?? match?.maxPlayers ?? 0;

    // v UI se doposud zobrazovalo "Hráči: X / (max / 2)"
    // → kapacita na tým (jak ji uživatel vidí) = rawMax / 2
    const lightCap = rawLightMax > 0 ? rawLightMax / 2 : 0;
    const darkCap = rawDarkMax > 0 ? rawDarkMax / 2 : 0;

    // celková kapacita zápasu
    const totalMaxPlayers = match?.maxPlayers ?? 0;
    const totalInGamePlayers =
        match?.inGamePlayers ?? lightCount + darkCount;

    const isLightFull = lightCap > 0 && lightCount >= lightCap;
    const isDarkFull = darkCap > 0 && darkCount >= darkCap;

    // dosažení celkové kapacity zápasu
    const isTotalFull =
        totalMaxPlayers > 0 && totalInGamePlayers >= totalMaxPlayers;

    const isLightDefault = defaultTeam === "LIGHT";
    const isDarkDefault = defaultTeam === "DARK";

    // NOVĚ: zjistíme, zda je v týmu volno už jen pro brankáře
    const lightAvailability = computeTeamPositionAvailability(match, "LIGHT");
    const darkAvailability = computeTeamPositionAvailability(match, "DARK");

    const onlyGoalieLeftLight = lightAvailability.onlyGoalieLeft;
    const onlyGoalieLeftDark = darkAvailability.onlyGoalieLeft;

    // helper: kdy ukázat info o registraci jako náhradník pro daný tým
    const showReserveNoteDark = isDarkFull || isTotalFull;
    const showReserveNoteLight = isLightFull || isTotalFull;

    const handleSelect = async (team) => {
        if (!onSelectTeam) return;

        if (onBeforeSelectTeam) {
            await onBeforeSelectTeam();
        }

        // registrace se provede až po výběru pozice v dalším modalu
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
                            Po kliknutí na tým si ještě vybereš{" "}
                            <strong>pozici pro tento zápas</strong>.
                        </p>

                        <div className="team-cards-row">
                            {/* DARK */}
                            <div
                                className={
                                    "card team-card text-center " +
                                    (isDarkDefault ? "border-primary " : "")
                                }
                                style={{ cursor: "pointer" }}
                                onClick={() => handleSelect("DARK")}
                            >
                                <div className="card-body">
                                    <div className="team-icon-wrapper">
                                        <TeamDarkIcon className="team-icon base" />
                                    </div>

                                    <p className="card-text mb-1">
                                        Hráči:{" "}
                                        <strong>
                                            {darkCount} / {darkCap}
                                        </strong>
                                    </p>
                                    <small className="text-muted d-block mb-1">
                                        Tmavé dresy
                                    </small>

                                    {showReserveNoteDark && (
                                        <small className="text-danger d-block">
                                            Kapacita týmu je plná. K tomuto týmu se
                                            budeš registrovat jako{" "}
                                            <strong>náhradník</strong>.
                                        </small>
                                    )}

                                    {onlyGoalieLeftDark && (
                                        <small className="text-danger d-block mt-1">
                                            V tomto týmu už je volné místo jen pro{" "}
                                            <strong>brankáře</strong>. Pokud nechceš chytat,
                                            můžeš se v dalším kroku přihlásit jako{" "}
                                            <strong>náhradník (obránce/útočník)</strong>.
                                        </small>
                                    )}
                                </div>
                            </div>

                            {/* LIGHT */}
                            <div
                                className={
                                    "card team-card text-center " +
                                    (isLightDefault ? "border-primary " : "")
                                }
                                style={{ cursor: "pointer" }}
                                onClick={() => handleSelect("LIGHT")}
                            >
                                <div className="card-body">
                                    <div className="team-icon-wrapper">
                                        <TeamLightIcon className="team-icon overlay" />
                                    </div>

                                    <p className="card-text mb-1">
                                        Hráči:{" "}
                                        <strong>
                                            {lightCount} / {lightCap}
                                        </strong>
                                    </p>
                                    <small className="text-muted d-block mb-1">
                                        Světlé dresy
                                    </small>

                                    {showReserveNoteLight && (
                                        <small className="text-danger d-block">
                                            Kapacita týmu je plná. K tomuto týmu se
                                            budeš registrovat jako{" "}
                                            <strong>náhradník</strong>.
                                        </small>
                                    )}

                                    {onlyGoalieLeftLight && (
                                        <small className="text-danger d-block mt-1">
                                            V tomto týmu už je volné místo jen pro{" "}
                                            <strong>brankáře</strong>. Pokud nechceš chytat,
                                            můžeš se v dalším kroku přihlásit jako{" "}
                                            <strong>náhradník (obránce/útočník)</strong>.
                                        </small>
                                    )}
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