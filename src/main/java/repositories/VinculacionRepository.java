package repositories;
import entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinculacionRepository extends JpaRepository<Vinculacion, Long> {
}
