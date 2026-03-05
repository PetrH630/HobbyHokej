// src/components/admin/AdminMatchInfo.jsx
import { useState } from "react";
import AdminMatchRegistrationInfo from "./AdminMatchRegistrationInfo";
import RoleGuard from "../RoleGuard";
import { useNotification } from "../../context/NotificationContext";
import {
    markNoExcusedAdmin,
    cancelNoExcusedAdmin,
    // TODO: import admin změny týmu
    changeRegistrationTeamAdmin,
} from "../../api/matchRegistrationApi";
import AdminPlayerRegistrationHistoryModal from "./AdminPlayerRegistrationHistoryModal";
import {
    autoLineupAdmin,
    updateMatchScoreAdmin,
} from "../../api/matchApi";
import {
    RegisteredIcon,
    UnregisteredIcon,
    ExcusedIcon,
    ReservedIcon,
    NoResponseIcon,
    NoExcusedIcon,
    UserIcon,
    MoneyIcon,
    TeamDarkIcon,
    TeamLightIcon,
} from "../../icons";
import "./AdminMatchInfo.css";

const parseDateTime = (dt) => {
    if (!dt) return null;
    const safe = dt.replace(" ", "T");
    const d = new Date(safe);
    return Number.isNaN(d.getTime()) ? null : d;
};

/**
 * AdminMatchInfo
 *
 * Komponenta související se zápasy, registracemi a jejich zobrazením.
 *
 * Props:
 * @param {import("../../types/dto").MatchDTO} props.match Data vybraného zápasu načtená z backendu.
 * @param {Function} props.onRefresh Callback, který se volá po úspěšné změně pro znovunačtení dat.
 */

