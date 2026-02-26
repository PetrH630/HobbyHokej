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

    // logika povolení/zakázání karet:
    // - standardně vypneme kartu, když je tým plný
    // - pokud je plný i druhý tým a současně je dosažen maxPlayers,
    //   tak se karty opět povolí (výběr týmu pro náhradníka)
    let isLightDisabled = false;
    let isDarkDisabled = false;

    if (!isTotalFull) {
        isLightDisabled = isLightFull;
        isDarkDisabled = isDarkFull;
    } else {
        isLightDisabled = false;
        isDarkDisabled = false;
    }

    const isLightDefault = defaultTeam === "LIGHT";
    const isDarkDefault = defaultTeam === "DARK";

    // 🔹 NOVĚ: zjistíme, zda je v týmu volno už jen pro brankáře
    const lightAvailability = computeTeamPositionAvailability(match, "LIGHT");
    const darkAvailability = computeTeamPositionAvailability(match, "DARK");

    const onlyGoalieLeftLight = lightAvailability.onlyGoalieLeft;
    const onlyGoalieLeftDark = darkAvailability.onlyGoalieLeft;

    const handleSelect = async (team, disabled) => {
        if (disabled) return;
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
                                    (isDarkDefault ? "border-primary " : "") +
                                    (isDarkDisabled ? " team-card-disabled" : "")
                                }
                                style={{ cursor: isDarkDisabled ? "not-allowed" : "pointer" }}
                                onClick={() => handleSelect("DARK", isDarkDisabled)}
                                aria-disabled={isDarkDisabled}
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
                                        {isDarkDisabled && !isTotalFull
                                            ? " • kapacita plná"
                                            : ""}
                                    </small>

                                    {onlyGoalieLeftDark && (
                                        <small className="text-danger d-block">
                                            V tomto týmu už je volné místo jen pro{" "}
                                            <strong>brankáře</strong>. Pokud nechceš chytat můžeš se v dalším kroku přihlásit jako{" "}
                                            <strong>náhradník (obránce/útočník)</strong>.
                                        </small>
                                    )}
                                </div>
                            </div>

                            {/* LIGHT */}
                            <div
                                className={
                                    "card team-card text-center " +
                                    (isLightDefault ? "border-primary " : "") +
                                    (isLightDisabled ? " team-card-disabled" : "")
                                }
                                style={{ cursor: isLightDisabled ? "not-allowed" : "pointer" }}
                                onClick={() => handleSelect("LIGHT", isLightDisabled)}
                                aria-disabled={isLightDisabled}
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
                                        {isLightDisabled && !isTotalFull
                                            ? " • kapacita plná"
                                            : ""}
                                    </small>
 
                                    {onlyGoalieLeftLight && (
                                        <small className="text-danger d-block">
                                            V tomto týmu už je volné místo jen pro{" "}
                                            <strong>brankáře</strong>. Pokud nechceš chytat můžeš se v dalším kroku přihlásit jako{" "}
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