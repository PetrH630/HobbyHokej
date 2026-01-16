package cz.phsoft.hokej.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;

import java.time.LocalDateTime;

public class MatchOverviewDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    private String location;
    private String description;
    private Integer price;
    private int maxPlayers;
    private int inGamePlayers;
    private double pricePerRegisteredPlayer;
    private PlayerMatchStatus status;

    // Gettery a settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public int getInGamePlayers() { return inGamePlayers; }
    public void setInGamePlayers(int inGamePlayers) { this.inGamePlayers = inGamePlayers; }

    public double getPricePerRegisteredPlayer() { return pricePerRegisteredPlayer; }
    public void setPricePerRegisteredPlayer(double pricePerRegisteredPlayer) { this.pricePerRegisteredPlayer = pricePerRegisteredPlayer; }

    public PlayerMatchStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerMatchStatus status) {
        this.status = status;
    }
}
