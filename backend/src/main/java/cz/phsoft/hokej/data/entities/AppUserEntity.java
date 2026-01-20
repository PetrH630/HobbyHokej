package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.Role;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "app_users")
public class AppUserEntity {

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
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = false; // výchozí hodnota false


    // One-to-Many: jeden uživatel → více hráčů
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerEntity> players;

    // gettery/settery


    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getSurname() { return surname; }

    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public Role getRole() {return role;}

    public void setRole(Role role) {this.role = role;}

    public boolean isEnabled() {    return enabled;    }

    public void setEnabled(boolean enabled) {  this.enabled = enabled;    }

    public Set<PlayerEntity> getPlayers() {return players;}

    public void setPlayers(Set<PlayerEntity> players) {this.players = players;}
}
