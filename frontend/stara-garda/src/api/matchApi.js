import api from "./axios";

// nadcházející zápasy pro currentPlayer
export const getMyUpcomingMatchesOverview = async () => {
    const res = await api.get("/matches/me/upcoming-overview", {
        withCredentials: true,
    });
    return res.data; // List<MatchOverviewDTO>
};

// uplynulé zápasy pro currentPlayer
export const getMyPassedMatchesOverview = async () => {
    const res = await api.get("/matches/me/all-passed", {
        withCredentials: true,
    });
    return res.data; // List<MatchOverviewDTO>
};

export const getMatchDetail = async (id) => {
    const res = await api.get(`/matches/${id}/detail`, {
        withCredentials: true,
    });
    return res.data; // očekáváme DTO s detailem + statusem hráče
};