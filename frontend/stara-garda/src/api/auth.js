import api from "./axios";

// 🔹 kontrola přihlášení
export const checkAuth = async () => {
    try {
        await api.get("/api/players", { withCredentials: true }); // chráněný endpoint
        return true;
    } catch {
        return false;
    }
};

export const getCurrentUser = async () => {
    const res = await api.get("/api/auth/me", { withCredentials: true });
    return res.data; // očekáváme AppUserDTO
};


const getMatchDetail = async (id) => {
    try {
        const res = await api.get(`/api/matches/matchDetail/${id}`, { withCredentials: true });
        return res.data;
    } catch (err) {
        console.error(err.response?.status, err.response?.data);
    }
};



// 🔹 logout
export const logout = async () => {
    await api.post("api/logout");
    
    window.location.href = "/login";
};

// 🔹 login přes JSON
export const login = async (email, password) => {
 
    // posíláme JSON
    return api.post("/api/login", { email, password });
};
