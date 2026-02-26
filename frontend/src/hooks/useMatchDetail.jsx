// src/hooks/useMatchDetail.jsx
import { useEffect, useState, useCallback } from "react";
import { getMatchDetail } from "../api/matchApi";

/**
 * Namapuje jednu registraci (MatchRegistrationDTO) na objekt,
 * se kterým se dobře pracuje na FE (MatchDetail, PlayerPositionModal).
 */
const mapRegistrationToPlayer = (reg) => {
    if (!reg) return null;

    const player = reg.playerDTO || {};

    const fullName =
        player.fullName ||
        (player.name && player.surname
            ? `${player.name} ${player.surname}`
            : "Neznámý hráč");

    return {
        // identifikace hráče
        id: reg.playerId ?? player.id ?? reg.id ?? null,
        playerId: reg.playerId ?? player.id ?? null,

        // pokud bys měl userId na PlayerDTO, můžeš ho tady použít
        userId: player.userId ?? null,

        // prezentační jméno
        fullName,

        // tým a status z registrace
        team: reg.team ?? player.team ?? null,
        status: reg.status ?? null,

        // pozice v konkrétním zápase
        positionInMatch: reg.positionInMatch ?? null,

        // případně další údaje, pokud je budeš chtít někde zobrazovat
        excuseReason: reg.excuseReason ?? null,
        excuseNote: reg.excuseNote ?? null,
    };
};

/**
 * Transformace MatchDetailDTO z backendu na strukturu pro FE.
 *
 * Většinu dat necháváme tak, jak přijdou z backendu (registeredPlayers,
 * registeredDarkPlayers, reservedPlayers, ...).
 *
 * Navíc:
 * - namapujeme registrations → match.registrations (FE-friendly objekty),
 * - případně bychom tady mohli dělat další drobné úpravy.
 */
const transformMatchDetail = (dto) => {
    if (!dto) return null;

    const registrations = (dto.registrations || [])
        .map(mapRegistrationToPlayer)
        .filter(Boolean);

    return {
        // všechno z backendu zachováme
        ...dto,

        // a navíc přidáme zpracované registrace
        registrations,

        // pro jistotu explicitně předáme matchMode (už je v dto, jen ho necháme)
        matchMode: dto.matchMode ?? null,
    };
};

export const useMatchDetail = (id) => {
    const [match, setMatch] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const load = useCallback(async () => {
        if (!id) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const data = await getMatchDetail(id);
            console.log("Načtený detail zápasu (raw DTO):", data);

            const transformed = transformMatchDetail(data);
            console.log("Načtený detail zápasu (transformed):", transformed);

            setMatch(transformed);
        } catch (err) {
            console.error("Chyba při načítání detailu zápasu:", err);

            const status = err?.response?.status;
            const msg = err?.response?.data?.message;

            if (status === 404) {
                setError(msg || "Zápas nebyl nalezen.");
            } else if (status === 403) {
                setError("Nemáte oprávnění zobrazit tento zápas.");
            } else {
                setError("Nepodařilo se načíst detail zápasu.");
            }
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        load();
    }, [load]);

    return { match, loading, error, reload: load };
};