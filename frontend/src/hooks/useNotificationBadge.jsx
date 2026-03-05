import { useContext } from "react";
import { NotificationBadgeContext } from "../context/NotificationBadgeContext";

/**
 * useNotificationBadge
 *
 * Hook nad NotificationBadgeContextem pro snadné použití v komponentách.
 * Zajišťuje jednotné čtení počtu notifikací a refresh logiky.
 */
export const useNotificationBadge = () => {
    return useContext(NotificationBadgeContext);
};