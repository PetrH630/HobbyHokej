package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Role;
import jakarta.persistence.*;

@Entity
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String playerPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType type; // VIP, STANDARD, BASIC

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // PLAYER, MANAGER, ADMIN

    private String fullName;

    private boolean enabled = false; // false = čeká na schválení

    public PlayerEntity() {}

    // Gettery a Settery + updateFullName
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; updateFullName(); }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; updateFullName(); }

    public String getFullName() { return fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPlayerPassword() { return playerPassword; }
    public void setPlayerPassword(String playerPassword) { this.playerPassword = playerPassword; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) { this.type = type; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    private void updateFullName() { this.fullName = name + " " + surname; }
}
