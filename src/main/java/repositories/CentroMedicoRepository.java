package repositories;
import entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CentroMedicoRepository extends JpaRepository<CentroMedico, Long> {
    boolean existsByCorreo(String correo);
    Optional<CentroMedico> findByCorreo(String correo);
}