const AdminMatchInfo = ({ match, onRefresh }) => {
    const { showNotification } = useNotification();

    const [saving, setSaving] = useState(false);
    const [historyPlayer, setHistoryPlayer] = useState(null);
    const [scoreLightInput, setScoreLightInput] = useState(
        match?.scoreLight ?? ""
    );
    const [scoreDarkInput, setScoreDarkInput] = useState(match?.scoreDark ?? "")

    const matchDate = parseDateTime(match?.dateTime);
    const now = new Date();
    
/**
 * Určí, zda zápas již proběhl na základě data a času začátku.
 */

const isPastMatch = matchDate ? matchDate < now : false;
    const scoreDark = match?.scoreDark;
    const scoreLight = match?.scoreLight;

    const hasScore =
        scoreDark != null &&
        scoreLight != null &&
        Number.isFinite(Number(scoreDark)) &&
        Number.isFinite(Number(scoreLight));

    const resultLabel = hasScore
        ? Number(scoreDark) > Number(scoreLight)
            ? "výhra DARK"
            : Number(scoreDark) < Number(scoreLight)
                ? "výhra LIGHT"
                : "Remíza"
        : null;

    const hasStarted = matchDate ? matchDate <= now : false;
    const canAutoLineup = !!match?.id && !hasStarted;   // jen před začátkem
    const canEditScore = !!match?.id && hasStarted;     // od začátku a po skončení

    const handleAutoLineup = async () => {
        try {
            setSaving(true);
            await autoLineupAdmin(match.id);
            showNotification("Automatická první lajna byla vygenerována.", "success");
            if (onRefresh) await onRefresh();
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.message ||
                "Generování první lajny se nezdařilo.";
            showNotification(msg, "danger");
        } finally {
            setSaving(false);
        }
    };

    const handleSaveScore = async () => {
        const light = scoreLightInput === "" ? null : Number(scoreLightInput);
        const dark = scoreDarkInput === "" ? null : Number(scoreDarkInput);

        if (!Number.isFinite(light) || !Number.isFinite(dark)) {
            showNotification("Vyplň prosím platná čísla pro skóre.", "danger");
            return;
        }

        try {
            setSaving(true);
            await updateMatchScoreAdmin(match.id, light, dark);
            showNotification("Skóre bylo uloženo.", "success");
            if (onRefresh) await onRefresh();
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.message ||
                "Uložení skóre se nezdařilo.";
            showNotification(msg, "danger");
        } finally {
            setSaving(false);
        }
    };

    const handleMarkNoExcuse = async (playerId, adminNote) => {
        try {
            setSaving(true);
            await markNoExcusedAdmin(
                match.id,
                playerId,
                adminNote && adminNote.trim()
                    ? adminNote.trim()
                    : "Admin: bez omluvy"
            );
            showNotification("Hráč byl označen jako bez omluvy.", "success");
            if (onRefresh) {
                await onRefresh();
            }
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.message ||
                "Neomluvení hráče se nezdařilo.";
            showNotification(msg, "danger");
        } finally {
            setSaving(false);
        }
    };

    const handleCancelNoExcuse = async (playerId, excuseNote) => {
        try {
            setSaving(true);
            await cancelNoExcusedAdmin(
                match.id,
                playerId,
                excuseNote && excuseNote.trim()
                    ? excuseNote.trim()
                    : "Admin: omluven - JINÉ"
            );
            showNotification(
                "Neomluvení bylo zrušeno (hráč omluven - JINÉ).",
                "success"
            );
            if (onRefresh) {
                await onRefresh();
            }
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.message ||
                "Zrušení neomluvení se nezdařilo.";
            showNotification(msg, "danger");
        } finally {
            setSaving(false);
        }
    };

    // obaly pro aktuálně vybraného hráče v modalu
    const handleMarkNoExcuseForHistoryPlayer = async (adminNote) => {
        if (!historyPlayer) return;
        await handleMarkNoExcuse(historyPlayer.id, adminNote);
    };

    const handleCancelNoExcuseForHistoryPlayer = async (excuseNote) => {
        if (!historyPlayer) return;
        await handleCancelNoExcuse(historyPlayer.id, excuseNote);
    };

    // TODO: změna týmu pro aktuálně vybraného hráče v modalu
    const handleChangeTeamForHistoryPlayer = async () => {
        if (!historyPlayer) return;
        try {
            setSaving(true);
            await changeRegistrationTeamAdmin(historyPlayer.id, match.id);
            showNotification("Tým hráče byl změněn.", "success");
            if (onRefresh) {
                await onRefresh();
            }
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.message ||
                "Změna týmu hráče se nezdařila.";
            showNotification(msg, "danger");
            // důležité: vyhodíme chybu dál, aby ji modal poznal a neukazoval success
            throw err; // TODO
        } finally {
            setSaving(false);
        }
    };
    
    return (
        <div className="card">
            <div className="card-body match-info-body">
                <div className="admin-match-info-header mb-3">
                    <div className="admin-match-info-left">
                        {match.description && (
                            <p className="card-text mb-2">
                                <strong>Popis: </strong>
                                {match.description}
                            </p>
                        )}

                        <p className="card-text mb-2 admin-players-total-row">
                            <strong>Hráči celkem: </strong>
                            {match.inGamePlayers} / {match.maxPlayers}
                        </p>

                        <p className="card-text mb-2">
                            <TeamDarkIcon className="match-reg-team-icon-dark" />
                            {match.inGamePlayersDark} /{" "}
                            {match.inGamePlayersLight} <TeamLightIcon className="match-reg-team-icon-light" />

                        </p>

                        {isPastMatch && hasScore && (
                            <p className="card-text mb-2">
                                <strong>Skóre: </strong>
                                {scoreDark} : {scoreLight}
                                {resultLabel && (
                                    <span className="ms-1">
                                        – {" pro "}
                                        {resultLabel === "výhra DARK" ? (
                                            <TeamDarkIcon className="match-reg-team-icon-dark" />
                                        ) : resultLabel === "výhra LIGHT" ? (
                                            <TeamLightIcon className="match-reg-team-icon-light" />
                                        ) : resultLabel === "DRAW" ? (
                                            Remíza
                                        ) : null}
                                    </span>
                                )}
                            </p>
                        )}

                        {match.price != null && (
                            <p className="card-text mb-2 admin-price-row">
                                <strong>Cena: </strong>
                                {match.price} Kč / <strong> hráč: </strong>
                                {match.pricePerRegisteredPlayer.toFixed(0)} Kč
                            </p>
                        )}
                    </div>

                    <RoleGuard roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
                        <div className="admin-match-info-actions">
                            {canAutoLineup && (
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-primary"
                                    onClick={handleAutoLineup}
                                    disabled={saving}
                                    title="Vygeneruje automaticky první lajnu"
                                >
                                   AutoLajny TODO
                                </button>
                            )}

                            {canEditScore && (
                                <div className="admin-score-editor">
                                    <input
                                        type="number"
                                        className="form-control form-control-sm"
                                        value={scoreDarkInput}
                                        onChange={(e) =>
                                            setScoreDarkInput(e.target.value)
                                        }
                                        placeholder="Dark"
                                        disabled={saving}
                                    />
                                    <span className="admin-score-sep">:</span>
                                    <input
                                        type="number"
                                        className="form-control form-control-sm"
                                        value={scoreLightInput}
                                        onChange={(e) =>
                                            setScoreLightInput(e.target.value)
                                        }
                                        placeholder="Light"
                                        disabled={saving}
                                    />

                                    <button
                                        type="button"
                                        className="btn btn-sm btn-outline-success"
                                        onClick={handleSaveScore}
                                        disabled={saving}
                                    >
                                        Uložit skóre
                                    </button>
                                </div>
                            )}
                        </div>
                    </RoleGuard>
                </div>

                <h4 className="mt-4">Sestava:</h4>
                <AdminMatchRegistrationInfo
                    match={match}
                    onPlayerClick={setHistoryPlayer}
                />
            </div>

            {historyPlayer && (
                <AdminPlayerRegistrationHistoryModal
                    match={match}
                    player={historyPlayer}
                    saving={saving}
                    onClose={() => setHistoryPlayer(null)}
                    onMarkNoExcuse={handleMarkNoExcuseForHistoryPlayer}
                    onCancelNoExcuse={handleCancelNoExcuseForHistoryPlayer}
                    onChangeTeam={handleChangeTeamForHistoryPlayer}
                />
            )}
        </div>
    );
};

export default AdminMatchInfo;