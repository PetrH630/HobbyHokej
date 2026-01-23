// src/api/registrationApi.js
import api from "./axios";

/**
 * Upsert registrace pro AKTUÁLNÍHO hráče (/me/upsert)
 * - matchId: ID zápasu
 * - team: tým (např. "DARK" / "LIGHT") nebo null
 * - excuseReason: důvod omluvy (např. "ILLNESS") nebo null
 * - excuseNote: text omluvy nebo null
 * - unregister: true → odhlásit, false → registrovat / omluvit
 */
export const upsertMyRegistration = async ({
    matchId,
    team = null,
    excuseReason = null,
    excuseNote = null,
    unregister = false,
}) => {
    const res = await api.post(
        "/registrations/me/upsert",
        {
            matchId,
            team,
            adminNote: null,       // hráč nic nevyplňuje
            excuseReason,
            excuseNote,
            unregister,
        },
        { withCredentials: true }
    );

    // backend vrací MatchRegistrationDTO
    return res.data;
};
