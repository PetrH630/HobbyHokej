package cz.phsoft.hokej.models.dto;

public class PlayerDTO {

    private Long id;
    private String name;
    private String surname;
    private String fullName;

    public PlayerDTO() {
    }

    public PlayerDTO(Long id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        updateFullName();
    }

    // --- Gettery a settery ---

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
        updateFullName();
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
        updateFullName();
    }

    public String getFullName() {
        return fullName;
    }

    // fullName je readonly → žádný setter
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
