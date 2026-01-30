package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ForgottenPasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUserEntity user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // volitelné – pokud chceš logovat použití
    private LocalDateTime usedAt;

    // gettery/settery

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
