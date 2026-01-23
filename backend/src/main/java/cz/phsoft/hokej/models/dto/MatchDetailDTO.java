package cz.phsoft.hokej.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;

import java.time.LocalDateTime;
import java.util.List;

public class MatchDetailDTO {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;
    private String location;
    private String description;
    private Integer price;
    private int maxPlayers;
    private int inGamePlayers;
    private int outGamePlayers;
    private int waitingPlayers;
    private int noActionPlayers;
    private int noExcusedPlayersSum;
    private double pricePerRegisteredPlayer;
    private int remainingSlots;
    private PlayerMatchStatus status;

    List<PlayerDTO> registeredPlayers;
    List<PlayerDTO> reservedPlayers;
    List<PlayerDTO> unregisteredPlayers;
    List<PlayerDTO> excusedPlayers;
    List<PlayerDTO> noExcusedPlayers;
    List<PlayerDTO> noResponsePlayers;

    // Gettery a settery



    public Long getId() {       return id;    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public PlayerMatchStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerMatchStatus status) {
        this.status = status;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getInGamePlayers() {
        return inGamePlayers;
    }
    public void setInGamePlayers(int inGamePlayers) {
        this.inGamePlayers = inGamePlayers;
    }

    public void setOutGamePlayers(int outGamePlayers) {
        this.outGamePlayers = outGamePlayers;
    }

    public int getWaitingPlayers() {
        return waitingPlayers;
    }
    public void setWaitingPlayers(int waitingPlayers) {
        this.waitingPlayers = waitingPlayers;
    }

    public int getNoActionPlayers() {
        return noActionPlayers;
    }
    public void setNoActionPlayers(int noActionPlayers) {
        this.noActionPlayers = noActionPlayers;
    }

    public double getPricePerRegisteredPlayer() {
        return pricePerRegisteredPlayer;
    }
    public void setPricePerRegisteredPlayer(double pricePerRegisteredPlayer) {
        this.pricePerRegisteredPlayer = pricePerRegisteredPlayer;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }
    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    public List<PlayerDTO> getRegisteredPlayers() {
        return registeredPlayers;
    }
    public void setRegisteredPlayers(List<PlayerDTO> registeredPlayers) {
        this.registeredPlayers = registeredPlayers;
    }

    public List<PlayerDTO> getReservedPlayers() {
        return reservedPlayers;
    }
    public void setReservedPlayers(List<PlayerDTO> reservedPlayers) {
        this.reservedPlayers = reservedPlayers;
    }

    public List<PlayerDTO> getUnregisteredPlayers() {
        return unregisteredPlayers;
    }

    public void setUnregisteredPlayers(List<PlayerDTO> unregisteredPlayers) {
        this.unregisteredPlayers = unregisteredPlayers;
    }
    public List<PlayerDTO> getExcusedPlayers() {
        return excusedPlayers;
    }
    public void setExcusedPlayers(List<PlayerDTO> excusedPlayers) {
        this.excusedPlayers = excusedPlayers;
    }

    public List<PlayerDTO> getNoResponsePlayers() {
        return noResponsePlayers;
    }
    public void setNoResponsePlayers(List<PlayerDTO> noResponsePlayers) {
        this.noResponsePlayers = noResponsePlayers;
    }

    public int getOutGamePlayers() {
        return outGamePlayers;
    }

    public int getNoExcusedPlayersSum() {
        return noExcusedPlayersSum;
    }

    public void setNoExcusedPlayersSum(int noExcusedPlayersSum) {
        this.noExcusedPlayersSum = noExcusedPlayersSum;
    }

    public List<PlayerDTO> getNoExcusedPlayers() {
        return noExcusedPlayers;
    }

    public void setNoExcusedPlayers(List<PlayerDTO> noExcusedPlayers) {
        this.noExcusedPlayers = noExcusedPlayers;
    }
}
