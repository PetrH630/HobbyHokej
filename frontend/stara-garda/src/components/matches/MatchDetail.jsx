// src/components/MatchDetail.jsx
import MatchHeader from "./MatchHeader";
import PlayerMatchStatus from "../players/PlayerMatchStatus";
import MatchActions from "./MatchActions";
import MatchInfo from "./MatchInfo";

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
}) => {
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

    return (
        <div className="container mt-3">
            <MatchHeader match={match} />

            <PlayerMatchStatus playerMatchStatus={match.playerMatchStatus}
                variant={isPast ? "past" : "upcoming"} />

            {actionError && (
                <p className="text-danger text-center mb-2">{actionError}</p>
            )}

            {!isPast && (
                <MatchActions
                    playerMatchStatus={playerMatchStatus}
                    onRegister={onRegister}
                    onUnregister={onUnregister}
                    onExcuse={onExcuse}
                    onSubstitute={onSubstitute}
                    disabled={saving}
                />
            )}

            <MatchInfo match={match} />
        </div>
    );
};

export default MatchDetail;
