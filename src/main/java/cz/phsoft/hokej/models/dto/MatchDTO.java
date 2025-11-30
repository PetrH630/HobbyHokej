package cz.phsoft.hokej.models.dto;

import java.time.LocalDateTime;

public class MatchDTO {
    private Long id;
    private LocalDateTime dateTime;
    private String location;
    private String description;

    // gettery a settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
