package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerType;

public class RegisterRequest {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String password;
    private PlayerType type;

    public RegisterRequest(String name, String surname, String email, String phone, String password, PlayerType type) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.type = type;
    }

    // getters
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public PlayerType getType() { return type; }
}
