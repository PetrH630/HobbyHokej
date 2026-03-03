package cz.phsoft.hokej.shared.dto;

/**
 * DTO reprezentující standardizovanou úspěšnou odpověď API.
 *
 * Používá se jako návratová hodnota u operací, které provedly změnu
 * stavu systému, například při vytvoření, aktualizaci, smazání
 * nebo schválení entity. Objekt umožňuje jednotným způsobem
 * předat textovou zprávu, identifikátor dotčené entity
 * a čas vzniku odpovědi.
 *
 * Třída je neměnná. Hodnoty jsou nastaveny prostřednictvím
 * konstruktoru a následně již nejsou měněny.
 */
public class SuccessResponseDTO {

    /**
     * Textová zpráva popisující výsledek operace.
     */
    private final String message;

    /**
     * ID entity, které se operace týkala.
     *
     * Může být null, pokud operace nemá přímou vazbu na konkrétní entitu.
     */
    private final Long id;

    /**
     * Čas vytvoření odpovědi.
     *
     * Čas se typicky předává ve formátu ISO-8601 a slouží
     * k ladění nebo zobrazování v logu na frontendu.
     */
    private final String timestamp;

    /**
     * Vytvoří instanci standardizované úspěšné odpovědi.
     *
     * @param message textová zpráva popisující výsledek operace
     * @param id identifikátor entity, které se operace týkala, nebo null
     * @param timestamp čas vytvoření odpovědi ve formátu ISO-8601
     */
    public SuccessResponseDTO(String message, Long id, String timestamp) {
        this.message = message;
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public Long getId() { return id; }
    public String getTimestamp() { return timestamp; }
}