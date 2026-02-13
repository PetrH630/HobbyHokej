import React from "react";
import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
    UserIcon,
    MoneyIcon,
} from "../../icons";
import { TeamDarkIcon, TeamLightIcon } from "../../icons";
import "../MatchRegistration/MatchRegistrationInfo.css";

const AdminMatchRegistrationInfo = ({ match, onPlayerClick }) => {
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
        if (onPlayerClick) {
            onPlayerClick(player);
        }
    };

    return (
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
                        <li className="match-reg-player-empty">Žádní hráči</li>
                    )}
                    {darkPlayers.map((p) => (
                        <li key={p.id} className="match-reg-player-item">
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
                        {match.inGamePlayersLight}
                    </span>
                </div>

                <ul className="match-reg-player-list mb-1">
                    {lightPlayers.length === 0 && (
                        <li className="match-reg-player-empty">Žádní hráči</li>
                    )}
                    {lightPlayers.map((p) => (
                        <li key={p.id} className="match-reg-player-item">
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
                        <li key={p.id} className="match-reg-player-item">
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

            {/* OMLUVENÍ */}
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
                        <li key={p.id} className="match-reg-player-item">
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
                    Náhradnící -
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
                        <li key={p.id} className="match-reg-player-item">
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
                        <li key={p.id} className="match-reg-player-item">
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
                        <li key={p.id} className="match-reg-player-item">
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
                        <li key={p.id} className="match-reg-player-item">
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
    );
};

export default AdminMatchRegistrationInfo;
