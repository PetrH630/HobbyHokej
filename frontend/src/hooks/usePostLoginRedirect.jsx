// src/hooks/usePostLoginRedirect.jsx
import { useNavigate } from "react-router-dom";
import {
    autoSelectCurrentPlayer,
    getCurrentPlayer,
} from "../api/playerApi";
import { fetchCurrentUser } from "../api/authApi";
import { getUserSettings } from "../api/userSettingsApi";

// malý helper na vytažení roli z různých tvarů objektu
const extractRoles = (user) => {
    if (!user) return [];

    let roles = [];

    // 1) user.roles – může být string, pole stringů, nebo pole objektů
    if (Array.isArray(user.roles)) {
        roles = roles.concat(
            user.roles
                .map((r) => {
                    if (typeof r === "string") return r;
                    return r.name || r.role || r.authority || null;
                })
                .filter(Boolean)
        );
    } else if (typeof user.roles === "string") {
        roles.push(user.roles);
    }

    // 2) user.role – fallback (string nebo pole)
    if (Array.isArray(user.role)) {
        roles = roles.concat(
            user.role
                .map((r) =>
                    typeof r === "string"
                        ? r
                        : r.name || r.role || r.authority || null
                )
                .filter(Boolean)
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

// rozhodne cílovou URL pro neadmina na základě defaultLandingPage + existence hráče
const resolveTargetPathForNonAdmin = (defaultLandingPage, hasPlayer) => {
    switch (defaultLandingPage) {
        case "DASHBOARD":
            // dashboard /app/player dává smysl jen pokud nějaký aktuální hráč existuje
            return hasPlayer ? "/app/player" : "/app/players";

        case "PLAYERS":
            return "/app/players";

        case "MATCHES":
        default:
            // fallback = původní chování:
            // pokud má hráče, jdi na zápasy, jinak na hráče
            return hasPlayer ? "/app/matches" : "/app/players";
    }
};

const usePostLoginRedirect = () => {
    const navigate = useNavigate();

    const run = async () => {
        // 1) načíst usera z backendu kvůli rolím
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
        // ROLE_MANAGER se chová jako "běžný" uživatel – rozhoduje defaultLandingPage.

        // 2) Admin na "/app" – tam běží HomeDecider a vrátí AdminHomePage
        if (isAdmin) {
            navigate("/app");
            return;
        }

        // 3) Ostatní – nejdřív zkusíme autovýběr hráče dle UserSettings (playerSelectionMode)
        try {
            await autoSelectCurrentPlayer();
        } catch (err) {
            console.error("Auto-select aktuálního hráče selhal", err);
        }

        // 4) zjistíme, jestli máme aktuálního hráče
        let hasPlayer = false;
        try {
            const player = await getCurrentPlayer(); // PlayerDTO nebo null
            hasPlayer = !!player;
        } catch (err) {
            console.error("Nepodařilo se zjistit aktuálního hráče", err);
        }

        // 5) načteme UserSettings a vytáhneme defaultLandingPage
        let defaultLandingPage = null;
        try {
            const settings = await getUserSettings();
            defaultLandingPage = settings?.defaultLandingPage || null;
            console.log("UserSettings:", settings);
        } catch (err) {
            console.error(
                "Nepodařilo se načíst uživatelská nastavení (UserSettings)",
                err
            );
        }

        // validace hodnoty – kdyby někdo do DB nacpal nesmysl, spadneme na fallback
        const allowed = ["MATCHES", "PLAYERS", "DASHBOARD"];
        if (!allowed.includes(defaultLandingPage)) {
            defaultLandingPage = null;
        }

        console.log("defaultLandingPage z /api/user/settings:", defaultLandingPage);

        // 6) určime cílovou stránku pro neadmina
        const targetPath = resolveTargetPathForNonAdmin(
            defaultLandingPage,
            hasPlayer
        );

        navigate(targetPath);
    };

    return run;
};

export default usePostLoginRedirect;