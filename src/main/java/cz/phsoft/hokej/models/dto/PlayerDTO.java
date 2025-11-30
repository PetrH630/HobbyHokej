package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerType;

public class PlayerDTO {

    private Long id;
    private String name;
    private String surname;
    private String fullName;
    private String email;
    private String phone;
    private PlayerType type;

    public PlayerDTO() {}

    public PlayerDTO(Long id, String name, String surname, String email, String phone, PlayerType type) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.fullName = name + " " + surname;
    }

    // --- Gettery a Settery ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        updateFullName();
    }

    public String getSurname() { return surname; }
    public void setSurname(String surname) {
        this.surname = surname;
        updateFullName();
    }

    public String getFullName() { return fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) { this.type = type; }

    private void updateFullName() {
        this.fullName = (name != null ? name : "") + " " + (surname != null ? surname : "");
    }
}
