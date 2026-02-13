// src/utils/registrationFormatters.js

export const formatDateTime = (value) => {
    if (!value) return "";
    return new Date(value).toLocaleString("cs-CZ");
};

export const statusLabel = (status) => {
    const map = {
        REGISTERED: "Přihlášen",
        UNREGISTERED: "Odhlášen",
        EXCUSED: "Omluven",
        NO_EXCUSED: "Neomluven",
        SUBSTITUTE: "Možná",
        RESERVED: "Čekatel"
    };
    return map[status] ?? status;
};

export const teamLabel = (team) => {
    if (!team) return "-";
    return team === "DARK" ? "Tmavý" : "Světlý";
};

export const excuseReasonLabel = (excuseReason) =>{
    const map = {
        NEMOC: "Nemoc",
        PRACE: "Práce",
        NECHCE_SE_MI: "Nechce se",
        JINE: "Jiné",
        
    };
    return map[excuseReason] ?? excuseReason;
}
