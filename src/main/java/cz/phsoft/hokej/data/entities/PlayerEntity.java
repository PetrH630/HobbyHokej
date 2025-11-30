package cz.phsoft.hokej.data.entities;

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

    private String fullName;

    public PlayerEntity() {}

    // Gettery a settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; updateFullName(); }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; updateFullName(); }

    public String getFullName() { return fullName; }

    private void updateFullName() {
        if (name != null && surname != null) {
            this.fullName = name + " " + surname;
        } else if (name != null) {
            this.fullName = name;
        } else if (surname != null) {
            this.fullName = surname;
        } else {
            this.fullName = "";
        }
    }
}
