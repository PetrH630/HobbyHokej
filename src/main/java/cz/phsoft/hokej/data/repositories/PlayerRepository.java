package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByEmail(String email);
}
