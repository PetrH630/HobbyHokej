package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerType;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerType type; // VIP, STANDARD, BASIC

    private String fullName;


    public PlayerEntity() {}

    // Gettery a Settery + updateFullName
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; updateFullName(); }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; updateFullName(); }

    public String getFullName() { return fullName; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) { this.type = type; }

    private void updateFullName() { this.fullName = name + " " + surname; }
}
