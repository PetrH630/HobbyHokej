package cz.phsoft.hokej.data.enums;

/**
 * Stav hráče v rámci aplikace.
 *
 * Stav se používá při schvalovacím procesu administrátorem
 * a může ovlivňovat viditelnost hráče v různých přehledech.
 */
public enum PlayerStatus {
    PENDING,  // čeká na schválení
    APPROVED, // schváleno administrátorem
    REJECTED,  // zamítnuto
    ARCHIVED   // TODO implementovat místo smazání hráče
}
