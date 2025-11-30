package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
}
