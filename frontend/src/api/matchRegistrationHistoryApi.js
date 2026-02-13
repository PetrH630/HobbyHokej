// src/api/matchRegistrationHistoryAdmin
import api from "./axios";

// historie registraci hráče na zápas dle id zápasu
export const getMyMatchRegistrationHistory = async (matchId) => {
    const res = await api.get(`/registrations/history/me/matches/${matchId}`,{
    withCredentials: true,
    });
    return res.data; // List<MatchRegistrationHistoryDTO> getHistoryForCurrentPlayerAndMatch(Long matchId)
};

// ADMIN: historie registrací konkrétního hráče v konkrétním zápase
// Upravenou URL přizpůsob svému backendu
export const getPlayerRegistrationHistoryAdmin = async (matchId, playerId) => {
    const res = await api.get(
        `/registrations/history/admin/matches/${matchId}/players/${playerId}`
    );
    return res.data;
};