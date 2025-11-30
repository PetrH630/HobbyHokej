package cz.phsoft.hokej.models.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class MatchDTO {
    private Long id;
    private LocalDateTime datumCas;
    private Set<PlayerDTO> players = new HashSet<>();

    public MatchDTO() {}

    // get/set
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDatumCas() { return datumCas; }
    public void setDatumCas(LocalDateTime datumCas) { this.datumCas = datumCas; }

    public Set<PlayerDTO> getPlayers() { return players; }
    public void setPlayers(Set<PlayerDTO> players) { this.players = players; }
}
