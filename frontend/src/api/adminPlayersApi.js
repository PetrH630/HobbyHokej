// src/api/adminPlayersApi.js
import api from "./axios";

export const fetchAllPlayersAdmin = async () => {
    return api.get("/players"); 
};
