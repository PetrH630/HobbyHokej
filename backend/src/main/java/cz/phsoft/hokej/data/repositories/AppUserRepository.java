package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByEmail(String email);
}
