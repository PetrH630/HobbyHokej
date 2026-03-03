package cz.phsoft.hokej.season.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO reprezentující sezónu v aplikaci.
 *
 * Slouží k přenosu informací o sezónách mezi backendem a klientem,
 * například při správě sezón nebo výběru aktivní sezóny. Sezóna vymezuje
 * časové období, ve kterém se konají zápasy a ke kterému se vztahují
 * statistiky a přehledy.
 *
 * Třída je používána především ve vrstvách controller a service
 * jako přenosový objekt oddělující interní entitu od veřejného API.
 */
public class SeasonDTO {

    /**
     * Jednoznačný identifikátor sezóny.
     *
     * Hodnota je generována na straně databáze.
     */
    private Long id;

    /**
     * Název sezóny, například "2024/2025".
     *
     * Musí být neprázdný a slouží k jednoznačné identifikaci sezóny
     * v uživatelském rozhraní.
     */
    @NotBlank(message = "např. 2025/2026")
    private String name;

    /**
     * Datum začátku sezóny.
     *
     * Určuje první den, od kterého je sezóna platná.
     */
    @NotNull(message = "datum sezony OD musí být zadán")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * Datum konce sezóny.
     *
     * Určuje poslední den, do kterého je sezóna platná.
     */
    @NotNull(message = "datum sezony DO musí být zadán")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * Příznak, zda je sezóna aktuálně aktivní.
     *
     * V systému může být v daném okamžiku aktivní nejvýše jedna sezóna.
     * Hodnota je nastavována prostřednictvím administrativních operací.
     */
    private boolean active;

    /**
     * Časové razítko sezóny.
     *
     * Slouží pro zobrazení data a času vytvoření nebo poslední změny sezóny.
     * Hodnota je spravována na backendu a nemá být nastavována klientem.
     */
    private LocalDateTime timestamp;

    /**
     * Bezparametrický konstruktor používaný při serializaci
     * a deserializaci objektu.
     */
    public SeasonDTO() {}

    /**
     * Konstruktor pro vytvoření DTO s vyplněnými základními údaji.
     *
     * Používá se zejména při mapování entity na DTO.
     *
     * @param id identifikátor sezóny
     * @param name název sezóny
     * @param startDate datum začátku sezóny
     * @param endDate datum konce sezóny
     * @param active příznak aktivní sezóny
     */
    public SeasonDTO(Long id,
                     String name,
                     LocalDate startDate,
                     LocalDate endDate,
                     boolean active) {

        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}