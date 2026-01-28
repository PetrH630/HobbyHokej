import api from "./axios";

export const getMyPlayers = async () => {
    const res = await api.get("/players/me");
    return res.data;
};

export const setCurrentPlayer = async (playerId) => {
    const res = await api.post(`/current-player/${playerId}`);
    return res.data; // nebo void, podle backendu
};

export const getCurrentPlayer = async () => {
    const res = await api.get("/current-player");
    return res.data;
};

export const createPlayer = async (data) => {
    const res = await api.post("/players/me", data);
    return res.data;
};
