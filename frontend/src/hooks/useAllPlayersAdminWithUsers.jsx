// src/hooks/useAllPlayersAdminWithUsers.js
import { useEffect, useMemo, useState } from "react";
import { useAllPlayersAdmin } from "./useAllPlayersAdmin";
import { userApi } from "../api/userApi";

/**
 * Kompozitní hook pro ADMIN:
 *
 * - načte všechny hráče (useAllPlayersAdmin)
 * - načte všechny uživatele (userApi.getAll)
 * - spojí hráče s uživateli na FE (bez zásahu do backendu)
 *
 * Výstup:
 *  - players: PlayerDTO[] rozšířené o { user }
 *  - loading
 *  - error
 *  - reload()
 */
export const useAllPlayersAdminWithUsers = () => {
    const {
        players,
        loading: playersLoading,
        error: playersError,
        reload: reloadPlayers,
    } = useAllPlayersAdmin();

    const [users, setUsers] = useState([]);
    const [usersLoading, setUsersLoading] = useState(true);
    const [usersError, setUsersError] = useState(null);

    const loadUsers = async () => {
        setUsersLoading(true);
        setUsersError(null);

        try {
            const data = await userApi.getAll();
            console.log("USERS v hooku:", data);
            setUsers(data);
            return data;
        } catch (err) {
            console.error("Nepodařilo se načíst uživatele", err);

            const message =
                err?.response?.data?.message ||
                err?.message ||
                "Nepodařilo se načíst uživatele";

            setUsersError(message);
            return null;
        } finally {
            setUsersLoading(false);
        }
    };

    useEffect(() => {
        loadUsers();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Spojení hráčů s uživateli
    const playersWithUsers = useMemo(() => {
        if (!players || players.length === 0) {
            return players;
        }
        if (!users || users.length === 0) {
            // ještě nemáme uživatele, vrať hráče bez user
            return players.map((p) => ({ ...p, user: null }));
        }

        // 1) Mapa playerId -> user podle AppUserDTO.players (pokud backend posílá hráče u uživatele)
        const playerIdToUser = new Map();

        users.forEach((u) => {
            if (Array.isArray(u.players)) {
                u.players.forEach((pl) => {
                    if (pl && pl.id != null && !playerIdToUser.has(pl.id)) {
                        playerIdToUser.set(pl.id, {
                            id: u.id,
                            name: u.name,
                            surname: u.surname,
                            email: u.email,
                        });
                    }
                });
            }
        });

        console.log("MAPA playerIdToUser:", playerIdToUser);

        return players.map((player) => {
            let user = null;

            // 1) Zkusíme najít podle player.id v mapě
            if (playerIdToUser.has(player.id)) {
                user = playerIdToUser.get(player.id);
            } else {
                // 2) fallback – match podle jména + příjmení
                //    (ty sám píšeš, že u uživatele nesmí být stejní hráči se stejným jménem + příjmením)
                user =
                    users.find(
                        (u) =>
                            u.name === player.name &&
                            u.surname === player.surname
                    ) || null;
            }

            return {
                ...player,
                user,
            };
        });
    }, [players, users]);

    return {
        players: playersWithUsers,
        loading: playersLoading || usersLoading,
        error: playersError || usersError,
        reload: async () => {
            await Promise.all([reloadPlayers(), loadUsers()]);
        },
    };
};
