import { useContext } from "react";
import { NotificationBadgeContext } from "../context/NotificationBadgeContext";

export const useNotificationBadge = () => {
    return useContext(NotificationBadgeContext);
};