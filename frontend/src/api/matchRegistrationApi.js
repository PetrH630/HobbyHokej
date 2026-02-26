// src/api/matchRegistrationApi.js
import api from "./axios";

/**
 * Upsert registrace pro AKTUÁLNÍHO hráče (/me/upsert)
 * - matchId: ID zápasu
 * - team: tým (např. "DARK" / "LIGHT") nebo null
 * - excuseReason: důvod omluvy
 * - excuseNote: text omluvy nebo null
 * - unregister: true → odhlásit, false → registrovat / omluvit
 * - substitute: true → náhradník
 * - positionInMatch: enum pozice hráče v zápase (např. "WING_LEFT")
 */
export const upsertMyRegistration = async ({
    matchId,
    team = null,
    excuseReason = null,
    excuseNote = null,
    unregister = false,
    substitute = false,
    positionInMatch = null,   // 🔹 NOVÝ PARAMETR
}) => {
    const res = await api.post(
        "/registrations/me/upsert",
        {
            matchId,
            team,
            adminNote: null,
            excuseReason,
            excuseNote,
            unregister,
            substitute,
            positionInMatch,    // 🔹 POSÍLÁME DO BACKENDU
        },
        { withCredentials: true }
    );
    return res.data;
};
 
/**
 * ADMIN/MANAGER: označí hráče jako NO_EXCUSED
 * PATCH /api/matches/match/{matchId}/players/{playerId}/no-excused
 *
 * POZOR: podle controlleru máš @RequestMapping("/api/matches")
 * a @PatchMapping("/match/{matchId}/players/{playerId}/no-excused"),
 * takže výsledná cesta je /api/matches/match/...
 */
export const markNoExcusedAdmin = async (matchId, playerId, adminNote) => {
    const res = await api.patch(
        `/registrations/match/${matchId}/players/${playerId}/no-excused`,
        null,
        {
            params: adminNote ? { adminNote } : {},
        }
    );
    return res.data;
};

/**
 * ADMIN/MANAGER: zruší NO_EXCUSED a označí hráče jako EXCUSED
 * s důvodem JINÉ a poznámkou „Omluven - nakonec opravdu nemohl“.
 *
 * Backend endpoint si můžeš udělat např.
 * @PatchMapping("/match/{matchId}/players/{playerId}/cancel-no-excused")
 */
export const cancelNoExcusedAdmin = async (matchId, playerId, excuseNote) => {
    const res = await api.patch(
        `/registrations/match/${matchId}/players/${playerId}/cancel-no-excused`,
        null,
        {
            params: {
                excuseReason: "JINE", 
                excuseNote,
            },
        }
    );
    return res.data;
};

/**
 * Změní tým aktuálního hráče u daného zápasu.
 *
 * PATCH /api/registrations/me/{matchId}/change-team
 */
export const changeMyRegistrationTeam = async (matchId) => {
    const res = await api.patch(
        `/registrations/me/${matchId}/change-team`,
        null,
        { withCredentials: true }
    );
    return res.data;
};

/**
 * TODO: ADMIN/MANAGER – změní tým hráče (dle playerId) na opačný v rámci daného zápasu.
 *
 * PATCH /api/registrations/{playerId}/{matchId}/change-team
 */
export const changeRegistrationTeamAdmin = async (playerId, matchId) => {
    const res = await api.patch(
        `/registrations/${playerId}/${matchId}/change-team`,
        null
    );
    return res.data;
};


