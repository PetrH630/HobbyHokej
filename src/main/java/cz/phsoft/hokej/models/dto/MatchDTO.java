package cz.phsoft.hokej.models.dto;

import java.time.LocalDateTime;

public class MatchDTO {

    private Long id;
    private LocalDateTime date;

    public MatchDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
