package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entita reprezentující token pro reset zapomenutého hesla.
 *
 * Slouží k bezpečnému nastavení nového hesla uživatele.
 * Token je časově omezený a navázaný na konkrétního uživatele.
 * Volitelné pole usedAt umožňuje evidovat okamžik použití tokenu.
 */
@Entity
@Table(name = "forgotten_password_reset_token_entity")
public class ForgottenPasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unikátní resetovací token.
     */
    @Column(nullable = false, unique = true, length = 128)
    private String token;

    /**
     * Uživatel, ke kterému token patří.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUserEntity user;

    /**
     * Datum a čas expirace tokenu.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Datum a čas, kdy byl token použit.
     * Hodnota je volitelná a slouží pro auditní účely.
     */
    private LocalDateTime usedAt;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public AppUserEntity getUser() { return user; }

    public void setUser(AppUserEntity user) { this.user = user; }

    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }

    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
