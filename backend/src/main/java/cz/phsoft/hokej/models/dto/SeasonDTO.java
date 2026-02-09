package cz.phsoft.hokej.models.dto;

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
 */
public class SeasonDTO {

    private Long id;

    /**
     * Název sezóny, například "2024/2025".
     */
    @NotBlank(message = "např. 2025/2026")
    private String name;

    @NotNull(message = "datum sezony OD musí být zadán")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "datum sezony DO musí být zadán")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * Příznak, zda je sezóna aktuálně aktivní.
     *
     * V systému může být v daném okamžiku aktivní nejvýše jedna sezóna.
     */
    private boolean active;

    /**
     * Časové razítko sezóny.
     * Slouží pro zobrazení data a času vytvoření / poslední změny sezóny.
     * Hodnota je spravována na backendu.
     */
    private LocalDateTime timestamp;


    // konstruktory

    public SeasonDTO() {}

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

    // gettery / settery

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
