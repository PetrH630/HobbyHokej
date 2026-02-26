// src/components/matches/MatchDetail.jsx
import { useState } from "react";
import MatchHeader from "./MatchHeader";
import PlayerMatchStatus from "../players/PlayerMatchStatus";
import MatchActions from "./MatchActions";
import MatchInfo from "./MatchInfo";
import TeamSelectModal from "../matchRegistration/TeamSelectModal";
import PlayerPositionModal from "../matchRegistration/PlayerPositionModal";
import { PlayerPosition } from "../../constants/playerPosition";
import BackButton from "../BackButton";
import MatchRegistrationHistory from "../MatchRegistration/MatchRegistrationHistory";
import { computeTeamPositionAvailability } from "../../utils/matchPositionUtils";

const isMatchUpcoming = (match) => {
    if (!match || !match.dateTime) {
        return true;
    }

    const raw = match.dateTime;
    let matchDate;

    if (raw instanceof Date) {
        matchDate = raw;
    } else if (typeof raw === "string") {
        const normalized = raw.includes("T") ? raw : raw.replace(" ", "T");
        matchDate = new Date(normalized);
    } else {
        return true;
    }

    if (Number.isNaN(matchDate.getTime())) {
        return true;
    }

    const now = new Date();
    return matchDate > now;
};

const MatchDetail = ({
    match,
    playerMatchStatus,
    matchStatus,
    loading,
    error,
    actionError,
    onRegister,
    onUnregister,
    onExcuse,
    onSubstitute,
    saving,
    isPast,
    defaultTeam,
    onRefresh,
}) => {
    console.log("MatchDetail RENDER", {
        matchId: match?.id,
        hasRegistrationsDTO: !!match?.registrations,
        registrationsLen: match?.registrations?.length,
        hasRegistrationsDarkDTO: !!match?.registeredDarkPlayers,
        darkLenDTO: match?.registeredDarkPlayers?.length,
        hasRegistrationsLightDTO: !!match?.registeredLightPlayers,
        lightLenDTO: match?.registeredLightPlayers?.length,
    });

    const [showTeamModal, setShowTeamModal] = useState(false);
    const [showHistory, setShowHistory] = useState(false);

    // dvoukrokový výběr: tým → pozice
    const [showPositionModal, setShowPositionModal] = useState(false);
    const [pendingTeam, setPendingTeam] = useState(null);

    const defaultPlayerPosition = PlayerPosition.ANY;

    // Výpočet obsazenosti a plných pozic pro zvolený tým pomocí utilu
    let positionCountsForPendingTeam = {};
    let occupiedPositionsForPendingTeam = [];
    let onlyGoalieLeftForPendingTeam = false;

    if (pendingTeam && match) {
        const { occupiedCounts, fullPositions, onlyGoalieLeft } =
            computeTeamPositionAvailability(match, pendingTeam);

        positionCountsForPendingTeam = occupiedCounts;
        occupiedPositionsForPendingTeam = fullPositions;
        onlyGoalieLeftForPendingTeam = !!onlyGoalieLeft;
    }

    if (loading) {
        return (
            <div className="container mt-4 text-center">
                <p>Načítám detail zápasu…</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mt-4 text-center">
                <p className="text-danger mb-3">{error}</p>
            </div>
        );
    }

    if (!match) {
        return (
            <div className="container mt-4 text-center">
                <p>Detail zápasu nebyl nalezen.</p>
            </div>
        );
    }

    const isUpcoming = isMatchUpcoming(match);

    const handleRegisterClick = () => {
        console.log("MatchDetail: klik na Přijdu → otevírám TeamSelectModal");
        setShowTeamModal(true);
    };

    const handleSelectTeam = (team) => {
        console.log("MatchDetail: vybraný tým z modalu:", team);
        setPendingTeam(team);
        setShowTeamModal(false);
        setShowPositionModal(true);
    };

    const handleSelectPosition = async (position) => {
        if (onRegister && pendingTeam) {
            await onRegister(pendingTeam, position);
        }

        setPendingTeam(null);
        setShowPositionModal(false);
    };

    const handleClosePositionModal = () => {
        setShowPositionModal(false);
        setPendingTeam(null);
    };

    const maxPlayers = match?.maxPlayers ?? 0;
    const inGamePlayers = match?.inGamePlayers ?? 0;
    const isCapacityFull = maxPlayers > 0 && inGamePlayers >= maxPlayers;

    return (
        <div className="container mt-3">
            <MatchHeader match={match} />

            <PlayerMatchStatus
                playerMatchStatus={match.playerMatchStatus}
                variant={isPast ? "past" : "upcoming"}
            />

            {actionError && (
                <p className="text-danger text-center mb-2">{actionError}</p>
            )}

            {!isPast && isUpcoming && (
                <MatchActions
                    playerMatchStatus={playerMatchStatus}
                    onRegister={handleRegisterClick}
                    onUnregister={onUnregister}
                    onExcuse={onExcuse}
                    onSubstitute={onSubstitute}
                    disabled={saving}
                />
            )}

            <BackButton />
            <br />
            <MatchInfo match={match} onRefresh={onRefresh} />

            <div className="d-flex justify-content-center mt-3 mb-3">
                <button
                    type="button"
                    className="btn btn-outline-secondary btn-lg"
                    onClick={() => setShowHistory((prev) => !prev)}
                >
                    {showHistory
                        ? "Skrýt historii mé registrace"
                        : "Zobrazit historii mé registrace"}
                </button>
            </div>

            {showHistory && <MatchRegistrationHistory matchId={match.id} />}

            <TeamSelectModal
                isOpen={showTeamModal}
                onClose={() => setShowTeamModal(false)}
                match={match}
                defaultTeam={defaultTeam || "LIGHT"}
                onSelectTeam={handleSelectTeam}
            />

            <PlayerPositionModal
                isOpen={showPositionModal}
                onClose={handleClosePositionModal}
                defaultPosition={defaultPlayerPosition}
                onSelectPosition={handleSelectPosition}
                matchModeKey={match.matchMode}
                occupiedPositions={occupiedPositionsForPendingTeam}
                positionCounts={positionCountsForPendingTeam}
                isCapacityFull={isCapacityFull}
                onlyGoalieLeft={onlyGoalieLeftForPendingTeam}
            />
        </div>
    );
};

export default MatchDetail;