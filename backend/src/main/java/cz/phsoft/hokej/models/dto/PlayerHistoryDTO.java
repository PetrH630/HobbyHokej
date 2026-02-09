package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

    /**
     * DTO, které reprezentuje hráče v systému a jeho historii.
     *
     *  Slouží pro auditní a přehledové účely. Obsahuje informace o tom,
     * jak se hráč v čase měnil, včetně původního časového razítka,
     *
     * Datový model odpovídá záznamu v tabulce historie hráče.
     */

    public class PlayerHistoryDTO {

        private Long id;
        private String name;
        private String surname;
        private String nickname;
        private String fullName;
        private String phoneNumber;
        private PlayerType type;
        private Team team;
        private PlayerStatus playerStatus;
       private LocalDateTime timestamp;
       private LocalDateTime originalTimestamp;

        // gettery / settery

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

       public String getSurname() {
           return surname;
       }

       public void setSurname(String surname) {
           this.surname = surname;
       }

       public String getNickname() {
           return nickname;
       }

       public void setNickname(String nickname) {
           this.nickname = nickname;
       }

       public String getFullName() {
           return fullName;
       }

       public void setFullName(String fullName) {
           this.fullName = fullName;
       }

       public String getPhoneNumber() {
           return phoneNumber;
       }

       public void setPhoneNumber(String phoneNumber) {
           this.phoneNumber = phoneNumber;
       }

       public PlayerType getType() {
           return type;
       }

       public void setType(PlayerType type) {
           this.type = type;
       }

       public Team getTeam() {
           return team;
       }

       public void setTeam(Team team) {
           this.team = team;
       }

       public PlayerStatus getPlayerStatus() {
           return playerStatus;
       }

       public void setPlayerStatus(PlayerStatus playerStatus) {
           this.playerStatus = playerStatus;
       }

       public LocalDateTime getTimestamp() {
           return timestamp;
       }

       public void setTimestamp(LocalDateTime timestamp) {
           this.timestamp = timestamp;
       }

       public LocalDateTime getOriginalTimestamp() {
           return originalTimestamp;
       }

       public void setOriginalTimestamp(LocalDateTime originalTimestamp) {
           this.originalTimestamp = originalTimestamp;
       }
   }
