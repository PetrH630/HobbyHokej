package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.ForgottenPasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgottenPasswordResetTokenRepository
        extends JpaRepository<ForgottenPasswordResetTokenEntity, Long> {

    Optional<ForgottenPasswordResetTokenEntity> findByToken(String token);

    void deleteByUser(AppUserEntity user);
}
