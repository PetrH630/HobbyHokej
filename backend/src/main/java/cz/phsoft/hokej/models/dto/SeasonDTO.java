package cz.phsoft.hokej.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class SeasonDTO {

    private Long id;
    /**
     * Např. "2024/2025"
     */
    @NotBlank(message = "např. 2025/2026")
    private String name;

    @NotBlank(message = "datum sezony OD musí být zadán")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotBlank(message = "datum sezony DO musí být zadán")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * Je tato sezóna aktuálně aktivní
     */
    private boolean active;

    // --- konstruktory ---

    public SeasonDTO() {
    }

    public SeasonDTO(
            Long id,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    // --- gettery / settery ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}


