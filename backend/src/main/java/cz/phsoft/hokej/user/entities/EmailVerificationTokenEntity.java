package cz.phsoft.hokej.user.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující ověřovací token pro emailovou aktivaci uživatele.
 *
 * Slouží k ověření emailové adresy a aktivaci uživatelského účtu.
 * Token je časově omezený a navázaný na konkrétního uživatele.
 * Entita se používá v procesu registrace a aktivace účtu.
 */
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationTokenEntity {

    /**
     * Primární klíč tokenu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unikátní ověřovací token.
     *
     * Hodnota se používá v aktivačním odkazu zasílaném uživateli.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /**
     * Datum a čas expirace tokenu.
     *
     * Po uplynutí této hodnoty se token považuje za neplatný.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Uživatel, ke kterému je token přiřazen.
     *
     * Token je svázán s jedním konkrétním uživatelem,
     * jehož účet má být aktivován.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public AppUserEntity getUser() { return user; }

    public void setUser(AppUserEntity user) { this.user = user; }
}