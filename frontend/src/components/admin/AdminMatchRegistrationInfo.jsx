import React, { useState } from "react";
import {
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
    TeamDarkIcon,
    TeamLightIcon,
} from "../../icons";
import "../MatchRegistration/MatchRegistrationInfo.css";
import PositionModalView from "../matchRegistration/PositionModalView";

/**
 * AdminMatchRegistrationInfo
 *
 * Komponenta související se zápasy, registracemi a jejich zobrazením.
 *
 * Props:
 * @param {import("../../types/dto").MatchDTO} props.match Data vybraného zápasu načtená z backendu.
 * @param {Object} props.onPlayerClick data hráče nebo identifikátor aktuálního hráče.
 * @param {import("../../types/dto").PlayerDTO} props.currentPlayer Aktuálně zvolený hráč (přihlášený nebo vybraný v aplikaci).
 */

const AdminMatchRegistrationInfo = ({ match, onPlayerClick, currentPlayer }) => {
    const darkPlayers = match?.registeredDarkPlayers ?? [];
    const lightPlayers = match?.registeredLightPlayers ?? [];
    const reservedPlayers = match?.reservedPlayers ?? [];
    const excusedPlayers = match?.excusedPlayers ?? [];
    const unregisteredPlayers = match?.unregisteredPlayers ?? [];
    const substitudedPlayers = match?.substitutedPlayers ?? [];
    const noExcusedPlayers = match?.noExcusedPlayers ?? [];
    const noResponsePlayers = match?.noResponsePlayers ?? [];
    const noActionPlayers = match?.noActionPlayers ?? 0;

    const handlePlayerClick = (player) => {
        if (typeof onPlayerClick === "function") {
            onPlayerClick(player);
        }
    };

    /**
     * Stav pro modal s rozložením hráčů.
     */
    const [showLayoutModal, setShowLayoutModal] = useState(false);
    const [layoutFocusTeam, setLayoutFocusTeam] = useState(null);

    const openLayout = (team) => {
        setLayoutFocusTeam(team);
        setShowLayoutModal(true);
    };

    const closeLayout = () => {
        setShowLayoutModal(false);
    };

    return (
        <>
            <div className="match-reg-info">
                {/* DARK */}
                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <TeamDarkIcon className="match-reg-team-icon-dark" />
                        <span className="match-reg-team-count">
                            {match?.inGamePlayersDark ?? 0}
                        </span>

                        <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary ms-2 match-reg-layout-btn"
                            onClick={() => openLayout("DARK")}
                        >
                            Grafika
                        </button>
                    </div>

                    <ul className="match-reg-player-list">
                        {darkPlayers.length === 0 && (
                            <li className="match-reg-player-empty">Žádní hráči</li>
                        )}
                        {darkPlayers.map((p) => (
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>

                {/* LIGHT */}
                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <TeamLightIcon className="match-reg-team-icon-light" />
                        <span className="match-reg-team-count">
                            {match?.inGamePlayersLight ?? 0}
                        </span>

                        <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary ms-2 match-reg-layout-btn"
                            onClick={() => openLayout("LIGHT")}
                        >
                            Grafika
                        </button>
                    </div>

                    <ul className="match-reg-player-list mb-1">
                        {lightPlayers.length === 0 && (
                            <li className="match-reg-player-empty">Žádní hráči</li>
                        )}
                        {lightPlayers.map((p) => (
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
                            </li>
                        ))}
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
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
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
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
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
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="match-reg-other-col"></div>

                {/* Možná budou */}
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
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>

                {/* Bez reakce */}
                <div className="match-reg-team-col">
                    <div className="match-reg-team-header">
                        <NoResponseIcon className="match-no-response-r" />
                        Zatím nereagovali -
                        <span className="match-reg-team-count">{noActionPlayers}</span>
                    </div>

                    <ul className="match-reg-player-list">
                        {noActionPlayers === 0 && (
                            <li className="match-reg-player-empty">
                                všichni reagovali
                            </li>
                        )}

                        {noResponsePlayers.map((p) => (
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
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
                            <li key={p.id ?? p.playerId} className="match-reg-player-item">
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary match-reg-player-button"
                                    onClick={() => handlePlayerClick(p)}
                                >
                                    {p.fullName}
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            </div>

            <PositionModalView
                isOpen={showLayoutModal}
                onClose={closeLayout}
                match={match}
                focusTeam={layoutFocusTeam}
                currentPlayer={currentPlayer}
            />
        </>
    );
};

export default AdminMatchRegistrationInfo;