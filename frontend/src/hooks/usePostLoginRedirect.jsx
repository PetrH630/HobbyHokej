// src/hooks/usePostLoginRedirect.jsx
import { useNavigate } from "react-router-dom";
import {
    autoSelectCurrentPlayer,
    getCurrentPlayer,
} from "../api/playerApi";
import { fetchCurrentUser } from "../api/authApi";

// malý helper na vytažení roli z různých tvarů objektu
const extractRoles = (user) => {
    if (!user) return [];

    let roles = [];

    // 1) user.roles – může být string, pole stringů, nebo pole objektů
    if (Array.isArray(user.roles)) {
        roles = roles.concat(
            user.roles.map((r) => {
                if (typeof r === "string") return r;
                return r.name || r.role || r.authority || null;
            }).filter(Boolean)
        );
    } else if (typeof user.roles === "string") {
        roles.push(user.roles);
    }

    // 2) user.role – fallback (string nebo pole)
    if (Array.isArray(user.role)) {
        roles = roles.concat(
            user.role.map((r) =>
                typeof r === "string" ? r : r.name || r.role || r.authority || null
            ).filter(Boolean)
        );
    } else if (typeof user.role === "string") {
        roles.push(user.role);
    }

    // 3) user.authorities – typický Spring Security tvar
    if (Array.isArray(user.authorities)) {
        roles = roles.concat(
            user.authorities
                .map((a) => a.authority || a.name || null)
                .filter(Boolean)
        );
    }

    // odstraníme duplicity
    return Array.from(new Set(roles));
};

const usePostLoginRedirect = () => {
    const navigate = useNavigate();

    const run = async () => {
        // 1) načíst usera z backendu
        let user = null;
        try {
            const res = await fetchCurrentUser();
            user = res.data;
            console.log("Post-login user:", user);
        } catch (err) {
            console.error("Nepodařilo se načíst uživatele po přihlášení", err);
        }

        const roles = extractRoles(user);
        console.log("Detekované role po loginu:", roles);

        const isAdmin = roles.includes("ROLE_ADMIN");
        // manažera na admin home:
        // const isManager = roles.includes("ROLE_MANAGER");

        // 2) Admin na "/"tam běží HomeDecider a vrátí AdminHomePage
        if (isAdmin /* || isManager */) {
            navigate("/app");
            return;
        }

        // 3) Ostatní – zachovat původní logiku s hráčem
        try {
            await autoSelectCurrentPlayer();
        } catch (err) {
            console.error("Auto-select aktuálního hráče selhal", err);
        }

        try {
            const player = await getCurrentPlayer(); // PlayerDTO nebo null

            if (player) {
                navigate("/app/matches");
            } else {
                navigate("/app/players");
            }
        } catch (err) {
            console.error("Nepodařilo se zjistit aktuálního hráče", err);
            navigate("/app/players");
        }
    };

    return run;
};

export default usePostLoginRedirect;
