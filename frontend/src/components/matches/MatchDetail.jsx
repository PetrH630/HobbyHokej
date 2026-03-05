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

/**
 * MatchDetail
 *
 * Komponenta související se zápasy, registracemi a jejich zobrazením.
 *
 * Props:
 * @param {import("../../types/dto").MatchDTO} props.match Data vybraného zápasu načtená z backendu.
 * @param {string} props.playerMatchStatus data vybraného zápasu.
 * @param {Object} props.matchStatus data vybraného zápasu.
 * @param {boolean} props.loading Příznak, že probíhá načítání dat a UI má zobrazit stav načítání.
 * @param {string} props.error Chybová zpráva určená k zobrazení uživateli.
 * @param {Object} props.actionError vstupní hodnota komponenty.
 * @param {Function} props.onRegister vstupní hodnota komponenty.
 * @param {Function} props.onUnregister vstupní hodnota komponenty.
 * @param {Object} props.onExcuse vstupní hodnota komponenty.
 * @param {Object} props.onSubstitute vstupní hodnota komponenty.
 * @param {boolean} props.saving Příznak, že probíhá ukládání a akce mají být dočasně blokovány.
 * @param {Object} props.isPast vstupní hodnota komponenty.
 * @param {Object} props.defaultTeam vstupní hodnota komponenty.
 * @param {Function} props.onRefresh Callback, který se volá po úspěšné změně pro znovunačtení dat.
 */
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


    const [showPositionModal, setShowPositionModal] = useState(false);
    const [pendingTeam, setPendingTeam] = useState(null);

    const defaultPlayerPosition = PlayerPosition.ANY;


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
                match={match}
                focusTeam={pendingTeam}
                isCapacityFull={isCapacityFull}
            />
        </div>
    );
};

export default MatchDetail;