package cz.phsoft.hokej.player.enums;

/**
 * Typ hráče z hlediska kategorií nebo úrovně.
 *
 * Enum se používá například pro odlišení různých úrovní
 * nebo rolí v rámci týmu, případně pro cenotvorbu.
 */
public enum PlayerType {
    /**
     * Hráč VIP kategorie.
     */
    VIP,

    /**
     * Standardní hráč.
     */
    STANDARD,

    /**
     * Základní typ hráče, výchozí hodnota.
     */
    BASIC
}
