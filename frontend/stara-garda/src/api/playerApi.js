import api from "./axios";

export const getMyPlayers = () => 
    api.get("/players/me");

export const setCurrentPlayer = (playerId) =>
    api.post(`/current-player/${playerId}`);

export const getCurrentPlayer = () =>
    api.get(`/current-player`);

export const createPlayer = (data) => 
    api.post("/players/me", data);