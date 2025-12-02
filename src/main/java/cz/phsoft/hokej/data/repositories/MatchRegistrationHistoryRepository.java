package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRegistrationHistoryRepository extends JpaRepository<MatchRegistrationHistoryEntity, Long> {}