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

export const register = async (data) => {
    const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        // načteme JSON z response
        const errorData = await response.json(); // ← tady definujeme proměnnou
        throw new Error(errorData.message);       // použijeme správně
    }

    return response.json();
};
