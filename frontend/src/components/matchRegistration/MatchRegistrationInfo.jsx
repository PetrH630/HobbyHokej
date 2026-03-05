import React, { useState, useEffect } from "react";
import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
    UserIcon,
    MoneyIcon,
    TeamDarkIcon,
    TeamLightIcon,
} from "../../icons";
import "./MatchRegistrationInfo.css";
import ConfirmActionModal from "../common/ConfirmActionModal";
import SuccessModal from "../common/SuccessModal";
import { changeMyRegistrationTeam } from "../../api/matchRegistrationApi";
import * as bootstrap from "bootstrap";
import PositionModalView from "./PositionModalView";

/**
 * MatchRegistrationInfo
 *
 * Komponenta související se zápasy, registracemi a jejich zobrazením.
 *
 * Props:
 * @param {import("../../types/dto").MatchDTO} props.match Data vybraného zápasu načtená z backendu.
 * @param {import("../../types/dto").PlayerDTO} props.currentPlayer Aktuálně zvolený hráč (přihlášený nebo vybraný v aplikaci).
 * @param {Object} props.onSwitchTeam callback pro předání akce do nadřazené vrstvy.
 */
const MatchRegistrationInfo = ({ match, currentPlayer, onSwitchTeam }) => {
    const darkPlayers = match?.registeredDarkPlayers ?? [];
    const lightPlayers = match?.registeredLightPlayers ?? [];
    const reservedPlayers = match?.reservedPlayers ?? [];
    const excusedPlayers = match?.excusedPlayers ?? [];
    const unregisteredPlayers = match?.unregisteredPlayers ?? [];
    const substitudedPlayers = match?.substitutedPlayers ?? [];
    const noExcusedPlayers = match?.noExcusedPlayers ?? [];
    const noResponsePlayers = match?.noResponsePlayers ?? [];
    const noActionPlayers = match?.noActionPlayers ?? 0;

    const registered = match?.registeredPlayers ?? [];


    const currentPlayerId =
        currentPlayer?.id ?? currentPlayer?.playerId ?? null;
    const currentUserId = currentPlayer?.userId ?? null;

    const isSamePlayer = (p) => {

        const pId = p.id ?? p.playerId ?? null;
        const pUserId = p.userId ?? null;

        const sameById =
            currentPlayerId != null &&
            pId != null &&
            String(pId) === String(currentPlayerId);

        const sameByUserId =
            currentUserId != null &&
            pUserId != null &&
            String(pUserId) === String(currentUserId);

        return sameById || sameByUserId;
    };


    const parseDateTime = (dt) => {
        if (!dt) return null;
        const safe = dt.replace(" ", "T");
        const d = new Date(safe);
        return Number.isNaN(d.getTime()) ? null : d;
    };

    const matchDate = parseDateTime(match?.dateTime);
    const now = new Date();
    const isPastMatch = matchDate ? matchDate < now : false;

    const isSwitchDisabled = isPastMatch;


    const [pendingTeamChange, setPendingTeamChange] = useState(null);


    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");


    const [showLayoutModal, setShowLayoutModal] = useState(false);
    const [layoutFocusTeam, setLayoutFocusTeam] = useState(null);

    
    const handleSwitchTeamClick = (team, player) => {

        if (!currentPlayer || !match) return;


        if (isSwitchDisabled) return;

        const matchId = match.id ?? match.matchId ?? null;
        if (!matchId) {
            console.warn(
                "MatchRegistrationInfo: chybí match.id nebo match.matchId"
            );
            return;
        }

        const targetTeam = team === "DARK" ? "LIGHT" : "DARK";

        setPendingTeamChange({
            matchId,
            currentTeam: team,
            targetTeam,
        });
    };

    
    const handleConfirmChangeTeam = async () => {
        if (!pendingTeamChange) return;

        const { matchId, targetTeam } = pendingTeamChange;

        try {
            const updatedRegistration = await changeMyRegistrationTeam(matchId);

            setPendingTeamChange(null);
            setSuccessMessage(`Tým byl úspěšně změněn na ${targetTeam}.`);
            setShowSuccessModal(true);


            if (typeof onSwitchTeam === "function") {
                onSwitchTeam(targetTeam, currentPlayer, updatedRegistration);
            }
        } catch (error) {
            console.error("Chyba při změně týmu:", error);

            alert("Nepodařilo se změnit tým. Zkus to prosím znovu.");
        }
    };

    
    const handleCloseConfirmModal = () => {
        setPendingTeamChange(null);
    };

    
    const handleCloseSuccessModal = () => {
        setShowSuccessModal(false);
    };

    useEffect(() => {
        const tooltipTriggerList = [].slice.call(
            document.querySelectorAll('[data-bs-toggle="tooltip"]')
        );
        tooltipTriggerList.map(
            (tooltipTriggerEl) => new bootstrap.Tooltip(tooltipTriggerEl)
        );
    }, [match]);


    return (
        <>
            <div className="match-reg-info">

                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <TeamDarkIcon className="match-reg-team-icon-dark" />
                        <span className="match-reg-team-count">
                            {match.inGamePlayersDark} {" / "} {match.maxPlayers / 2}
                        </span>

                        {/* NOVÉ: tlačítko pro zobrazení rozložení hráčů */}
                        <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary ms-2 match-reg-layout-btn"
                            onClick={() => { setLayoutFocusTeam("DARK"); setShowLayoutModal(true); }}
                            >
                            Grafika
                        </button>
                    </div>

                    <ul className="match-reg-player-list">
                        {darkPlayers.length === 0 && (
                            <li className="match-reg-player-empty">
                                Žádní hráči
                            </li>
                        )}
                        {darkPlayers.map((p) => {
                            const isCurrent = currentPlayer && isSamePlayer(p);

                            return (
                                <li
                                    key={p.id ?? p.playerId}
                                    className="match-reg-player-item"
                                >
                                    {isCurrent ? (
                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-primary w-100 text-start match-reg-switch-btn"
                                            onClick={() => handleSwitchTeamClick("DARK", p)}
                                            disabled
                                        >
                                            <span className="match-reg-player-name">
                                                {p.fullName}{" "}
                                            </span>
                                        </button>
                                    ) : (
                                        p.fullName
                                    )}
                                </li>
                            );
                        })}
                    </ul>
                </div>

                {/* LIGHT */}
                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <TeamLightIcon className="match-reg-team-icon-light" />
                        <span className="match-reg-team-count">
                            {match.inGamePlayersLight} {" / "} {match.maxPlayers / 2}
                        </span>

                        {/* NOVÉ: tlačítko pro zobrazení rozložení hráčů */}
                        <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary ms-2 match-reg-layout-btn"
                            onClick={() => { setLayoutFocusTeam("LIGHT"); setShowLayoutModal(true); }}
                            >
                            Grafika
                        </button>
                    </div>

                    <ul className="match-reg-player-list mb-1">
                        {lightPlayers.length === 0 && (
                            <li className="match-reg-player-empty">
                                Žádní hráči
                            </li>
                        )}
                        {lightPlayers.map((p) => {
                            const isCurrent = currentPlayer && isSamePlayer(p);

                            return (
                                <li
                                    key={p.id ?? p.playerId}
                                    className="match-reg-player-item"
                                >
                                    {isCurrent ? (
                                        <button
                                            type="button"
                                            className="btn btn-sm btn-outline-primary w-100 text-start match-reg-switch-btn"
                                            onClick={() => handleSwitchTeamClick("LIGHT", p)}
                                            disabled
                                        >
                                            <span className="match-reg-player-name">
                                                {p.fullName}{" "}
                                            </span>
                                        </button>
                                    ) : (
                                        p.fullName
                                    )}
                                </li>
                            );
                        })}
                    </ul>
                </div>

                <div className="match-reg-other-col"></div>
                <h5>Ostatní statusy:</h5>
                <div className="match-reg-other-col"></div>

                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <UnregisteredIcon className="match-unregistered-r" />
                        Odhlášení -
                        <span className="match-reg-team-count">
                            {unregisteredPlayers.length}
                        </span>
                    </div>

                    <ul className="match-reg-player-list">
                        {unregisteredPlayers.length === 0 && (
                            <li className="match-reg-player-empty">
                                nikdo se neodhlásil
                            </li>
                        )}
                        {unregisteredPlayers.map((p) => (
                            <li
                                key={p.id ?? p.playerId}
                                className="match-reg-player-item"
                            >
                                {p.fullName}
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <ExcusedIcon className="match-excused-r" />
                        Omluvení -
                        <span className="match-reg-team-count">
                            {excusedPlayers.length}
                        </span>
                    </div>

                    <ul className="match-reg-player-list">
                        {excusedPlayers.length === 0 && (
                            <li className="match-reg-player-empty">
                                nikdo se neomluvil
                            </li>
                        )}
                        {excusedPlayers.map((p) => (
                            <li
                                key={p.id ?? p.playerId}
                                className="match-reg-player-item"
                            >
                                {p.fullName}
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <ReservedIcon className="match-reserved-r" />
                        Náhradníci -
                        <span className="match-reg-team-count">
                            {reservedPlayers.length}
                        </span>
                    </div>

                    <ul className="match-reg-player-list">
                        {reservedPlayers.length === 0 && (
                            <li className="match-reg-player-empty">
                                žádný náhradník
                            </li>
                        )}
                        {reservedPlayers.map((p) => (
                            <li
                                key={p.id ?? p.playerId}
                                className="match-reg-player-item"
                            >
                                {p.fullName}
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="match-reg-other-col"></div>

                {/* Možná */}
                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <NoResponseIcon className="match-no-response-r" />
                        Možná budou -
                        <span className="match-reg-team-count">
                            {substitudedPlayers.length}
                        </span>
                    </div>

                    <ul className="match-reg-player-list">
                        {substitudedPlayers.length === 0 && (
                            <li className="match-reg-player-empty">
                                žádný náhradník
                            </li>
                        )}
                        {substitudedPlayers.map((p) => (
                            <li
                                key={p.id ?? p.playerId}
                                className="match-reg-player-item"
                            >
                                {p.fullName}
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <NoResponseIcon className="match-no-response-r" />
                        Zatím nereagovali -
                        <span className="match-reg-team-count">
                            {noActionPlayers}
                        </span>
                    </div>

                    <ul className="match-reg-player-list">
                        {noActionPlayers === 0 && (
                            <li className="match-reg-player-empty">
                                všichni reagovali
                            </li>
                        )}

                        {noResponsePlayers.map((p) => (
                            <li
                                key={p.id ?? p.playerId}
                                className="match-reg-player-item"
                            >
                                {p.fullName}
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <NoExcusedIcon className="match-no-excused-r" />
                        Bez omluvy -
                        <span className="match-reg-team-count">
                            {noExcusedPlayers.length}
                        </span>
                    </div>

                    <ul className="match-reg-player-list">
                        {noExcusedPlayers.length === 0 && (
                            <li className="match-reg-player-empty">vše OK</li>
                        )}
                        {noExcusedPlayers.map((p) => (
                            <li
                                key={p.id ?? p.playerId}
                                className="match-reg-player-item"
                            >
                                {p.fullName}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>

            {/* Potvrzovací modal pro změnu týmu */}
            <ConfirmActionModal
                show={!!pendingTeamChange}
                title="Změna týmu"
                message={
                    pendingTeamChange
                        ? `Opravdu chceš změnit tým na ${pendingTeamChange.targetTeam}?`
                        : ""
                }
                confirmText="Změnit tým"
                confirmVariant="primary"
                onConfirm={handleConfirmChangeTeam}
                onClose={handleCloseConfirmModal}
            />

            <SuccessModal
                show={showSuccessModal}
                title="Tým změněn"
                message={successMessage}
                onClose={handleCloseSuccessModal}
                closeLabel="OK"
            />

            <ConfirmActionModal
                show={showLayoutModal}
                title="Rozložení hráčů"
                message={
                    "Zde bude grafické rozložení hráčů na ledě pro aktuální zápas. " +
                    "Později sem doplníme schéma a vizualizaci."
                }
                confirmText="Zavřít"
                confirmVariant="secondary"
                onConfirm={() => setShowLayoutModal(false)}
                onClose={() => setShowLayoutModal(false)}
            />
            <PositionModalView
                isOpen={showLayoutModal}
                onClose={() => setShowLayoutModal(false)}
                match={match}
                focusTeam={layoutFocusTeam}
                currentPlayer={currentPlayer}
            />
        </>
    );
};

export default MatchRegistrationInfo;