import axios from "axios";

axios.defaults.withCredentials = true;

const api = axios.create({
    baseURL: "http://localhost:8080", // backend
    withCredentials: true,            // 🔥 JSESSIONID
});

export default api;
