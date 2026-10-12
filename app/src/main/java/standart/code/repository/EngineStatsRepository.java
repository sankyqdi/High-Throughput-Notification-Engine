package standart.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import standart.code.model.EngineStats;

import java.util.Optional;

@Repository
public interface EngineStatsRepository extends JpaRepository<EngineStats, Long> {

    Optional<EngineStats> findTopByOrderByIdDesc();

}
