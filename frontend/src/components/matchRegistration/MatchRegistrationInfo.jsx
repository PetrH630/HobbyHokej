// src/components/MatchRegistration/MatchRegistrationInfo.jsx
import React, { useState } from "react";
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

    // Zkusíme pokrýt různé možné názvy ID
    const currentPlayerId =
        currentPlayer?.id ?? currentPlayer?.playerId ?? null;
    const currentUserId = currentPlayer?.userId ?? null;

    const isSamePlayer = (p) => {
        // sjednocení typů – string/number
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

    // --- NOVÉ: výpočet, zda je zápas v minulosti a zda má hráč roli PLAYER ---

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

    /**
     * Stav pro potvrzení změny týmu.
     * - matchId: ID zápasu
     * - currentTeam: aktuální tým ("DARK" / "LIGHT")
     * - targetTeam: cílový tým (opačný než currentTeam)
     */
    const [pendingTeamChange, setPendingTeamChange] = useState(null);

    /**
     * Stav pro SuccessModal.
     */
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

    const handleSwitchTeamClick = (team, player) => {
        // Tlačítko je viditelné jen pro "já", ale pro jistotu:
        if (!currentPlayer || !match) return;

        // bezpečnostně: když je to v minulosti a role je PLAYER, nic nedělat
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

            // Volitelně: callback pro rodiče (např. refetch dat)
            if (typeof onSwitchTeam === "function") {
                onSwitchTeam(targetTeam, currentPlayer, updatedRegistration);
            }
        } catch (error) {
            console.error("Chyba při změně týmu:", error);
            // Později můžeš nahradit globálním toastem
            alert("Nepodařilo se změnit tým. Zkus to prosím znovu.");
        }
    };

    const handleCloseConfirmModal = () => {
        setPendingTeamChange(null);
    };

    const handleCloseSuccessModal = () => {
        setShowSuccessModal(false);
    };

    return (
        <>
            <div className="match-reg-info">
                {/* DARK */}
                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <TeamDarkIcon className="match-reg-team-icon-dark" />
                        <span className="match-reg-team-count">
                            {match.inGamePlayersDark}
                        </span>
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
                                            onClick={() =>
                                                handleSwitchTeamClick(
                                                    "DARK",
                                                    p
                                                )
                                            }
                                            disabled={isSwitchDisabled}
                                            title={
                                                isSwitchDisabled
                                                    ? "Tým nelze měnit po odehrání zápasu"
                                                    : "Klikni pro změnu týmu"
                                            }
                                        >
                                            {p.fullName}{" "}
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
                            {match.inGamePlayersLight}
                        </span>
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
                                            onClick={() =>
                                                handleSwitchTeamClick(
                                                    "LIGHT",
                                                    p
                                                )
                                            }
                                            disabled={isSwitchDisabled}
                                            title={
                                                isSwitchDisabled
                                                    ? "Tým nelze měnit po odehrání zápasu"
                                                    : "Klikni pro změnu týmu"
                                            }
                                        >
                                            {p.fullName}{" "}
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

                {/* Odhlášení */}
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

                {/* Omluvení */}
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

                {/* Náhradníci */}
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

                {/* Bez reakce */}
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

                {/* Bez omluvy */}
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

            {/* Success modal po úspěšné změně */}
            <SuccessModal
                show={showSuccessModal}
                title="Tým změněn"
                message={successMessage}
                onClose={handleCloseSuccessModal}
                closeLabel="OK"
            />
        </>
    );
};

export default MatchRegistrationInfo;