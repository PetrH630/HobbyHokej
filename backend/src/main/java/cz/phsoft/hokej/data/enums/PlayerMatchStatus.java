package cz.phsoft.hokej.data.enums;

/**
 * Stav registrace hráče k zápasu.
 *
 * Jednotlivé hodnoty popisují, jaký je aktuální vztah hráče
 * k danému zápasu z hlediska účasti a reakce.
 */
public enum PlayerMatchStatus {

    REGISTERED,   // registrován
    UNREGISTERED, // odhlášen - chtěl, ale nemůže
    EXCUSED,      // omluven - věděl, že nemůže
    RESERVED,     // náhradník není kapacita, ale chce jít
    NO_RESPONSE,  // bez odpovědi
    SUBSTITUTE,   // náhradník-možná
    NO_EXCUSED,   // neomluven - byl registrován a nepřišel
}
