// src/api/demoNotificationsApi.js
import api from "./axios";

export const getDemoNotifications = () => {
    return api.get("/demo/notifications");
};
