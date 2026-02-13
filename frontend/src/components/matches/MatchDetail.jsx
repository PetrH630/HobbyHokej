import { useState } from "react";
import MatchHeader from "./MatchHeader";
import PlayerMatchStatus from "../players/PlayerMatchStatus";
import MatchActions from "./MatchActions";
import MatchInfo from "./MatchInfo";
import TeamSelectModal from "../matchRegistration/TeamSelectModal";
import BackButton from "../BackButton";
import MatchRegistrationHistory from "../MatchRegistration/MatchRegistrationHistory";

const isMatchUpcoming = (match) => {
    if (!match || !match.dateTime) {
        // radši tlačítka zobrazit, než je vždy schovat
        return true;
    }

    const raw = match.dateTime;
    let matchDate;

    if (raw instanceof Date) {
        matchDate = raw;
    } else if (typeof raw === "string") {
        // backend posílá "2026-01-23 18:45:00"
        // → uděláme z toho "2026-01-23T18:45:00"
        const normalized = raw.includes("T") ? raw : raw.replace(" ", "T");
        matchDate = new Date(normalized);
    } else {
        // neznámý formát – radši povolit tlačítka
        return true;
    }

    if (Number.isNaN(matchDate.getTime())) {
        // špatné datum – radši povolit tlačítka
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
    const [showTeamModal, setShowTeamModal] = useState(false);
    const [showHistory, setShowHistory] = useState(false);

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
        // tady už nesaháme na API – jen otevřeme modal
        console.log("MatchDetail: klik na Přijdu → otevírám modal");
        setShowTeamModal(true);
    };

    const handleSelectTeam = async (team) => {
        console.log("MatchDetail: vybraný tým z modalu:", team);
        if (onRegister) {
            // do rodiče (MatchDetailPage) posíláme tým
            await onRegister(team);
        }
        setShowTeamModal(false);
    };

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
            <br></br>
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

            {/* tabulka se vykreslí jen když je showHistory = true */}
            {showHistory && (
                <MatchRegistrationHistory matchId={match.id} />
            )}

            {/* Modal s výběrem týmu */}
            <TeamSelectModal
                isOpen={showTeamModal}
                onClose={() => setShowTeamModal(false)}
                match={match}
                defaultTeam={defaultTeam || "LIGHT"}
                onSelectTeam={handleSelectTeam}
            />
        </div>
    );
};

export default MatchDetail;
